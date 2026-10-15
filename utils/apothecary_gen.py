#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
木花药台资源生成器 / Wooden Petal Apothecary Generator
=================================================
为 Verdant Sky 的每一种木材批量生成花药台的全套资源与数据文件。
注意：不包括本地化和标签

用法: python utils/apothecary_gen.py [--output <目录>] [--mod-id <id>]
      旧写法兼容: 直接运行，使用默认参数

参数:
    --output <目录>   资源根目录。默认 common/src/main/resources
    --mod-id <id>     模组 ID。默认 verdant_sky

生成内容（每种木材）:
    assets/<mod>/blockstates/apothecary_<wood>.json
    assets/<mod>/models/block/apothecary_<wood>.json
    assets/<mod>/models/item/apothecary_<wood>.json
    data/<mod>/loot_tables/blocks/apothecary_<wood>.json
    data/<mod>/recipes/apothecary_<wood>.json

示例:
    python utils/apothecary_gen.py
    python utils/apothecary_gen.py --mod-id verdant_sky --output common/src/main/resources

与炼药锅生成器的差异:
    炼药锅有 level 属性（1/2/3）和四种状态（空/水/熔岩/细雪），每种木材产出 7 个模型；
    花药台只有 water 布尔属性，每种木材只产出一个模型，父级复用 Botania 的花药台形状。
    两者共用同一套木材表和输出目录约定，便于统一维护。
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

# 木材 → (英文名, 中文名, 木板物品ID, 纹理ID)
WOODS = {
    # 原版主世界
    "oak":        ("Oak",        "橡木",      "minecraft:oak_planks",        "minecraft:block/oak_planks"),
    "spruce":     ("Spruce",     "云杉木",    "minecraft:spruce_planks",     "minecraft:block/spruce_planks"),
    "birch":      ("Birch",      "白桦木",    "minecraft:birch_planks",      "minecraft:block/birch_planks"),
    "jungle":     ("Jungle",     "丛林木",    "minecraft:jungle_planks",     "minecraft:block/jungle_planks"),
    "acacia":     ("Acacia",     "金合欢木",  "minecraft:acacia_planks",     "minecraft:block/acacia_planks"),
    "dark_oak":   ("Dark Oak",   "深色橡木",  "minecraft:dark_oak_planks",   "minecraft:block/dark_oak_planks"),
    "mangrove":   ("Mangrove",   "红树木",    "minecraft:mangrove_planks",   "minecraft:block/mangrove_planks"),
    "cherry":     ("Cherry",     "樱花木",    "minecraft:cherry_planks",     "minecraft:block/cherry_planks"),
    "bamboo":     ("Bamboo",     "竹",        "minecraft:bamboo_planks",     "minecraft:block/bamboo_planks"),
    # 原版下界
    "crimson":    ("Crimson",    "绯红木",    "minecraft:crimson_planks",    "minecraft:block/crimson_planks"),
    "warped":     ("Warped",     "诡异木",    "minecraft:warped_planks",     "minecraft:block/warped_planks"),
    # Botania
    "livingwood": ("Livingwood", "活木",      "botania:livingwood_planks",   "botania:block/livingwood_planks"),
    "dreamwood":  ("Dreamwood",  "梦之木",    "botania:dreamwood_planks",    "botania:block/dreamwood_planks"),
    # ── 以下两行按需启用（1.20.1 原版不存在，需回移植模组）──
    # "pale_oak": ("Pale Oak", "苍白橡木", "minecraft:pale_oak_planks", "minecraft:block/pale_oak_planks"),  # 1.21+
    # "poplar":   ("Poplar",   "杨木",     "minecraft:poplar_planks",   "minecraft:block/poplar_planks"),     # 26.3
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


# ══════════════════════════════════════════════════════════════
#  1. 语言文件
# ══════════════════════════════════════════════════════════════

# 由于语言文件需要排版的特殊性，不直接进行追加。而是使用额外的工具整合并重新生成。

def gen_lang(root: Path):
    """
    追加语言条目。已存在的键不动，只往文件末尾插入缺失的键。
    原文件内容保持原样，不做整体重新格式化。
    """
    # lang_dir = root / "assets" / MOD_ID / "lang"
    # en_path = lang_dir / "en_us.json"
    # zh_path = lang_dir / "zh_cn.json"

    # en_added = append_lang_entries(en_path, {
    #     f"block.{MOD_ID}.apothecary_{wood}": f"{wood_en} Petal Apothecary"
    #     for wood, (wood_en, _, _, _) in WOODS.items()
    # })
    # zh_added = append_lang_entries(zh_path, {
    #     f"block.{MOD_ID}.apothecary_{wood}": f"{wood_zh}花药台"
    #     for wood, (_, wood_zh, _, _) in WOODS.items()
    # })

    # print(f"  [lang]     en_us +{en_added} / zh_cn +{zh_added}")
    return


def append_lang_entries(path: Path, entries: dict) -> int:
    """
    把 entries 中尚未存在的键追加到 JSON 文件末尾。
    返回新增数量。已有内容原样保留。
    """
    # if not path.exists():
    #     write_json(path, entries)
    #     return len(entries)

    # text = path.read_text(encoding="utf-8")

    # # 解析已有内容，判断哪些键已存在
    # try:
    #     existing = json.loads(text)
    # except json.JSONDecodeError:
    #     # 文件坏了就直接整体重写
    #     write_json(path, entries)
    #     return len(entries)

    # new_entries = {k: v for k, v in entries.items() if k not in existing}
    # if not new_entries:
    #     return 0

    # # 找到最后一个 } 的位置，在它前面插入新行
    # last_brace = text.rfind("}")
    # if last_brace == -1:
    #     write_json(path, {**existing, **new_entries})
    #     return len(new_entries)

    # before = text[:last_brace].rstrip()
    # after = text[last_brace:]

    # # 判断原文件是否为空对象
    # if before.endswith("{"):
    #     # {} 空对象，直接填
    #     lines = [
    #         f'  {json.dumps(k, ensure_ascii=False)}: '
    #         f'{json.dumps(v, ensure_ascii=False)}'
    #         for k, v in new_entries.items()
    #     ]
    #     new_text = "{\n" + ",\n".join(lines) + "\n}"
    # else:
    #     # 非空，在最后一个已有条目后加逗号再追加
    #     lines = [
    #         f'  {json.dumps(k, ensure_ascii=False)}: '
    #         f'{json.dumps(v, ensure_ascii=False)}'
    #         for k, v in new_entries.items()
    #     ]
    #     new_text = before + ",\n" + ",\n".join(lines) + "\n}" + after[1:]

    # path.write_text(new_text, encoding="utf-8")
    # return len(new_entries)
    return 0

# ══════════════════════════════════════════════════════════════
#  2. 战利品表
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
#  3. 合成配方
# ══════════════════════════════════════════════════════════════
def gen_recipes(root: Path):
    """每种木材一个工作台配方。"""
    recipe_dir = root / "data" / MOD_ID / "recipes/crafting"

    for wood, (_, _, planks, _) in WOODS.items():
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
#  4. 资源文件（方块状态 + 方块模型 + 物品模型）
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

    for wood, (_, _, _, texture) in WOODS.items():
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

    gen_lang(root)
    gen_loot_tables(root)
    gen_recipes(root)
    gen_resources(root)

    print("-" * 60)
    total = len(WOODS) * 4 + 2  # 4 类数据文件 + 2 个 lang
    print(f"完成。共 {len(WOODS)} 种木材，约 {total} 个 JSON 文件。")


if __name__ == "__main__":
    main()