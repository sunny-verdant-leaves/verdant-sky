#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Botania 花卉资源与数据生成器 / Botania Flower Generator
=================================================
为 Verdant Sky 批量生成 Botania 风格的花（普通与浮空）的资源和数据。
包括：方块状态、模型、纹理、战利品表、标签
不包括：本地化

用法:
    python utils/flower_gen.py [--output <目录>]
"""

import argparse
import json
from pathlib import Path

# ══════════════════════════════════════════════════════════════
#  配置区
# ══════════════════════════════════════════════════════════════

MOD_ID = "verdant_sky"

# 花卉列表配置
# type 可选: "misc" (杂项), "generating" (产能), "functional" (功能)
FLOWERS = [
    {"id": "decay_agapanthus", "type": "functional", "has_floating": True},
    {"id": "drinking_snow_lotus", "type": "generating", "has_floating": True},
]

# ══════════════════════════════════════════════════════════════
#  工具函数
# ══════════════════════════════════════════════════════════════

def write_json(path: Path, data: dict):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def load_and_append_json(path: Path, new_values: list):
    """用于向已有的数组 JSON (如 tag) 中追加元素"""
    path.parent.mkdir(parents=True, exist_ok=True)
    data = {"replace": False, "values": []}
    if path.exists():
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
            
    if "values" not in data:
        data["values"] = []
        
    for val in new_values:
        if val not in data["values"]:
            data["values"].append(val)
            
    write_json(path, data)

# ══════════════════════════════════════════════════════════════
#  1. 资源文件 (Blockstates & Models)
# ══════════════════════════════════════════════════════════════

def gen_assets(root: Path):
    bs_dir = root / "assets" / MOD_ID / "blockstates"
    block_model_dir = root / "assets" / MOD_ID / "models" / "block"
    item_model_dir = root / "assets" / MOD_ID / "models" / "item"

    for flower in FLOWERS:
        fid = flower["id"]
        
        # --普通花--
        # 方块状态
        write_json(bs_dir / f"{fid}.json", {
            "variants": { "": { "model": f"{MOD_ID}:block/{fid}" } }
        })
        
        # 方块模型
        write_json(block_model_dir / f"{fid}.json", {
            "parent": "botania:block/shapes/cross",
            "textures": { "cross": f"{MOD_ID}:block/{fid}" }
        })
        
        # 物品模型
        write_json(item_model_dir / f"{fid}.json", {
            "parent": "minecraft:item/generated",
            "textures": { "layer0": f"{MOD_ID}:block/{fid}" }
        })

        # --浮空花--
        if flower.get("has_floating"):
            floating_id = f"floating_{fid}"
            
            # 方块状态
            write_json(bs_dir / f"{floating_id}.json", {
                "variants": { "": { "model": f"{MOD_ID}:block/{floating_id}" } }
            })
            
            # 方块模型
            write_json(block_model_dir / f"{floating_id}.json", {
                "parent": "minecraft:block/block",
                "flower": {
                    "parent": f"{MOD_ID}:block/{fid}"
                },
                "loader": "botania:floating_flower"
            })
            
            # 物品模型
            write_json(item_model_dir / f"{floating_id}.json", {
                "parent": f"{MOD_ID}:block/{floating_id}"
            })

    print(f"  [assets]     生成完毕")

# ══════════════════════════════════════════════════════════════
#  2. 战利品表 (Loot Tables)
# ══════════════════════════════════════════════════════════════

def gen_loot_tables(root: Path):
    lt_dir = root / "data" / MOD_ID / "loot_tables" / "blocks"

    def create_simple_loot(item_name: str):
        return {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{ "type": "minecraft:item", "name": item_name }],
                "conditions": [{ "condition": "minecraft:survives_explosion" }]
            }]
        }

    for flower in FLOWERS:
        fid = flower["id"]
        # 普通花掉落
        write_json(lt_dir / f"{fid}.json", create_simple_loot(f"{MOD_ID}:{fid}"))

        # 浮空花掉落
        if flower.get("has_floating"):
            floating_id = f"floating_{fid}"
            write_json(lt_dir / f"{floating_id}.json", create_simple_loot(f"{MOD_ID}:{floating_id}"))

    print(f"  [loot]       生成完毕")

# ══════════════════════════════════════════════════════════════
#  3. 标签 (Tags)
# ══════════════════════════════════════════════════════════════

def gen_tags(root: Path):
    # 1. 所有浮空花都应为锹可挖掘
    shovel_tag_path = root / "data" / "minecraft" / "tags" / "blocks" / "mineable" / "shovel.json"
    floating_ids = [f"{MOD_ID}:floating_{f['id']}" for f in FLOWERS if f.get("has_floating")]
    if floating_ids:
        load_and_append_json(shovel_tag_path, floating_ids)

    # 2. Botania 花卉标签分类
    for flower in FLOWERS:
        fid = flower["id"]
        ftype = flower.get("type", "misc")
        
        # 收集需要添加的 ID
        normal_id = f"{MOD_ID}:{fid}"
        floating_id = f"{MOD_ID}:floating_{fid}" if flower.get("has_floating") else None

        # special_flowers
        tag_special = root / "data" / "botania" / "tags" / "blocks" / "special_flowers.json"
        load_and_append_json(tag_special, [normal_id])

        # functional_special_flower, etc.
        tag_special_sub = root / "data" / "botania" / "tags" / "blocks" / f"{ftype}_special_flowers.json"
        load_and_append_json(tag_special_sub, [normal_id])

        # functional_floating_flowers, etc.
        if floating_id:
            tag_float_sub = root / "data" / "botania" / "tags" / "blocks" / f"{ftype}_floating_flowers.json"
            load_and_append_json(tag_float_sub, [floating_id])

    print(f"  [tags]       生成完毕")

# ══════════════════════════════════════════════════════════════
#  入口
# ══════════════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(description="生成 Botania 花卉资产与数据。")
    parser.add_argument("--output", default="common/src/main/resources", help="资源根目录")
    args = parser.parse_args()

    root = Path(args.output)
    if not root.is_dir():
        print(f"[!] 目录不存在：{root}")
        return

    print(f"模组        : {MOD_ID}")
    print(f"待处理花卉  : {len(FLOWERS)} 种")
    print("-" * 60)

    gen_assets(root)
    gen_loot_tables(root)
    gen_tags(root)

    print("-" * 60)
    print("全部生成完毕！")

if __name__ == "__main__":
    main()