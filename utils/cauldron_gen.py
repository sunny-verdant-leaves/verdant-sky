#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
木炼药锅资源生成器 / Wooden Cauldron Generator
=================================================
为 Verdant Sky 的每一种木材批量生成炼药锅的全套资源与数据文件。
包括：方块状态、模型（方块+物品）、战利品表、配方、部分标签（方块+物品）
不包括：纹理、本地化、帕秋莉手册、部分方块标签（minecraft:mineable/axe, minecraft:cauldrons, create:copycat_deny）

用法: python utils/cauldron_gen.py [--output <目录>] [--mod-id <id>]
"""

import argparse
import json
from pathlib import Path

# ══════════════════════════════════════════════════════════════
#  配置区
# ══════════════════════════════════════════════════════════════

MOD_ID = "verdant_sky"

# 木材 → (木板物品ID, 纹理ID, 是否必需(留作标签用))
WOODS = {
    # 原版主世界
    "oak":        ("minecraft:oak_planks",        "minecraft:block/oak_planks",      True),
    "spruce":     ("minecraft:spruce_planks",     "minecraft:block/spruce_planks",   True),
    "birch":      ("minecraft:birch_planks",      "minecraft:block/birch_planks",    True),
    "jungle":     ("minecraft:jungle_planks",     "minecraft:block/jungle_planks",   True),
    "acacia":     ("minecraft:acacia_planks",     "minecraft:block/acacia_planks",   True),
    "dark_oak":   ("minecraft:dark_oak_planks",   "minecraft:block/dark_oak_planks", True),
    "mangrove":   ("minecraft:mangrove_planks",   "minecraft:block/mangrove_planks", True),
    "cherry":     ("minecraft:cherry_planks",     "minecraft:block/cherry_planks",   True),
    "bamboo":     ("minecraft:bamboo_planks",     "minecraft:block/bamboo_planks",   True),
    # 原版下界
    "crimson":    ("minecraft:crimson_planks",    "minecraft:block/crimson_planks",  True),
    "warped":     ("minecraft:warped_planks",     "minecraft:block/warped_planks",   True),
    # Botania
    "livingwood": ("botania:livingwood_planks",   "botania:block/livingwood_planks", False),
    "dreamwood":  ("botania:dreamwood_planks",    "botania:block/dreamwood_planks",  False),
}


# ══════════════════════════════════════════════════════════════
#  工具
# ══════════════════════════════════════════════════════════════

def write_json(path: Path, data: dict):
    """写出 JSON 文件，自动创建父目录。"""
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")

def write_tag_file(path: Path, groups: list, include_replace: bool):
    """
    专门用于生成标签文件，支持按组输出（组间带空行）及外部内容保留。
    groups: 二维列表，每个内层列表是一个分组。
    """
    path.parent.mkdir(parents=True, exist_ok=True)
    
    # 读取原有外部条目，防止覆盖其他模组/数据包追加的内容
    existing_extra = []
    if path.exists():
        with open(path, "r", encoding="utf-8") as f:
            try:
                data = json.load(f)
                generated_ids = set()
                for g in groups:
                    for item in g:
                        generated_ids.add(item["id"] if isinstance(item, dict) else item)
                for item in data.get("values", []):
                    item_id = item["id"] if isinstance(item, dict) else item
                    if item_id not in generated_ids:
                        existing_extra.append(item)
            except json.JSONDecodeError:
                pass

    def format_val(val):
        if isinstance(val, dict):
            s = json.dumps(val, ensure_ascii=False, separators=(', ', ': '))
            return "    " + s
        return "    " + json.dumps(val, ensure_ascii=False)

    blocks = []
    for group in groups:
        lines = [format_val(val) for val in group]
        if lines:
            blocks.append(",\n".join(lines))
            
    # 如果存在外部追加的内容，作为最后独立的一组
    if existing_extra:
        lines = [format_val(val) for val in existing_extra]
        blocks.append(",\n".join(lines))
        
    body = ",\n\n".join(blocks)
    
    content = "{\n"
    if include_replace:
        content += '  "replace": false,\n'
    content += '  "values": [\n'
    if body:
        content += body + "\n"
    content += "  ]\n}\n"
    
    path.write_text(content, encoding="utf-8")


# ══════════════════════════════════════════════════════════════
#  1. 战利品表
# ══════════════════════════════════════════════════════════════

def gen_loot_tables(root: Path):
    """每种木材一个战利品表，所有液体状态的锅统一掉落空锅。"""
    lt_dir = root / "data" / MOD_ID / "loot_tables" / "blocks"
    cauldron_types = ["cauldron", "water_cauldron", "lava_cauldron", "powder_snow_cauldron"]
    
    for wood in WOODS:
        for ctype in cauldron_types:
            write_json(lt_dir / f"{wood}_{ctype}.json", {
                "type": "minecraft:block",
                "pools": [{
                    "rolls": 1,
                    "entries": [{
                        "type": "minecraft:item",
                        "name": f"{MOD_ID}:{wood}_cauldron"
                    }],
                    "conditions": [{
                        "condition": "minecraft:survives_explosion"
                    }]
                }]
            })
    print(f"  [loot]     生成 {len(WOODS)} × 4 = {len(WOODS) * 4} 个战利品表")


# ══════════════════════════════════════════════════════════════
#  2. 配方
# ══════════════════════════════════════════════════════════════

def gen_recipes(root: Path):
    """每种木材一个工作台配方"""
    recipe_dir = root / "data" / MOD_ID / "recipes" / "crafting"
    for wood, (planks, _, _) in WOODS.items():
        write_json(recipe_dir / f"{wood}_cauldron.json", {
            "type": "minecraft:crafting_shaped",
            "category": "misc",
            "key": {
                "#": {"item": planks}
            },
            "pattern": [
                "# #",
                "# #",
                "###"
            ],
            "result": {"item": f"{MOD_ID}:{wood}_cauldron"},
            "show_notification": True
        })
    print(f"  [recipe]   生成 {len(WOODS)} 个配方")


# ══════════════════════════════════════════════════════════════
#  3. 资源文件（方块状态 + 模型）
# ══════════════════════════════════════════════════════════════

def gen_resources(root: Path):
    """
    每种木材生成：
      1 个空锅物品模型
      4 个方块状态文件 (空/水/熔岩/细雪)
      8 个方块模型文件 (空/熔岩各1个，水/细雪各3个)
    """
    assets = root / "assets" / MOD_ID
    bs_dir = assets / "blockstates"
    block_dir = assets / "models" / "block"
    item_dir = assets / "models" / "item"

    for wood, (_, texture, _) in WOODS.items():
        # ---- 物品模型 ----
        write_json(item_dir / f"{wood}_cauldron.json", {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"{MOD_ID}:item/{wood}_cauldron"}
        })

        # ---- 方块状态 ----
        write_json(bs_dir / f"{wood}_cauldron.json", {
            "variants": {"": {"model": f"{MOD_ID}:block/{wood}_cauldron"}}
        })
        write_json(bs_dir / f"{wood}_lava_cauldron.json", {
            "variants": {"": {"model": f"{MOD_ID}:block/{wood}_lava_cauldron"}}
        })
        write_json(bs_dir / f"{wood}_water_cauldron.json", {
            "variants": {
                "level=1": {"model": f"{MOD_ID}:block/{wood}_water_cauldron_level1"},
                "level=2": {"model": f"{MOD_ID}:block/{wood}_water_cauldron_level2"},
                "level=3": {"model": f"{MOD_ID}:block/{wood}_water_cauldron_full"}
            }
        })
        write_json(bs_dir / f"{wood}_powder_snow_cauldron.json", {
            "variants": {
                "level=1": {"model": f"{MOD_ID}:block/{wood}_powder_snow_cauldron_level1"},
                "level=2": {"model": f"{MOD_ID}:block/{wood}_powder_snow_cauldron_level2"},
                "level=3": {"model": f"{MOD_ID}:block/{wood}_powder_snow_cauldron_full"}
            }
        })

        # ---- 方块模型 ----
        base_textures = {
            "bottom": texture,
            "inside": texture,
            "particle": texture,
            "side": texture,
            "top": texture
        }

        write_json(block_dir / f"{wood}_cauldron.json", {
            "parent": "minecraft:block/cauldron",
            "textures": base_textures
        })
        write_json(block_dir / f"{wood}_lava_cauldron.json", {
            "parent": "minecraft:block/template_cauldron_full",
            "textures": {**base_textures, "content": "minecraft:block/lava_still"}
        })
        for lvl in ["level1", "level2", "full"]:
            write_json(block_dir / f"{wood}_water_cauldron_{lvl}.json", {
                "parent": f"minecraft:block/template_cauldron_{lvl}",
                "textures": {**base_textures, "content": "minecraft:block/water_still"}
            })
        for lvl in ["level1", "level2", "full"]:
            write_json(block_dir / f"{wood}_powder_snow_cauldron_{lvl}.json", {
                "parent": f"minecraft:block/template_cauldron_{lvl}",
                "textures": {**base_textures, "content": "minecraft:block/powder_snow"}
            })

    print(f"  [resource] 生成 {len(WOODS)} 套资源文件 (物品+4方块状态+8模型)")


# ══════════════════════════════════════════════════════════════
#  4. 标签 Tags
# ══════════════════════════════════════════════════════════════

def gen_tags(root: Path):
    # ---------------- 物品标签 ----------------
    # verdant_sky:wooden_cauldrons
    item_entries = []
    for wood, (_, _, req) in WOODS.items():
        entry = f"{MOD_ID}:{wood}_cauldron" if req else {"id": f"{MOD_ID}:{wood}_cauldron", "required": False}
        item_entries.append(entry)
    item_tag = root / "data" / MOD_ID / "tags" / "items" / "wooden_cauldrons.json"
    write_tag_file(item_tag, [item_entries], include_replace=False)

    # ---------------- 方块标签 ----------------
    # verdant_sky:wooden_cauldrons
    block_groups = []
    for wood, (_, _, req) in WOODS.items():
        group = []
        for ctype in ["cauldron", "water_cauldron", "lava_cauldron", "powder_snow_cauldron"]:
            entry_id = f"{MOD_ID}:{wood}_{ctype}"
            group.append(entry_id if req else {"id": entry_id, "required": False})
        block_groups.append(group)
    block_tag = root / "data" / MOD_ID / "tags" / "blocks" / "wooden_cauldrons.json"
    write_tag_file(block_tag, block_groups, include_replace=False)

    # ---------------- 热源标签 ----------------
    # farmersdelight:heat_sources
    heat_entries = []
    for wood, (_, _, req) in WOODS.items():
        entry = f"{MOD_ID}:{wood}_lava_cauldron" if req else {"id": f"{MOD_ID}:{wood}_lava_cauldron", "required": False}
        heat_entries.append(entry)
    heat_tag = root / "data" / "farmersdelight" / "tags" / "blocks" / "heat_sources.json"
    write_tag_file(heat_tag, [heat_entries], include_replace=True)

    print(f"  [tags]     生成 3 个标签文件")


# ══════════════════════════════════════════════════════════════
#  入口
# ══════════════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(
        description="为 Verdant Sky 的所有木材批量生成炼药锅资源与数据文件。",
    )
    parser.add_argument("--output", default="common/src/main/resources", help="资源根目录")
    parser.add_argument("--mod-id", default=MOD_ID, help=f"模组 ID（默认：{MOD_ID}）")
    args = parser.parse_args()

    root = Path(args.output)
    if not root.is_dir():
        print(f"[!] 目录不存在：{root}")
        return

    print(f"模组    : {args.mod_id}")
    print(f"输出    : {root.resolve()}")
    print(f"木材种类: {len(WOODS)}")
    print("-" * 60)

    gen_loot_tables(root)
    gen_recipes(root)
    gen_resources(root)
    gen_tags(root)

    print("-" * 60)
    total = len(WOODS) * 18 + 3  # 18类数据文件 + 3个标签文件
    print(f"完成。共 {len(WOODS)} 种木材，约 {total} 个 JSON 文件。")


if __name__ == "__main__":
    main()