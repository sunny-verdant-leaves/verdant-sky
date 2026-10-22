#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
装饰方块资源与数据生成器 / Decorative Block Generator
=================================================
为 Verdant Sky 批量生成方块变种（楼梯、台阶、墙、门等）的资源和数据。

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
"""

import argparse
import json
from pathlib import Path

# ══════════════════════════════════════════════════════════════
#  配置区
# ══════════════════════════════════════════════════════════════

MOD_ID = "verdant_sky"

# 基础方块配置
BASE_BLOCK_ID = "snow_bricks"               # 完整方块的注册名
BASE_BLOCK_PREFIX = "snow_brick"            # 其他变种的前缀 (例如: snow_brick_slab)

# 纹理配置
TEXTURE_BASE = "verdant_sky:block/snow_bricks"
TEXTURE_DOOR_TOP = "verdant_sky:block/snow_brick_door_top"
TEXTURE_DOOR_BOTTOM = "verdant_sky:block/snow_brick_door_bottom"
TEXTURE_TRAPDOOR = "verdant_sky:block/snow_brick_trapdoor"

# 类型
# 必需: base
# 可选: slab, stairs, wall, fence, fence_gate, door, trapdoor, pressure_plate, button
ENABLED_TYPES = ["base", "slab", "stairs", "wall"]

# 配方配置
IS_STONE_LIKE = True  # 如果是石质方块，将额外生成切石机配方
RECIPE_MATERIAL = "minecraft:snow_block" # 基础方块的合成材料

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

def get_model_id(block_type: str, suffix: str = "") -> str:
    base = get_block_id(block_type)
    if suffix:
        return f"{MOD_ID}:block/{base}_{suffix}"
    return f"{MOD_ID}:block/{base}"

# ══════════════════════════════════════════════════════════════
#  1. 资源文件生成 (Blockstates)
# ══════════════════════════════════════════════════════════════

def gen_blockstates(root: Path):
    bs_dir = root / "assets" / MOD_ID / "blockstates"

    if "base" in ENABLED_TYPES:
        write_json(bs_dir / f"{BASE_BLOCK_ID}.json", {
            "variants": { "": { "model": get_model_id("base") } }
        })

    if "slab" in ENABLED_TYPES:
        write_json(bs_dir / f"{get_block_id('slab')}.json", {
            "variants": {
                "type=bottom": { "model": get_model_id("slab") },
                "type=top": { "model": get_model_id("slab", "top") },
                "type=double": { "model": get_model_id("base") }
            }
        })

    if "stairs" in ENABLED_TYPES:
        variants = {}
        for facing in ["east", "north", "south", "west"]:
            for half in ["bottom", "top"]:
                for shape in ["straight", "inner_left", "inner_right", "outer_left", "outer_right"]:
                    model_suffix = ""
                    if "inner" in shape: model_suffix = "inner"
                    elif "outer" in shape: model_suffix = "outer"
                    
                    x = 0
                    y = 0
                    uvlock = False
                    
                    if facing == "east":
                        if half == "bottom":
                            if shape == "inner_left": y = 270; uvlock = True
                            elif shape == "inner_right": y = 0
                            elif shape == "outer_left": y = 270; uvlock = True
                            elif shape == "outer_right": y = 0
                        else:
                            x = 180
                            if shape == "inner_left": y = 0; uvlock = True
                            elif shape == "inner_right": y = 90; uvlock = True
                            elif shape == "outer_left": y = 0; uvlock = True
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
                            elif shape == "inner_right": y = 0; uvlock = True
                            elif shape == "outer_left": y = 270; uvlock = True
                            elif shape == "outer_right": y = 0; uvlock = True
                            elif shape == "straight": y = 270; uvlock = True
                    elif facing == "south":
                        if half == "bottom":
                            if shape == "inner_left": y = 0
                            elif shape == "inner_right": y = 90; uvlock = True
                            elif shape == "outer_left": y = 0
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
                    entry = {"model": get_model_id("stairs", model_suffix)}
                    if uvlock: entry["uvlock"] = True
                    if x != 0: entry["x"] = x
                    if y != 0: entry["y"] = y
                    
                    variants[key] = entry
                    
        write_json(bs_dir / f"{get_block_id('stairs')}.json", { "variants": variants })

    if "wall" in ENABLED_TYPES:
        write_json(bs_dir / f"{get_block_id('wall')}.json", {
            "multipart": [
                { "when": { "up": "true" }, "apply": { "model": get_model_id("wall", "post") } },
                { "when": { "north": "low" }, "apply": { "model": get_model_id("wall", "side"), "uvlock": True } },
                { "when": { "east": "low" }, "apply": { "model": get_model_id("wall", "side"), "y": 90, "uvlock": True } },
                { "when": { "south": "low" }, "apply": { "model": get_model_id("wall", "side"), "y": 180, "uvlock": True } },
                { "when": { "west": "low" }, "apply": { "model": get_model_id("wall", "side"), "y": 270, "uvlock": True } },
                { "when": { "north": "tall" }, "apply": { "model": get_model_id("wall", "side_tall"), "uvlock": True } },
                { "when": { "east": "tall" }, "apply": { "model": get_model_id("wall", "side_tall"), "y": 90, "uvlock": True } },
                { "when": { "south": "tall" }, "apply": { "model": get_model_id("wall", "side_tall"), "y": 180, "uvlock": True } },
                { "when": { "west": "tall" }, "apply": { "model": get_model_id("wall", "side_tall"), "y": 270, "uvlock": True } }
            ]
        })

    if "fence" in ENABLED_TYPES:
        write_json(bs_dir / f"{get_block_id('fence')}.json", {
            "multipart": [
                { "apply": { "model": get_model_id("fence", "post") } },
                { "when": { "north": "true" }, "apply": { "model": get_model_id("fence", "side"), "uvlock": True } },
                { "when": { "east": "true" }, "apply": { "model": get_model_id("fence", "side"), "y": 90, "uvlock": True } },
                { "when": { "south": "true" }, "apply": { "model": get_model_id("fence", "side"), "y": 180, "uvlock": True } },
                { "when": { "west": "true" }, "apply": { "model": get_model_id("fence", "side"), "y": 270, "uvlock": True } }
            ]
        })

    if "fence_gate" in ENABLED_TYPES:
        variants = {}
        for facing in ["east", "north", "south", "west"]:
            for in_wall in ["false", "true"]:
                for open in ["false", "true"]:
                    y = {"east": 270, "north": 180, "south": 0, "west": 90}[facing]
                    model_suffix = ""
                    if in_wall == "true": model_suffix = "_wall"
                    if open == "true": model_suffix += "_open"
                    
                    key = f"facing={facing},in_wall={in_wall},open={open}"
                    entry = {"model": get_model_id("fence_gate", model_suffix.lstrip("_")) if model_suffix else get_model_id("fence_gate"), "uvlock": True}
                    if y != 0: entry["y"] = y
                    
                    variants[key] = entry
        write_json(bs_dir / f"{get_block_id('fence_gate')}.json", { "variants": variants })

    if "door" in ENABLED_TYPES:
        variants = {}
        for facing in ["east", "north", "south", "west"]:
            for half in ["lower", "upper"]:
                for hinge in ["left", "right"]:
                    for open in ["false", "true"]:
                        model_name = f"{get_block_id('door')}_{half}_{hinge}"
                        if open == "true": model_name += "_open"
                        
                        y = 0
                        if facing == "east":
                            if open == "true": y = 90 if hinge == "left" else 270
                        elif facing == "north":
                            if open == "false": y = 270
                            else: y = 0 if hinge == "left" else 180
                        elif facing == "south":
                            if open == "false": y = 90
                            else: y = 180 if hinge == "left" else 0
                        elif facing == "west":
                            if open == "false": y = 180
                            else: y = 270 if hinge == "left" else 90
                                
                        key = f"facing={facing},half={half},hinge={hinge},open={open}"
                        entry = {"model": f"{MOD_ID}:block/{model_name}"}
                        if y != 0: entry["y"] = y
                        variants[key] = entry
        write_json(bs_dir / f"{get_block_id('door')}.json", { "variants": variants })

    if "trapdoor" in ENABLED_TYPES:
        variants = {}
        for facing in ["east", "north", "south", "west"]:
            for half in ["bottom", "top"]:
                for open in ["false", "true"]:
                    model_suffix = ""
                    x = 0
                    y = 0
                    
                    if open == "false":
                        model_suffix = half
                    else:
                        model_suffix = "open"
                        if half == "top": x = 180
                            
                    if facing == "east":
                        if open == "false": y = 90
                        else: y = 90 if half == "bottom" else 270
                    elif facing == "south":
                        if open == "false": y = 180
                        else: y = 180 if half == "bottom" else 0
                    elif facing == "west":
                        if open == "false": y = 270
                        else: y = 270 if half == "bottom" else 90
                        
                    key = f"facing={facing},half={half},open={open}"
                    entry = {"model": get_model_id("trapdoor", model_suffix)}
                    if x != 0: entry["x"] = x
                    if y != 0: entry["y"] = y
                    
                    variants[key] = entry
        write_json(bs_dir / f"{get_block_id('trapdoor')}.json", { "variants": variants })

    if "pressure_plate" in ENABLED_TYPES:
        write_json(bs_dir / f"{get_block_id('pressure_plate')}.json", {
            "variants": {
                "powered=false": { "model": get_model_id("pressure_plate") },
                "powered=true": { "model": get_model_id("pressure_plate", "down") }
            }
        })

    if "button" in ENABLED_TYPES:
        variants = {}
        for face in ["floor", "wall", "ceiling"]:
            for facing in ["east", "north", "south", "west"]:
                for powered in ["false", "true"]:
                    model_suffix = "" if powered == "false" else "pressed"
                    x = 0
                    y = 0
                    uvlock = (face == "wall")
                    
                    if face == "ceiling":
                        x = 180
                        if facing == "east": y = 270
                        elif facing == "north": y = 180
                        elif facing == "south": y = 0
                        elif facing == "west": y = 90
                    elif face == "floor":
                        if facing == "east": y = 90
                        elif facing == "north": y = 0
                        elif facing == "south": y = 180
                        elif facing == "west": y = 270
                    elif face == "wall":
                        x = 90
                        if facing == "east": y = 90
                        elif facing == "north": y = 0
                        elif facing == "south": y = 180
                        elif facing == "west": y = 270
                        
                    key = f"face={face},facing={facing},powered={powered}"
                    entry = {"model": get_model_id("button", model_suffix).replace("_pressed", "") + ("_pressed" if powered == "true" else "")}
                    if uvlock: entry["uvlock"] = True
                    if x != 0: entry["x"] = x
                    if y != 0: entry["y"] = y
                    
                    variants[key] = entry
        write_json(bs_dir / f"{get_block_id('button')}.json", { "variants": variants })

    print(f"  [blockstate] 生成完毕")


# ══════════════════════════════════════════════════════════════
#  2. 模型文件生成
# ══════════════════════════════════════════════════════════════

def gen_models(root: Path):
    block_dir = root / "assets" / MOD_ID / "models" / "block"
    item_dir = root / "assets" / MOD_ID / "models" / "item"

    if "base" in ENABLED_TYPES:
        write_json(block_dir / f"{BASE_BLOCK_ID}.json", {
            "parent": "minecraft:block/cube_all",
            "textures": { "all": TEXTURE_BASE }
        })
        write_json(item_dir / f"{BASE_BLOCK_ID}.json", { "parent": get_model_id("base") })

    if "slab" in ENABLED_TYPES:
        write_json(block_dir / f"{get_block_id('slab')}.json", {
            "parent": "minecraft:block/slab",
            "textures": { "bottom": TEXTURE_BASE, "top": TEXTURE_BASE, "side": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('slab')}_top.json", {
            "parent": "minecraft:block/slab_top",
            "textures": { "bottom": TEXTURE_BASE, "top": TEXTURE_BASE, "side": TEXTURE_BASE }
        })
        write_json(item_dir / f"{get_block_id('slab')}.json", { "parent": get_model_id("slab") })

    if "stairs" in ENABLED_TYPES:
        write_json(block_dir / f"{get_block_id('stairs')}.json", {
            "parent": "minecraft:block/stairs",
            "textures": { "bottom": TEXTURE_BASE, "top": TEXTURE_BASE, "side": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('stairs')}_inner.json", {
            "parent": "minecraft:block/inner_stairs",
            "textures": { "bottom": TEXTURE_BASE, "top": TEXTURE_BASE, "side": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('stairs')}_outer.json", {
            "parent": "minecraft:block/outer_stairs",
            "textures": { "bottom": TEXTURE_BASE, "top": TEXTURE_BASE, "side": TEXTURE_BASE }
        })
        write_json(item_dir / f"{get_block_id('stairs')}.json", { "parent": get_model_id("stairs") })

    if "wall" in ENABLED_TYPES:
        write_json(block_dir / f"{get_block_id('wall')}_post.json", {
            "parent": "minecraft:block/template_wall_post",
            "textures": { "wall": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('wall')}_side.json", {
            "parent": "minecraft:block/template_wall_side",
            "textures": { "wall": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('wall')}_side_tall.json", {
            "parent": "minecraft:block/template_wall_side_tall",
            "textures": { "wall": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('wall')}_inventory.json", {
            "parent": "minecraft:block/wall_inventory",
            "textures": { "wall": TEXTURE_BASE }
        })
        write_json(item_dir / f"{get_block_id('wall')}.json", { "parent": get_model_id("wall", "inventory") })

    if "fence" in ENABLED_TYPES:
        write_json(block_dir / f"{get_block_id('fence')}_post.json", {
            "parent": "minecraft:block/fence_post",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('fence')}_side.json", {
            "parent": "minecraft:block/fence_side",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('fence')}_inventory.json", {
            "parent": "minecraft:block/fence_inventory",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(item_dir / f"{get_block_id('fence')}.json", { "parent": get_model_id("fence", "inventory") })

    if "fence_gate" in ENABLED_TYPES:
        write_json(block_dir / f"{get_block_id('fence_gate')}.json", {
            "parent": "minecraft:block/template_fence_gate",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('fence_gate')}_open.json", {
            "parent": "minecraft:block/template_fence_gate_open",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('fence_gate')}_wall.json", {
            "parent": "minecraft:block/template_fence_gate_wall",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('fence_gate')}_wall_open.json", {
            "parent": "minecraft:block/template_fence_gate_wall_open",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(item_dir / f"{get_block_id('fence_gate')}.json", { "parent": get_model_id("fence_gate") })

    if "door" in ENABLED_TYPES:
        for half in ["bottom", "top"]:
            for hinge in ["left", "right"]:
                write_json(block_dir / f"{get_block_id('door')}_{half}_{hinge}.json", {
                    "parent": f"minecraft:block/door_{half}_{hinge}",
                    "textures": { "bottom": TEXTURE_DOOR_BOTTOM, "top": TEXTURE_DOOR_TOP }
                })
                write_json(block_dir / f"{get_block_id('door')}_{half}_{hinge}_open.json", {
                    "parent": f"minecraft:block/door_{half}_{hinge}_open",
                    "textures": { "bottom": TEXTURE_DOOR_BOTTOM, "top": TEXTURE_DOOR_TOP }
                })
        write_json(item_dir / f"{get_block_id('door')}.json", {
            "parent": "minecraft:item/generated",
            "textures": { "layer0": f"{MOD_ID}:item/{get_block_id('door')}" }
        })

    if "trapdoor" in ENABLED_TYPES:
        for suffix in ["bottom", "top", "open"]:
            write_json(block_dir / f"{get_block_id('trapdoor')}_{suffix}.json", {
                "parent": f"minecraft:block/template_orientable_trapdoor_{suffix}",
                "textures": { "texture": TEXTURE_TRAPDOOR }
            })
        write_json(item_dir / f"{get_block_id('trapdoor')}.json", { "parent": get_model_id("trapdoor", "bottom") })

    if "pressure_plate" in ENABLED_TYPES:
        write_json(block_dir / f"{get_block_id('pressure_plate')}.json", {
            "parent": "minecraft:block/pressure_plate_up",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('pressure_plate')}_down.json", {
            "parent": "minecraft:block/pressure_plate_down",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(item_dir / f"{get_block_id('pressure_plate')}.json", { "parent": get_model_id("pressure_plate") })

    if "button" in ENABLED_TYPES:
        write_json(block_dir / f"{get_block_id('button')}.json", {
            "parent": "minecraft:block/button",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('button')}_pressed.json", {
            "parent": "minecraft:block/button_pressed",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(block_dir / f"{get_block_id('button')}_inventory.json", {
            "parent": "minecraft:block/button_inventory",
            "textures": { "texture": TEXTURE_BASE }
        })
        write_json(item_dir / f"{get_block_id('button')}.json", { "parent": get_model_id("button", "inventory") })

    print(f"  [model]      生成完毕")


# ══════════════════════════════════════════════════════════════
#  3. 战利品表生成
# ══════════════════════════════════════════════════════════════

def gen_loot_tables(root: Path):
    lt_dir = root / "data" / MOD_ID / "loot_tables" / "blocks"

    def create_simple_loot(block_id: str):
        return {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{ "type": "minecraft:item", "name": f"{MOD_ID}:{block_id}" }],
                "conditions": [{ "condition": "minecraft:survives_explosion" }]
            }]
        }

    if "base" in ENABLED_TYPES:
        write_json(lt_dir / f"{BASE_BLOCK_ID}.json", create_simple_loot(BASE_BLOCK_ID))

    if "slab" in ENABLED_TYPES:
        write_json(lt_dir / f"{get_block_id('slab')}.json", {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{
                    "type": "minecraft:item",
                    "name": f"{MOD_ID}:{get_block_id('slab')}",
                    "functions": [{
                        "function": "minecraft:set_count",
                        "count": 2,
                        "conditions": [{
                            "condition": "minecraft:block_state_property",
                            "block": f"{MOD_ID}:{get_block_id('slab')}",
                            "properties": { "type": "double" }
                        }]
                    }, {
                        "function": "minecraft:explosion_decay"
                    }]
                }]
            }]
        })

    if "stairs" in ENABLED_TYPES:
        write_json(lt_dir / f"{get_block_id('stairs')}.json", create_simple_loot(get_block_id('stairs')))
    
    if "wall" in ENABLED_TYPES:
        write_json(lt_dir / f"{get_block_id('wall')}.json", create_simple_loot(get_block_id('wall')))

    if "fence" in ENABLED_TYPES:
        write_json(lt_dir / f"{get_block_id('fence')}.json", create_simple_loot(get_block_id('fence')))

    if "fence_gate" in ENABLED_TYPES:
        write_json(lt_dir / f"{get_block_id('fence_gate')}.json", create_simple_loot(get_block_id('fence_gate')))
    
    if "door" in ENABLED_TYPES:
        write_json(lt_dir / f"{get_block_id('door')}.json", create_simple_loot(get_block_id('door')))

    if "trapdoor" in ENABLED_TYPES:
        write_json(lt_dir / f"{get_block_id('trapdoor')}.json", create_simple_loot(get_block_id('trapdoor')))

    if "pressure_plate" in ENABLED_TYPES:
        write_json(lt_dir / f"{get_block_id('pressure_plate')}.json", create_simple_loot(get_block_id('pressure_plate')))

    if "button" in ENABLED_TYPES:
        write_json(lt_dir / f"{get_block_id('button')}.json", create_simple_loot(get_block_id('button')))

    print(f"  [loot]       生成完毕")


# ══════════════════════════════════════════════════════════════
#  4. 配方生成
# ══════════════════════════════════════════════════════════════

def gen_recipes(root: Path):
    craft_dir = root / "data" / MOD_ID / "recipes" / "crafting"
    stone_dir = root / "data" / MOD_ID / "recipes" / "stonecutting"

    def save_craft(recipe_id, data): write_json(craft_dir / f"{recipe_id}.json", data)
    def save_stone(recipe_id, data): 
        if IS_STONE_LIKE: write_json(stone_dir / f"{recipe_id}.json", data)

    if "base" in ENABLED_TYPES:
        save_craft(BASE_BLOCK_ID, {
            "type": "minecraft:crafting_shaped",
            "category": "building",
            "key": { "#": { "item": RECIPE_MATERIAL } },
            "pattern": ["##", "##"],
            "result": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}", "count": 4 }
        })

    if "slab" in ENABLED_TYPES:
        slab_id = get_block_id("slab")
        save_craft(slab_id, {
            "type": "minecraft:crafting_shaped",
            "category": "building",
            "key": { "#": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" } },
            "pattern": ["###"],
            "result": { "item": f"{MOD_ID}:{slab_id}", "count": 6 }
        })
        save_stone(slab_id, {
            "type": "minecraft:stonecutting",
            "ingredient": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" },
            "result": f"{MOD_ID}:{slab_id}",
            "count": 2
        })

    if "stairs" in ENABLED_TYPES:
        stairs_id = get_block_id("stairs")
        save_craft(stairs_id, {
            "type": "minecraft:crafting_shaped",
            "category": "building",
            "key": { "#": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" } },
            "pattern": ["#  ", "## ", "###"],
            "result": { "item": f"{MOD_ID}:{stairs_id}", "count": 4 }
        })
        save_stone(stairs_id, {
            "type": "minecraft:stonecutting",
            "ingredient": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" },
            "result": f"{MOD_ID}:{stairs_id}",
            "count": 1
        })

    if "wall" in ENABLED_TYPES:
        wall_id = get_block_id("wall")
        save_craft(wall_id, {
            "type": "minecraft:crafting_shaped",
            "category": "building",
            "key": { "#": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" } },
            "pattern": ["###", "###"],
            "result": { "item": f"{MOD_ID}:{wall_id}", "count": 6 }
        })
        save_stone(wall_id, {
            "type": "minecraft:stonecutting",
            "ingredient": { "item": f"{MOD_ID}:{BASE_BLOCK_ID}" },
            "result": f"{MOD_ID}:{wall_id}",
            "count": 1
        })

    if "fence" in ENABLED_TYPES:
        fence_id = get_block_id("fence")
        # 栅栏配方：木板 + 木棍
        save_craft(fence_id, {
            "type": "minecraft:crafting_shaped",
            "category": "building",
            "key": {
                "#": { "item": RECIPE_MATERIAL },
                "S": { "item": "minecraft:stick" }
            },
            "pattern": ["#S#", "#S#"],
            "result": { "item": f"{MOD_ID}:{fence_id}", "count": 3 }
        })

    if "fence_gate" in ENABLED_TYPES:
        gate_id = get_block_id("fence_gate")
        save_craft(gate_id, {
            "type": "minecraft:crafting_shaped",
            "category": "redstone",
            "key": {
                "#": { "item": RECIPE_MATERIAL },
                "S": { "item": "minecraft:stick" }
            },
            "pattern": ["S#S", "S#S"],
            "result": { "item": f"{MOD_ID}:{gate_id}" }
        })

    if "door" in ENABLED_TYPES:
        door_id = get_block_id("door")
        save_craft(door_id, {
            "type": "minecraft:crafting_shaped",
            "category": "redstone",
            "key": { "#": { "item": RECIPE_MATERIAL } },
            "pattern": ["##", "##", "##"],
            "result": { "item": f"{MOD_ID}:{door_id}", "count": 3 }
        })

    if "trapdoor" in ENABLED_TYPES:
        trapdoor_id = get_block_id("trapdoor")
        save_craft(trapdoor_id, {
            "type": "minecraft:crafting_shaped",
            "category": "redstone",
            "key": { "#": { "item": RECIPE_MATERIAL } },
            "pattern": ["###", "###"],
            "result": { "item": f"{MOD_ID}:{trapdoor_id}", "count": 2 }
        })

    if "pressure_plate" in ENABLED_TYPES:
        plate_id = get_block_id("pressure_plate")
        save_craft(plate_id, {
            "type": "minecraft:crafting_shaped",
            "category": "redstone",
            "key": { "#": { "item": RECIPE_MATERIAL } },
            "pattern": ["##"],
            "result": { "item": f"{MOD_ID}:{plate_id}" }
        })

    if "button" in ENABLED_TYPES:
        button_id = get_block_id("button")
        save_craft(button_id, {
            "type": "minecraft:crafting_shapeless",
            "category": "redstone",
            "ingredients": [{ "item": RECIPE_MATERIAL }],
            "result": { "item": f"{MOD_ID}:{button_id}" }
        })

    print(f"  [recipe]     生成完毕")


# ══════════════════════════════════════════════════════════════
#  入口
# ══════════════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(description="生成装饰方块变种的资源与数据。")
    parser.add_argument("--output", default="common/src/main/resources", help="资源根目录")
    parser.add_argument("--mod-id", default=MOD_ID, help="模组 ID")
    args = parser.parse_args()

    root = Path(args.output)
    if not root.is_dir():
        print(f"[!] 目录不存在：{root}")
        return

    print(f"模组        : {args.mod_id}")
    print(f"基础方块    : {BASE_BLOCK_ID}")
    print(f"启用变种    : {', '.join(ENABLED_TYPES)}")
    print("-" * 60)

    gen_blockstates(root)
    gen_models(root)
    gen_loot_tables(root)
    gen_recipes(root)

    print("-" * 60)
    print("全部生成完毕！")

if __name__ == "__main__":
    main()