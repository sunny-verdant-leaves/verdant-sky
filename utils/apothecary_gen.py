#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
木花药台资源生成器 / Wooden Petal Apothecary Generator
=================================================
为 Verdant Sky 的每一种木材批量生成花药台的全套资源与数据文件。
包括：方块状态、模型（方块+物品）、战利品表、配方、部分标签（方块+物品）
不包括：纹理、本地化、帕秋莉手册、方块标签minecraft:mineable/axe(会破坏已有格式)

用法: python utils/apothecary_gen.py [--output <目录>] [--mod-id <id>]

参数:
    --output <目录>   资源根目录。默认 common/src/main/resources
    --mod-id <id>     模组 ID。默认 verdant_sky

示例:
    python utils/apothecary_gen.py
    python utils/apothecary_gen.py --mod-id verdant_sky --output common/src/main/resources
"""

import argparse
import json
from pathlib import Path

# ══════════════════════════════════════════════════════════════
#  配置区
# ══════════════════════════════════════════════════════════════

MOD_ID = "verdant_sky"

# Botania 花药台的形状模型父级
APOTHECARY_SHAPE = "botania:block/shapes/petal_apothecary"

# 木材 → (英文名, 中文名, 木板物品ID, 纹理ID, 是否必需)
WOODS = {
    # 原版主世界
    "oak":        ("Oak",        "橡木",      "minecraft:oak_planks",        "minecraft:block/oak_planks",      True),
    "spruce":     ("Spruce",     "云杉木",    "minecraft:spruce_planks",     "minecraft:block/spruce_planks",   True),
    "birch":      ("Birch",      "白桦木",    "minecraft:birch_planks",      "minecraft:block/birch_planks",    True),
    "jungle":     ("Jungle",     "丛林木",    "minecraft:jungle_planks",     "minecraft:block/jungle_planks",   True),
    "acacia":     ("Acacia",     "金合欢木",  "minecraft:acacia_planks",     "minecraft:block/acacia_planks",   True),
    "dark_oak":   ("Dark Oak",   "深色橡木",  "minecraft:dark_oak_planks",   "minecraft:block/dark_oak_planks", True),
    "mangrove":   ("Mangrove",   "红树木",    "minecraft:mangrove_planks",   "minecraft:block/mangrove_planks", True),
    "cherry":     ("Cherry",     "樱花木",    "minecraft:cherry_planks",     "minecraft:block/cherry_planks",   True),
    "bamboo":     ("Bamboo",     "竹",        "minecraft:bamboo_planks",     "minecraft:block/bamboo_planks",   True),
    # 原版下界
    "crimson":    ("Crimson",    "绯红木",    "minecraft:crimson_planks",    "minecraft:block/crimson_planks",  True),
    "warped":     ("Warped",     "诡异木",    "minecraft:warped_planks",     "minecraft:block/warped_planks",   True),
    # Botania
    "livingwood": ("Livingwood", "活木",      "botania:livingwood_planks",   "botania:block/livingwood_planks", False),
    "dreamwood":  ("Dreamwood",  "梦之木",    "botania:dreamwood_planks",    "botania:block/dreamwood_planks",  False),
    # ── 以下两行按需启用（1.20.1 原版不存在，需回移植模组）──
    # "pale_oak": ("Pale Oak", "苍白橡木", "minecraft:pale_oak_planks", "minecraft:block/pale_oak_planks", False),  # 1.21+
    # "poplar":   ("Poplar",   "杨木",     "minecraft:poplar_planks",   "minecraft:block/poplar_planks", False),     # 26.3
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


def load_json(path: Path) -> dict:
    """读取 JSON 文件，不存在则返回空 dict。"""
    if path.exists():
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    return {}


def load_and_append_json(path: Path, new_values: list):
    """
    向已有的数组 JSON (如 tag) 中追加元素。
    支持两种条目形式：字符串 ID，或带 required 的可选对象 {"id": ..., "required": false}。
    按条目 ID 去重，已存在的 ID 不会重复写入。
    """
    path.parent.mkdir(parents=True, exist_ok=True)
    data = {"replace": False, "values": []}
    if path.exists():
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)

    if "values" not in data:
        data["values"] = []

    def entry_id(val):
        return val["id"] if isinstance(val, dict) else val

    existing_ids = {entry_id(v) for v in data["values"]}
    for val in new_values:
        if entry_id(val) not in existing_ids:
            data["values"].append(val)
            existing_ids.add(entry_id(val))

    # 每个 values 条目单行输出
    path.parent.mkdir(parents=True, exist_ok=True)
    body = ",\n".join(
        "    " + json.dumps(v, ensure_ascii=False) for v in data["values"]
    )
    path.write_text(
        "{\n"
        '  "replace": false,\n'
        '  "values": [\n'
        f"{body}\n"
        "  ]\n"
        "}\n",
        encoding="utf-8",
    )


# ══════════════════════════════════════════════════════════════
#  1. 战利品表
# ══════════════════════════════════════════════════════════════

def gen_loot_tables(root: Path):
    """每种木材一个战利品表，挖掉掉自己，爆炸保护。"""
    lt_dir = root / "data" / MOD_ID / "loot_tables" / "blocks"

    for wood in WOODS:
        write_json(lt_dir / f"apothecary_{wood}.json", {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{
                    "type": "minecraft:item",
                    "name": f"{MOD_ID}:apothecary_{wood}"
                }],
                "conditions": [{
                    "condition": "minecraft:survives_explosion"
                }]
            }]
        })
    print(f"  [loot]     生成 {len(WOODS)} 个战利品表")


# ══════════════════════════════════════════════════════════════
#  2. 合成配方
# ══════════════════════════════════════════════════════════════
def gen_recipes(root: Path):
    """每种木材一个工作台配方。"""
    recipe_dir = root / "data" / MOD_ID / "recipes/crafting"

    for wood, (_, _, planks, _, _) in WOODS.items():
        write_json(recipe_dir / f"apothecary_{wood}.json", {
            "type": "minecraft:crafting_shaped",
            "category": "misc",
            "key": {
                "C": {"item": planks},
                "P": {"tag": "botania:petals"}
            },
            "pattern": [
                "CPC",
                " C ",
                "CCC"
            ],
            "result": {"item": f"{MOD_ID}:apothecary_{wood}"},
            "show_notification": True
        })
    print(f"  [recipe]   生成 {len(WOODS)} 个配方")


# ══════════════════════════════════════════════════════════════
#  3. 资源文件（方块状态 + 方块模型 + 物品模型）
# ══════════════════════════════════════════════════════════════

def gen_resources(root: Path):
    """
    每种木材生成：
      blockstates/apothecary_<wood>.json      —— 方块状态
      models/block/apothecary_<wood>.json     —— 方块模型: 复用 Botania 形状，只换纹理
      models/item/apothecary_<wood>.json      —— 物品模型: 继承方块模型
    """
    assets = root / "assets" / MOD_ID
    bs_dir = assets / "blockstates"
    block_dir = assets / "models" / "block"
    item_dir = assets / "models" / "item"

    for wood, (_, _, _, texture, _) in WOODS.items():
        model_id = f"{MOD_ID}:block/apothecary_{wood}"

        write_json(bs_dir / f"apothecary_{wood}.json", {
            "variants": {
                "": { "model": f"verdant_sky:block/apothecary_{wood}" }
            }
        })

        # 方块模型：只暴露 bottom / side / top 三个纹理槽，都指向对应木板
        write_json(block_dir / f"apothecary_{wood}.json", {
            "parent": APOTHECARY_SHAPE,
            "textures": {
                "bottom": f"verdant_sky:block/apothecary_{wood}_bottom",
                "side":   f"verdant_sky:block/apothecary_{wood}_side",
                "top":    f"verdant_sky:block/apothecary_{wood}_top"
            }
        })

        # 物品模型：直接继承方块模型
        write_json(item_dir / f"apothecary_{wood}.json", {
            "parent": model_id
        })

    print(f"  [resource] 生成 {len(WOODS)} × 3 = {len(WOODS) * 3} 个资源文件")


# ══════════════════════════════════════════════════════════════
#  4. 标签
# ══════════════════════════════════════════════════════════════

def gen_tags(root: Path):
    """
    生成木花药台标签：
      data/<mod>/tags/blocks/apothecary_wooden.json  —— 方块标签
      data/<mod>/tags/items/apothecary_wooden.json   —— 物品标签
    """
    entries = [
        f"{MOD_ID}:apothecary_{wood}" if req else {"id": f"{MOD_ID}:apothecary_{wood}", "required": False}
        for wood, (_, _, _, _, req) in WOODS.items()
    ]

    block_tag = root / "data" / MOD_ID / "tags" / "blocks" / "apothecary_wooden.json"
    item_tag = root / "data" / MOD_ID / "tags" / "items" / "apothecary_wooden.json"
    load_and_append_json(block_tag, entries)
    load_and_append_json(item_tag, entries)

    print(f"  [tags]     生成 2 个标签文件")


# ══════════════════════════════════════════════════════════════
#  入口
# ══════════════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(
        description="为 Verdant Sky 的所有木材批量生成花药台资源与数据文件。",
    )
    parser.add_argument(
        "--output", default="common/src/main/resources",
        help="资源根目录（默认：common/src/main/resources）",
    )
    parser.add_argument(
        "--mod-id", default=MOD_ID,
        help=f"模组 ID（默认：{MOD_ID}）",
    )
    args = parser.parse_args()

    root = Path(args.output)
    if not root.is_dir():
        print(f"[!] 目录不存在：{root}")
        print("    请确认在项目根目录运行，或手动指定 --output。")
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
    total = len(WOODS) * 5 + 2  # 5 类数据文件 + 2 个标签文件
    print(f"完成。共 {len(WOODS)} 种木材，约 {total} 个 JSON 文件。")


if __name__ == "__main__":
    main()