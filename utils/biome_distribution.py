#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生物群系分布估算器 / Biome Distribution Estimator
=================================================

用法 / Usage:
    python biome_distribution.py <world_preset.json> [noise_settings.json] [采样数]

参数:
    world_preset.json    世界预设文件（包含 dimensions.minecraft:overworld.generator.biome_source.biomes），
                         也可以直接传入解包后的 multi_noise_biome_source_parameter_list.json
    noise_settings.json  可选。提供时额外估算「出生点区域」内的生物群系分布
    采样数                默认 200000

示例:
    python biome_distribution.py world_preset.json
    python biome_distribution.py world_preset.json noise_settings.json 500000

原理:
    Minecraft 的多噪声生物群系源把每个生物群系映射到 6 维气候参数空间中的一点/区间:
        temperature / humidity / continentalness / erosion / weirdness / depth
    世界中任意位置先被采样成该空间中的一个点，然后选出「适应度」最小的生物群系。
    适应度 = 各坐标轴到该生物群系参数区间的距离平方和（欧氏距离的平方）。

    若近似认为气候参数在世界范围内均匀分布，则在参数空间内均匀撒点，
    对每个点做最近邻归类，得到的命中比例就是该生物群系面积占比的估算值。

    与原脚本的区别:
      · 原脚本只在 spawn_target 范围内采样 —— 估的是「出生点附近」的分布；
      · 本脚本默认在整个参数空间采样 —— 估的是「全地图」的分布，
        并把 spawn_target 分布作为附加结果输出。

    注意: 这是统计近似。实际世界中气候参数的边缘分布并非严格均匀
    （尤其 weirdness 轴），因此结果应作为参考量级而非精确值。
