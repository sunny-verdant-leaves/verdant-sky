#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
装饰方块资源与数据生成器 / Decorative Block Generator
=================================================
为 Verdant Sky 批量生成方块变种（楼梯、台阶、墙、门等）的资源和数据。
注意：不包括本地化和标签

生成内容:
    assets/<mod>/blockstates/<name>.json
    assets/<mod>/models/block/<name>.json
    assets/<mod>/models/item/<name>.json
    data/<mod>/loot_tables/blocks/<name>.json
    data/<mod>/recipes/crafting/<name>.json
    data/<mod>/recipes/stonecutting/<name>.json

用法:
    python utils/decorative_block_gen.py [--output <目录>] [--mod-id <id>]

示例:
    python utils/decorative_block_gen.py --output common/src/main/resources
    python utils/decorative_block_gen.py --gen-pack
"""

import argparse
import json
import shutil
import zipfile
from pathlib import Path

# ══════════════════════════════════════════════════════════════
#  配置区
# ══════════════════════════════════════════════════════════════

MOD_ID = "verdant_sky"
BASE_BLOCK_ID = "snow_bricks"               # 完整方块的注册名
BASE_BLOCK_PREFIX = "snow_brick"            # 其他变种的前缀 (例如: snow_brick_slab)

# 模组默认纹理
TEXTURE_BASE = "verdant_sky:block/snow_bricks"
TEXTURE_DOOR_TOP = "verdant_sky:block/snow_brick_door_top"
TEXTURE_DOOR_BOTTOM = "verdant_sky:block/snow_brick_door_bottom"
TEXTURE_TRAPDOOR = "verdant_sky:block/snow_brick_trapdoor"

# 资源包专属配置
VANILLA_TEXTURE_BASE = "minecraft:block/snow_bricks"
SOURCE_TEXTURE_PATH = "utils/assets/minecraft/textures/block/snow_bricks.png"

# 类型
# 必需: base
# 可选: slab, stairs, wall, fence, fence_gate, door, trapdoor, pressure_plate, button
ENABLED_TYPES = ["base", "slab", "stairs", "wall"]
IS_STONE_LIKE = False 
RECIPE_MATERIAL = "minecraft:snow_block" 

# ══════════════════════════════════════════════════════════════
#  工具函数
# ══════════════════════════════════════════════════════════════

def write_json(path: Path, data: dict):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")

def get_block_id(block_type: str) -> str:
    if block_type == "base":
        return BASE_BLOCK_ID
    return f"{BASE_BLOCK_PREFIX}_{block_type}"

# ══════════════════════════════════════════════════════════════
#  变体数据生成器 (Variants Builders) - 核心复用逻辑
# ══════════════════════════════════════════════════════════════

def build_stairs_variants(model_ns: str) -> dict:
    stairs_id = get_block_id("stairs")
    variants = {}
    for facing in ["east", "north", "south", "west"]:
        for half in ["bottom", "top"]:
            for shape in ["straight", "inner_left", "inner_right", "outer_left", "outer_right"]:
                model_suffix = ""
                if "inner" in shape: model_suffix = "_inner"
                elif "outer" in shape: model_suffix = "_outer"
                
                x, y, uvlock = 0, 0, False
                
                if facing == "east":
                    if half == "bottom":
                        if shape == "inner_left": y = 270; uvlock = True
                        elif shape == "outer_left": y = 270; uvlock = True
                    else:
                        x = 180
                        if shape == "inner_left": uvlock = True
                        elif shape == "inner_right": y = 90; uvlock = True
                        elif shape == "outer_left": uvlock = True
                        elif shape == "outer_right": y = 90; uvlock = True
                        elif shape == "straight": uvlock = True
                elif facing == "north":
                    if half == "bottom":
                        if shape == "inner_left": y = 180; uvlock = True
                        elif shape == "inner_right": y = 270; uvlock = True
                        elif shape == "outer_left": y = 180; uvlock = True
                        elif shape == "outer_right": y = 270; uvlock = True
                        elif shape == "straight": y = 270; uvlock = True
                    else:
                        x = 180
                        if shape == "inner_left": y = 270; uvlock = True
                        elif shape == "outer_left": y = 270; uvlock = True
                        elif shape == "straight": y = 270; uvlock = True
                elif facing == "south":
                    if half == "bottom":
                        if shape == "inner_right": y = 90; uvlock = True
                        elif shape == "outer_right": y = 90; uvlock = True
                        elif shape == "straight": y = 90; uvlock = True
                    else:
                        x = 180
                        if shape == "inner_left": y = 90; uvlock = True
                        elif shape == "inner_right": y = 180; uvlock = True
                        elif shape == "outer_left": y = 90; uvlock = True
                        elif shape == "outer_right": y = 180; uvlock = True
                        elif shape == "straight": y = 90; uvlock = True
                elif facing == "west":
                    if half == "bottom":
                        if shape == "inner_left": y = 90; uvlock = True
                        elif shape == "inner_right": y = 180; uvlock = True
                        elif shape == "outer_left": y = 90; uvlock = True
                        elif shape == "outer_right": y = 180; uvlock = True
                        elif shape == "straight": y = 180; uvlock = True
                    else:
                        x = 180
                        if shape == "inner_left": y = 180; uvlock = True
                        elif shape == "inner_right": y = 270; uvlock = True
                        elif shape == "outer_left": y = 180; uvlock = True
                        elif shape == "outer_right": y = 270; uvlock = True
                        elif shape == "straight": y = 180; uvlock = True

                key = f"facing={facing},half={half},shape={shape}"
                entry = {"model": f"{model_ns}:block/{stairs_id}{model_suffix}"}
                if uvlock: entry["uvlock"] = True
                if x != 0: entry["x"] = x
                if y != 0: entry["y"] = y
                variants[key] = entry
    return variants

def build_wall_multipart(model_ns: str) -> dict:
    wall_id = get_block_id("wall")
    return {
        "multipart": [
            { "when": { "up": "true" }, "apply": { "model": f"{model_ns}:block/{wall_id}_post" } },
            { "when": { "north": "low" }, "apply": { "model": f"{model_ns}:block/{wall_id}_side", "uvlock": True } },
            { "when": { "east": "low" }, "apply": { "model": f"{model_ns}:block/{wall_id}_side", "y": 90, "uvlock": True } },
            { "when": { "south": "low" }, "apply": { "model": f"{model_ns}:block/{wall_id}_side", "y": 180, "uvlock": True } },
            { "when": { "west": "low" }, "apply": { "model": f"{model_ns}:block/{wall_id}_side", "y": 270, "uvlock": True } },
            { "when": { "north": "tall" }, "apply": { "model": f"{model_ns}:block/{wall_id}_side_tall", "uvlock": True } },
            { "when": { "east": "tall" }, "apply": { "model": f"{model_ns}:block/{wall_id}_side_tall", "y": 90, "uvlock": True } },
            { "when": { "south": "tall" }, "apply": { "model": f"{model_ns}:block/{wall_id}_side_tall", "y": 180, "uvlock": True } },
            { "when": { "west": "tall" }, "apply": { "model": f"{model_ns}:block/{wall_id}_side_tall", "y": 270, "uvlock": True } }
        ]
    }

# ══════════════════════════════════════════════════════════════
#  资源文件生成 (Blockstates & Models)
# ══════════════════════════════════════════════════════════════

def generate_blockstates(bs_ns: str, model_ns: str, root: Path):
    """生成 Blockstates。bs_ns 决定放在哪个文件夹，model_ns 决定指向谁的模型"""
    bs_dir = root / "assets" / bs_ns / "blockstates"

    if "base" in ENABLED_TYPES:
        write_json(bs_dir / f"{BASE_BLOCK_ID}.json", {
            "variants": { "": { "model": f"{model_ns}:block/{BASE_BLOCK_ID}" } }
        })

    if "slab" in ENABLED_TYPES:
        slab_id = get_block_id('slab')
        write_json(bs_dir / f"{slab_id}.json", {
            "variants": {
                "type=bottom": { "model": f"{model_ns}:block/{slab_id}" },
                "type=top": { "model": f"{model_ns}:block/{slab_id}_top" },
                "type=double": { "model": f"{model_ns}:block/{BASE_BLOCK_ID}" }
            }
        })

    if "stairs" in ENABLED_TYPES:
        write_json(bs_dir / f"{get_block_id('stairs')}.json", { "variants": build_stairs_variants(model_ns) })

    if "wall" in ENABLED_TYPES:
        write_json(bs_dir / f"{get_block_id('wall')}.json", build_wall_multipart(model_ns))

def generate_models(model_ns: str, texture: str, root: Path, include_items: bool = True):
    """生成模型文件。model_ns 决定放在哪个文件夹，texture 决定指向谁的贴图"""
    block_dir = root / "assets" / model_ns / "models" / "block"
    item_dir = root / "assets" / model_ns / "models" / "item"

    if "base" in ENABLED_TYPES:
        write_json(block_dir / f"{BASE_BLOCK_ID}.json", {
            "parent": "minecraft:block/cube_all",
            "textures": { "all": texture }
        })
        if include_items:
            write_json(item_dir / f"{BASE_BLOCK_ID}.json", { "parent": f"{model_ns}:block/{BASE_BLOCK_ID}" })

    if "slab" in ENABLED_TYPES:
        slab_id = get_block_id('slab')
        write_json(block_dir / f"{slab_id}.json", {
            "parent": "minecraft:block/slab",
            "textures": { "bottom": texture, "top": texture, "side": texture }
        })
        write_json(block_dir / f"{slab_id}_top.json", {
            "parent": "minecraft:block/slab_top",
            "textures": { "bottom": texture, "top": texture, "side": texture }
        })
        if include_items:
            write_json(item_dir / f"{slab_id}.json", { "parent": f"{model_ns}:block/{slab_id}" })

    if "stairs" in ENABLED_TYPES:
        stairs_id = get_block_id('stairs')
        write_json(block_dir / f"{stairs_id}.json", {
            "parent": "minecraft:block/stairs",
            "textures": { "bottom": texture, "top": texture, "side": texture }
        })
        write_json(block_dir / f"{stairs_id}_inner.json", {
            "parent": "minecraft:block/inner_stairs",
            "textures": { "bottom": texture, "top": texture, "side": texture }
        })
        write_json(block_dir / f"{stairs_id}_outer.json", {
            "parent": "minecraft:block/outer_stairs",
            "textures": { "bottom": texture, "top": texture, "side": texture }
        })
        if include_items:
            write_json(item_dir / f"{stairs_id}.json", { "parent": f"{model_ns}:block/{stairs_id}" })

    if "wall" in ENABLED_TYPES:
        wall_id = get_block_id('wall')
        write_json(block_dir / f"{wall_id}_post.json", {
            "parent": "minecraft:block/template_wall_post",
            "textures": { "wall": texture }
        })
        write_json(block_dir / f"{wall_id}_side.json", {
            "parent": "minecraft:block/template_wall_side",
            "textures": { "wall": texture }
        })
        write_json(block_dir / f"{wall_id}_side_tall.json", {
            "parent": "minecraft:block/template_wall_side_tall",
            "textures": { "wall": texture }
        })
        if include_items:
            write_json(block_dir / f"{wall_id}_inventory.json", {
                "parent": "minecraft:block/wall_inventory",
                "textures": { "wall": texture }
            })
            write_json(item_dir / f"{wall_id}.json", { "parent": f"{model_ns}:block/{wall_id}_inventory" })


# ══════════════════════════════════════════════════════════════
#  数据文件生成 (Loot Tables & Recipes)
# ══════════════════════════════════════════════════════════════

def generate_data(root: Path):
    lt_dir = root / "data" / MOD_ID / "loot_tables" / "blocks"
    craft_dir = root / "data" / MOD_ID / "recipes" / "crafting"
    stone_dir = root / "data" / MOD_ID / "recipes" / "stonecutting"

    def simple_loot(block_id: str):
        return {
            "type": "minecraft:block",
            "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{MOD_ID}:{block_id}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]
        }

    def save_craft(r_id, data): write_json(craft_dir / f"{r_id}.json", data)
    def save_stone(r_id, data): 
        if IS_STONE_LIKE: write_json(stone_dir / f"{r_id}.json", data)

    if "base" in ENABLED_TYPES:
        write_json(lt_dir / f"{BASE_BLOCK_ID}.json", simple_loot(BASE_BLOCK_ID))
        save_craft(BASE_BLOCK_ID, {
            "type": "minecraft:crafting_shaped", "category": "building",
            "key": { "#": { "item": RECIPE_MATERIAL } }, "pattern": ["##", "##"],
            "result": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}", "count": 4 }
        })

    if "slab" in ENABLED_TYPES:
        slab_id = get_block_id('slab')
        write_json(lt_dir / f"{slab_id}.json", {
            "type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{
                "type": "minecraft:item", "name": f"{MOD_ID}:{slab_id}",
                "functions": [{"function": "minecraft:set_count", "count": 2, "conditions": [{"condition": "minecraft:block_state_property", "block": f"{MOD_ID}:{slab_id}", "properties": {"type": "double"}}]}, {"function": "minecraft:explosion_decay"}]
            }]}]
        })
        save_craft(slab_id, {
            "type": "minecraft:crafting_shaped", "category": "building",
            "key": { "#": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" } }, "pattern": ["###"],
            "result": { "item": f"{MOD_ID}:{slab_id}", "count": 6 }
        })
        save_stone(slab_id, {
            "type": "minecraft:stonecutting", "ingredient": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" },
            "result": f"{MOD_ID}:{slab_id}", "count": 2
        })

    if "stairs" in ENABLED_TYPES:
        stairs_id = get_block_id('stairs')
        write_json(lt_dir / f"{stairs_id}.json", simple_loot(stairs_id))
        save_craft(stairs_id, {
            "type": "minecraft:crafting_shaped", "category": "building",
            "key": { "#": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" } }, "pattern": ["#  ", "## ", "###"],
            "result": { "item": f"{MOD_ID}:{stairs_id}", "count": 4 }
        })
        save_stone(stairs_id, {
            "type": "minecraft:stonecutting", "ingredient": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" },
            "result": f"{MOD_ID}:{stairs_id}", "count": 1
        })

    if "wall" in ENABLED_TYPES:
        wall_id = get_block_id('wall')
        write_json(lt_dir / f"{wall_id}.json", simple_loot(wall_id))
        save_craft(wall_id, {
            "type": "minecraft:crafting_shaped", "category": "building",
            "key": { "#": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" } }, "pattern": ["###", "###"],
            "result": { "item": f"{MOD_ID}:{wall_id}", "count": 6 }
        })
        save_stone(wall_id, {
            "type": "minecraft:stonecutting", "ingredient": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" },
            "result": f"{MOD_ID}:{wall_id}", "count": 1
        })

    print(f"  [data]       生成完毕")

# ══════════════════════════════════════════════════════════════
#  资源包与主模组入口
# ══════════════════════════════════════════════════════════════

def gen_optional_pack(root: Path, pack_name: str):
    """生成可选内置资源包，并自动打包为 zip 供分发。"""
    pack_dir = root / "resourcepacks" / pack_name
    
    # 1. pack.mcmeta
    write_json(pack_dir / "pack.mcmeta", {
        "pack": {
            "pack_format": 15,
            "description": {
                "text": "", "extra": [
                    {"translate": "resourcepack.descriptionTranslation.verdant_sky.vanilla_blocks.1", "fallback": "Makes Snow Brick textures match newer vanilla versions.", "color": "#ffffbc", "italic": False},
                    {"text": "\n"},
                    {"translate": "resourcepack.descriptionTranslation.verdant_sky.vanilla_blocks.2", "fallback": "such as Snow Bricks.", "color": "#ffffbc", "italic": False}
                ]
            }
        }
    })

    # 2. 生成 Blockstates (覆盖到 mod_id 命名空间，指向 minecraft 模型)
    generate_blockstates(bs_ns=MOD_ID, model_ns="minecraft", root=pack_dir)
    
    # 3. 生成 Models (存放在 minecraft 命名空间，使用原版纹理)
    generate_models(model_ns="minecraft", texture=VANILLA_TEXTURE_BASE, root=pack_dir, include_items=False)

    # 4. 复制原版纹理
    src_tex = Path(SOURCE_TEXTURE_PATH)
    if src_tex.exists():
        dst_tex = pack_dir / "assets" / "minecraft" / "textures" / "block" / f"{BASE_BLOCK_ID}.png"
        dst_tex.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src_tex, dst_tex)
        print(f"  [pack] 成功复制原版纹理: {dst_tex.name}")
    else:
        print(f"  [!] 警告: 找不到源纹理文件 {src_tex}")

    # 5. 自动打包为 zip
    zip_path = root / "resourcepacks" / f"{pack_name}.zip"
    zip_path.parent.mkdir(parents=True, exist_ok=True)
    
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for file_path in pack_dir.rglob('*'):
            if file_path.is_file():
                arcname = file_path.relative_to(pack_dir)
                zipf.write(file_path, arcname)

    print(f"  [pack] 资源包生成完毕！")
    print(f"  [pack] 文件夹位置: {pack_dir.resolve()}")
    print(f"  [pack] Zip打包位置: {zip_path.resolve()}")

# ══════════════════════════════════════════════════════════════
#  入口
# ══════════════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(description="生成装饰方块变种的资源与数据。")
    parser.add_argument("--output", default="common/src/main/resources", help="资源根目录")
    parser.add_argument("--gen-pack", action="store_true", help="仅生成可选内置资源包")
    parser.add_argument("--pack-name", default="vanilla_blocks", help="资源包文件夹名")
    args = parser.parse_args()

    root = Path(args.output)
    if not root.is_dir():
        print(f"[!] 目录不存在：{root}")
        return

    if args.gen_pack:
        print(f"资源包名称  : {args.pack_name}")
        print("-" * 60)
        gen_optional_pack(root, args.pack_name)
        print("-" * 60)
    else:
        print(f"模组        : {MOD_ID}")
        print(f"基础方块    : {BASE_BLOCK_ID}")
        print("-" * 60)
        
        # 生成主模组资源 (放在 verdant_sky 命名空间)
        generate_blockstates(bs_ns=MOD_ID, model_ns=MOD_ID, root=root)
        generate_models(model_ns=MOD_ID, texture=TEXTURE_BASE, root=root, include_items=True)
        generate_data(root)
        
        print("-" * 60)
        print("全部生成完毕！")

if __name__ == "__main__":
    main()