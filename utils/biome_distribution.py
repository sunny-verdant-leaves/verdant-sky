#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修正版生物群系分布估算器 / Biome Distribution Estimator
=================================================
用法: python biome_estimator.py <world_preset.json> [采样数]
      兼容旧写法: python biome_estimator.py <world_preset.json> <noise_settings.json> [采样数]

参数:
    world_preset.json    世界预设文件（包含 dimensions.minecraft:overworld.generator.biome_source.biomes），
                         也可以直接传入解包后的 multi_noise_biome_source_parameter_list.json
    noise_settings.json  可选。提供时额外估算「出生点区域」内的生物群系分布
    采样数                默认 200000

示例:
    python utils/biome_distribution.py world_preset.json
    python utils/biome_distribution.py common/src/main/resources/data/verdant_sky/worldgen/world_preset/void.json common/src/main/resources/data/verdant_sky/worldgen/noise_settings/void/overworld.json 20000

与旧版 spawn_estimator 的差异:
    旧版在 spawn_target 范围内均匀采样 -> 只估出生点附近
    新版在整个参数空间采样，分布按 N(0, 0.3^2) 截断 -> 估全图真实分布
"""

import json
import random
import sys
from collections import defaultdict

PARAMS = ('temperature', 'humidity', 'continentalness',
          'erosion', 'weirdness', 'depth')
SIGMA = 0.3       # Minecraft 气候噪声近似正态的 σ
BAR_WIDTH = 28    # 条形图最大宽度


def load_json(path):
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)


def get_overworld_biomes(world_preset):
    dims = world_preset.get('dimensions', {})
    overworld = dims.get('minecraft:overworld', {})
    generator = overworld.get('generator', {})
    biome_source = generator.get('biome_source', {})
    return biome_source.get('biomes', [])


def to_range(val):
    if isinstance(val, list):
        a, b = float(val[0]), float(val[1])
        return (a, b) if a <= b else (b, a)
    v = float(val)
    return (v, v)


def build_table(biomes):
    table = []
    for entry in biomes:
        name = entry.get('biome') or entry.get('name')
        if not name:
            continue
        params = entry.get('parameters', {})
        ranges = {p: to_range(params.get(p, 0.0)) for p in PARAMS}
        table.append((name, ranges))
    return table


def axis_envelope(table):
    env = {}
    for p in PARAMS:
        lo = min(r[p][0] for _, r in table)
        hi = max(r[p][1] for _, r in table)
        if hi - lo < 1e-9:
            lo -= 0.5
            hi += 0.5
        env[p] = (lo, hi)
    return env


def spawn_envelope(noise_settings):
    """从 spawn_target 提取采样包络（各轴并集）。"""
    targets = noise_settings.get('spawn_target') or []
    if not targets:
        return None
    env = {}
    for p in PARAMS:
        los, his = [], []
        for st in targets:
            v = st.get(p)
            if v is None:
                continue
            lo, hi = to_range(v)
            los.append(lo)
            his.append(hi)
        env[p] = (min(los), max(his)) if los else (-1.0, 1.0)
    return env


def sample_point(rng, env):
    """截断正态采样。env 为 None 时退化为均匀。"""
    pt = {}
    for p, (lo, hi) in env.items():
        if lo == hi:
            pt[p] = lo
            continue
        for _ in range(200):
            v = rng.gauss(0.0, SIGMA)
            if lo <= v <= hi:
                pt[p] = v
                break
        else:
            pt[p] = rng.uniform(lo, hi)
    return pt


def distance_to_range(value, rng):
    lo, hi = rng
    if value < lo:
        return lo - value
    if value > hi:
        return value - hi
    return 0.0


def biome_distance(point, ranges):
    total = 0.0
    for p, v in point.items():
        if p not in ranges:
            continue
        d = distance_to_range(v, ranges[p])
        total += d * d
    return total


def run_sampling(table, env, samples, seed=20240101):
    """给定采样包络，跑 N 次最近邻归类。返回 (counts, dist_total)。"""
    rng = random.Random(seed)
    counts = defaultdict(int)
    dist_total = 0.0
    for _ in range(samples):
        point = sample_point(rng, env)
        best_name, best_dist = None, float('inf')
        for name, ranges in table:
            d = biome_distance(point, ranges)
            if d < best_dist:
                best_dist = d
                best_name = name
        counts[best_name] += 1
        dist_total += best_dist
    return counts, dist_total


def print_envelope(title, env):
    print(title)
    for p in PARAMS:
        lo, hi = env[p]
        print(f'  {p:<16s} [{lo:+.3f}, {hi:+.3f}]')


def print_distribution(title, counts, total, dist_total):
    print()
    print('=' * 72)
    print(title)
    print('=' * 72)

    items = sorted(counts.items(), key=lambda x: (-x[1], x[0]))
    if not items:
        print('（无数据）')
        return
    top = items[0][1]

    print(f"{'生物群系':<44s}{'占比':>9s}")
    print('-' * 72)
    for name, cnt in items:
        pct = cnt / total * 100
        bar = '█' * max(1, round(cnt / top * BAR_WIDTH)) if cnt else ''
        print(f'{name:<44s}{pct:8.3f}%  {bar}')
    print('-' * 72)
    print(f'共 {len(items)} 个生物群系 / {total} 次采样，'
          f'平均最近距离 {dist_total / total:.5f}')


def estimate(world_preset_path, noise_settings_path=None, samples=200000):
    world_preset = load_json(world_preset_path)
    biomes = get_overworld_biomes(world_preset)
    if not biomes:
        print('错误：未找到主世界生物群系列表')
        return

    table = build_table(biomes)
    env = axis_envelope(table)

    print(f'生物群系数: {len(table)}')
    print(f'采样数    : {samples}')
    print_envelope('全图采样包络（所有生物群系参数的总包络）:', env)

    # ---- 1. 全图分布 ----
    counts, dist_total = run_sampling(table, env, samples)
    print_distribution(
        '全地图生物群系分布（整个参数空间，N(0, 0.3^2) 截断采样）',
        counts, samples, dist_total,
    )

    # ---- 2. 出生点分布（可选） ----
    if noise_settings_path:
        try:
            noise_settings = load_json(noise_settings_path)
        except (OSError, json.JSONDecodeError) as e:
            print(f'\n警告: 无法读取 {noise_settings_path}: {e}')
            return

        spawn_env = spawn_envelope(noise_settings)
        if spawn_env is None:
            print('\n警告: noise_settings 中没有 spawn_target，跳过出生点估算')
            return

        print()
        print_envelope('出生点采样包络（spawn_target 各轴并集）:', spawn_env)

        counts2, dist2 = run_sampling(table, spawn_env, samples)
        print_distribution(
            '出生点附近生物群系分布（spawn_target 范围内）',
            counts2, samples, dist2,
        )


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('用法: python biome_estimator.py '
              '<world_preset.json> [noise_settings.json] [采样数]')
        sys.exit(1)

    world_preset_path = sys.argv[1]
    noise_settings_path = None
    samples = 200000

    for arg in sys.argv[2:]:
        if arg.lstrip('+-').isdigit():
            samples = int(arg)
        elif noise_settings_path is None:
            noise_settings_path = arg

    estimate(world_preset_path, noise_settings_path, samples)