"""

from __future__ import annotations

import json
import random
import sys
from collections import defaultdict

PARAMS = (
    "temperature",
    "humidity",
    "continentalness",
    "erosion",
    "weirdness",
    "depth",
)

USAGE = __doc__.split("原理:")[0].strip()

try:
    import numpy as _np
except ImportError:  # pragma: no cover
    _np = None


# --------------------------------------------------------------------------- #
# 数据读取
# --------------------------------------------------------------------------- #

def load_json(path):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def get_biome_list(world_preset, dimension="minecraft:overworld"):
    """从世界预设中取出指定维度的 multi_noise 生物群系列表。

    返回 (biomes, source_type)。
    """
    # 情形 A: 传入的就是 multi_noise_biome_source_parameter_list.json
    if "dimensions" not in world_preset and isinstance(world_preset.get("biomes"), list):
        return world_preset["biomes"], world_preset.get("type", "minecraft:multi_noise")

    dims = world_preset.get("dimensions")
    if dims is None:
        if "generator" in world_preset:
            dims = {dimension: world_preset}
        else:
            raise KeyError("world_preset 中既没有 dimensions，也没有 generator")

    overworld = dims.get(dimension)
    if overworld is None:
        # 退而求其次: 找第一个带 biomes 的维度
        for dim_name, dim in dims.items():
            src = dim.get("generator", {}).get("biome_source", {})
            if src.get("biomes"):
                overworld = dim
                break
        if overworld is None:
            raise KeyError(f"在 world_preset 中找不到维度 {dimension!r}（也没有其他含 biomes 的维度）")

    biome_source = overworld.get("generator", {}).get("biome_source", {})
    source_type = biome_source.get("type", "")
    biomes = biome_source.get("biomes")

    if not biomes:
        preset = biome_source.get("preset")
        if preset:
            raise KeyError(
                f"biome_source 使用了预设 {preset!r}，没有内联 biomes 数组。\n"
                f"       请把解包后的数据包文件\n"
                f"       data/<命名空间>/worldgen/multi_noise_biome_source_parameter_list/<名称>.json\n"
                f"       作为 world_preset 传入。"
            )
        biomes = []

    return biomes, source_type


def _span(value):
    """把参数值统一成 (min, max)。点值 -> (v, v)。"""
    if isinstance(value, (list, tuple)):
        if len(value) != 2:
            raise ValueError(f"参数区间必须是 [min, max]，实际得到 {value!r}")
        lo, hi = float(value[0]), float(value[1])
        return (lo, hi) if hi >= lo else (hi, lo)
    v = float(value)
    return v, v


def build_table(biomes):
    """把生物群系列表转换成便于计算的表结构。"""
    table = []
    missing = set()
    for idx, entry in enumerate(biomes):
        name = entry.get("biome") or entry.get("name")
        if name is None:
            continue
        params = entry.get("parameters") or {}
        lo, hi = [], []
        for p in PARAMS:
            if p in params:
                a, b = _span(params[p])
            else:
                missing.add(p)
                a = b = 0.0
            lo.append(a)
            hi.append(b)
        table.append({"index": idx, "name": name, "lo": lo, "hi": hi})
    return table, missing


def axis_ranges(table):
    """由数据自动推导每个参数轴的采样范围（所有生物群系条目的包络盒）。"""
    ranges = {}
    for j, p in enumerate(PARAMS):
        lo = min(e["lo"][j] for e in table)
        hi = max(e["hi"][j] for e in table)
        if hi - lo < 1e-9:          # 该轴在所有条目上都是常量，给个宽度避免退化
            lo -= 0.5
            hi += 0.5
        ranges[p] = (lo, hi)
    return ranges


def build_spawn_targets(noise_settings):
    """把 noise_settings 的 spawn_target 转成 [(lo 列表, hi 列表), ...]。"""
    targets = []
    for st in noise_settings.get("spawn_target") or []:
        lo, hi = [], []
        for p in PARAMS:
            v = st.get(p)
            if v is None:
                lo.append(-1.0)
                hi.append(1.0)
            else:
                a, b = _span(v)
                lo.append(a)
                hi.append(b)
        targets.append((lo, hi))
    return targets


# --------------------------------------------------------------------------- #
# 采样器
# --------------------------------------------------------------------------- #

def _make_sampler_np(box, targets=None):
    """numpy 采样器: (rng, n) -> ndarray(n, 6)"""
    los, his = box
    los = _np.asarray(los, dtype=_np.float64)
    his = _np.asarray(his, dtype=_np.float64)

    if targets:
        tlo = _np.asarray([t[0] for t in targets], dtype=_np.float64)
        thi = _np.asarray([t[1] for t in targets], dtype=_np.float64)
        count = len(targets)

        def sample(rng, n):
            idx = rng.integers(0, count, size=n)
            lo = tlo[idx]
            hi = thi[idx]
            return lo + rng.random((n, len(PARAMS))) * (hi - lo)

        return sample

    def sample(rng, n):
        return los + rng.random((n, len(PARAMS))) * (his - los)

    return sample


def _make_sampler_py(box, targets=None):
    """纯 Python 采样器: (rnd, n) -> list[list[float]]"""
    if targets:
        count = len(targets)

        def sample(rnd, n):
            out = []
            for _ in range(n):
                lo, hi = targets[rnd.randrange(count)]
                out.append([rnd.uniform(a, b) if b > a else a for a, b in zip(lo, hi)])
            return out

        return sample

    los, his = box

    def sample(rnd, n):
        return [
            [rnd.uniform(a, b) if b > a else a for a, b in zip(los, his)]
            for _ in range(n)
        ]

    return sample


# --------------------------------------------------------------------------- #
# 最近邻归类（核心）
# --------------------------------------------------------------------------- #

def _estimate_np(table, samples, seed, sampler):
    rng = _np.random.default_rng(seed)
    B = len(table)
    P = len(PARAMS)
    blo = _np.array([e["lo"] for e in table], dtype=_np.float64)
    bhi = _np.array([e["hi"] for e in table], dtype=_np.float64)

    counts = _np.zeros(B, dtype=_np.int64)
    dist_total = 0.0
    chunk = 16384
    done = 0

    while done < samples:
        n = min(chunk, samples - done)
        pts = sampler(rng, n)                       # (n, P)

        best_d = _np.full(n, _np.inf, dtype=_np.float64)
        best_i = _np.zeros(n, dtype=_np.int64)

        for b in range(B):
            d = _np.zeros(n, dtype=_np.float64)
            for j in range(P):
                v = pts[:, j]
                low = blo[b, j] - v                 # v 低于区间下界
                high = v - bhi[b, j]                # v 高于区间上界
                _np.maximum(low, 0.0, out=low)
                _np.maximum(high, 0.0, out=high)
                d += low * low
                d += high * high
            better = d < best_d                     # 严格小于 -> 平局时保留先出现的
            if better.any():
                best_d[better] = d[better]
                best_i[better] = b

        counts += _np.bincount(best_i, minlength=B)
        dist_total += float(best_d.sum())
        done += n

    return counts.tolist(), dist_total


def _estimate_py(table, samples, seed, sampler):
    rnd = random.Random(seed)
    P = len(PARAMS)
    lohi = [(e["lo"], e["hi"]) for e in table]
    B = len(lohi)

    counts = [0] * B
    dist_total = 0.0
    chunk = 4096
    done = 0

    while done < samples:
        n = min(chunk, samples - done)
        for pt in sampler(rnd, n):
            best_d = float("inf")
            best_i = -1
            for b in range(B):
                lo, hi = lohi[b]
                d = 0.0
                for j in range(P):
                    v = pt[j]
                    if v < lo[j]:
                        t = lo[j] - v
                    elif v > hi[j]:
                        t = v - hi[j]
                    else:
                        continue
                    d += t * t
                if d < best_d:
                    best_d = d
                    best_i = b
                    if best_d == 0.0:               # 已是最优，无法更近
                        break
            counts[best_i] += 1
            dist_total += best_d
        done += n

    return counts, dist_total


def run_estimate(table, samples, seed, box, targets=None):
    if _np is not None:
        sampler = _make_sampler_np(box, targets)
        return _estimate_np(table, samples, seed, sampler)
    sampler = _make_sampler_py(box, targets)
    return _estimate_py(table, samples, seed, sampler)


# --------------------------------------------------------------------------- #
# 输出
# --------------------------------------------------------------------------- #

def print_distribution(title, table, counts, total, dist_total, bar_width=28):
    print()
    print("=" * 76)
    print(title)
    print("=" * 76)

    agg = defaultdict(int)
    for entry, c in zip(table, counts):
        agg[entry["name"]] += c

    items = sorted(agg.items(), key=lambda kv: (-kv[1], kv[0]))
    if not items:
        print("（无数据）")
        return

    top = items[0][1]
    print(f"{'生物群系':<48s}{'占比':>10s}")
    print("-" * 76)
    for name, c in items:
        pct = c / total * 100.0
        bar = "█" * max(1, round(c / top * bar_width)) if c else ""
        print(f"{name:<48s}{pct:9.3f}%  {bar}")
    print("-" * 76)
    print(
        f"共 {len(items)} 个不同生物群系 / {len(table)} 个参数条目，"
        f"{total} 次采样，平均最近距离 {dist_total / total:.5f}"
    )


# --------------------------------------------------------------------------- #
# 主流程
# --------------------------------------------------------------------------- #

def main(argv):
    if len(argv) < 2 or argv[1] in ("-h", "--help"):
        print(USAGE)
        return 0 if len(argv) > 1 else 1

    world_preset_path = argv[1]
    noise_settings_path = None
    samples = 200_000

    for a in argv[2:]:
        if a.lstrip("+-").isdigit():
            samples = int(a)
        elif noise_settings_path is None:
            noise_settings_path = a
        else:
            print(f"警告: 忽略了多余参数 {a!r}", file=sys.stderr)

    if samples <= 0:
        print("错误: 采样数必须为正整数", file=sys.stderr)
        return 1

    try:
        world_preset = load_json(world_preset_path)
    except (OSError, json.JSONDecodeError) as e:
        print(f"错误: 无法读取世界预设 {world_preset_path}: {e}", file=sys.stderr)
        return 1

    try:
        biomes, source_type = get_biome_list(world_preset)
    except (KeyError, ValueError) as e:
        print(f"错误: {e}", file=sys.stderr)
        return 1

    if not biomes:
        print("错误: 生物群系列表为空（biome_source 可能不是 multi_noise）", file=sys.stderr)
        return 1

    if source_type and source_type != "minecraft:multi_noise":
        print(
            f"警告: biome_source 类型为 {source_type}，不是 minecraft:multi_noise，"
            f"结果可能无意义",
            file=sys.stderr,
        )

    table, missing = build_table(biomes)
    if not table:
        print("错误: 没有可用的生物群系条目", file=sys.stderr)
        return 1
    if missing:
        print(f"警告: 部分条目缺少参数 {sorted(missing)}，已按 0.0 处理", file=sys.stderr)

    ranges = axis_ranges(table)
    box = ([ranges[p][0] for p in PARAMS], [ranges[p][1] for p in PARAMS])

    print(f"世界预设      : {world_preset_path}")
    print(f"参数条目      : {len(table)} 条 / {len({e['name'] for e in table})} 个不同生物群系")
    print(f"采样数        : {samples}")
    print(f"计算后端      : {'numpy' if _np is not None else '纯 Python（较慢）'}")
    print("参数采样范围（由数据自动推导）:")
    for p in PARAMS:
        lo, hi = ranges[p]
        print(f"    {p:<16s} [{lo:+.3f}, {hi:+.3f}]")

    # ---- 1. 全地图分布 ----
    counts, dist_total = run_estimate(table, samples, seed=20240101, box=box)
    print_distribution(
        "全地图生物群系分布估算（整个参数空间均匀撒点）",
        table, counts, samples, dist_total,
    )

    # ---- 2. 出生点区域分布（可选） ----
    if noise_settings_path:
        try:
            noise_settings = load_json(noise_settings_path)
        except (OSError, json.JSONDecodeError) as e:
            print(f"警告: 无法读取 {noise_settings_path}: {e}，跳过出生点估算", file=sys.stderr)
        else:
            targets = build_spawn_targets(noise_settings)
            if not targets:
                print("警告: noise_settings 中没有 spawn_target，跳过出生点估算", file=sys.stderr)
            else:
                spawn_box = (
                    [min(t[0][j] for t in targets) for j in range(len(PARAMS))],
                    [max(t[1][j] for t in targets) for j in range(len(PARAMS))],
                )
                print()
                print("出生点参数范围（spawn_target 各轴并集）:")
                for j, p in enumerate(PARAMS):
                    print(f"    {p:<16s} [{spawn_box[0][j]:+.3f}, {spawn_box[1][j]:+.3f}]")

                counts2, dist2 = run_estimate(
                    table, samples, seed=20240101, box=spawn_box, targets=targets
                )
                print_distribution(
                    "出生点区域生物群系分布估算（在 spawn_target 内撒点）",
                    table, counts2, samples, dist2,
                )

    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))