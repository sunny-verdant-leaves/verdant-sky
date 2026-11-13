English | [中文](../zh/changelog.md)

# Verdant Sky — Changelog

# v2.1.4 - 2026-11-05

## What's Changed

_for player:_
- Add: Generating Flower **Drinking Snow Lotus** and its floating variant, **Floating Drinking Snow Lotus**. They consume Snow, Snow Blocks, and Snowballs to produce Mana.
- Add: Potted Decay Agapanthus and Potted Drinking Snow Lotus.
- Add: Built-in resource pack that makes the modded Snow Bricks look like their newer vanilla counterparts.
- Adjust: Modded Snow Bricks now use a Shovel instead of a Pickaxe as the correct harvesting tool, to match their newer vanilla counterparts. 
- Adjust: Modded Snow Bricks can no longer be crafted from Snow Blocks using a Stonecutter, to match the vanilla recipe in newer versions. 
- Adjust: Start Island now provides a fallback stone platform if generation fails, preventing players from falling into the void at the start.
- Remove: Features placed on the Start Island in specific environments. They are non-essential and were never functioning from the beginning.
- Fix: Typo in the Forest Start Island ID causing generation failure in the Void world preset.
- Fix: Mismatch between the allowed minimum values of some config options and their intended semantics:
  - `decayAgapanthusRadius` now requires a minimum value of 1 (previously could be set to 0).
  - Setting `decayAgapanthusMaxConversionsPerTick` to 0 now means no conversions will occur (previously it would convert at least 1).
  - Setting `decayAgapanthusManaCost` to 0 or a negative value now means no Mana is consumed (previously it would consume at least 1 Mana).
  - Setting `decayAgapanthusWitherDuration` to 0 now means the Wither effect is not applied (previously it would apply for at least 1 tick).
- Fix: Decay Agapanthus forcing multiple block conversions even when it lacked the Mana to pay for all of them, causing Mana overdraw.
- Fix: Floating Special Flower items failing to render on Fabric.

_for developer:_
- Add: `PlatformBlocks` class to register `SpecialFlower` via cross-platform factory methods.
- Add: `utils/flower_gen.py` for bulk generating `SpecialFlower` assets and data.

**Details**: [v2.1.3...v2.1.4](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.1.3...v2.1.4)

# v2.1.3 - 2026-10-19

## What's Changed

_for player:_
- Add: **Snow Bricks** family, including base block, slab, stairs, and wall variants.
- Fix: Livingwood Mana Pool could not interact with certain mana-system blocks on Fabric.

_for developer:_
- Add: `utils/decorative_block_gen.py` for batch-generating resource and data files for decorative block variants.
- Adjust: Downgraded the following dependencies:
  - Architectury API: 9.2.14  → 9.1.10
  - Fabric Loader:    0.19.5  → 0.15.6
  - Forge:            47.4.22 → 47.1.3

**Details**: [v2.1.2...v2.1.3](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.1.2...v2.1.3)

# v2.1.2 - 2026-10-19

## What's Changed 

⚠️ **Breaking change**: The legacy `apothecary_wooden` block has been removed and its block ID deleted. It is superseded by the new wooden Petal Apothecary variants, but existing `apothecary_wooden` blocks are not automatically migrated. Placed `apothecary_wooden` blocks will be lost when a world is upgraded to v2.1.2.

_for player:_
- Add: More wooden variants of the Petal Apothecary.
- Remove: Legacy generic `apothecary_wooden` block, superseded by the new wooden variants.

_for developer:_
- Add: `utils/apothecary_gen.py` for batch-generating resource and data files for Petal Apothecary variants.
- Remove: `utils/spawn_estimator.py`, which was used to estimate the player's spawn biome, superseded by `utils/biome_distribution.py`.

**Details**: [v2.1.1...v2.1.2](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.1.1...v2.1.2)

# v2.1.1 - 2026-10-14

## What's Changed 

_for player:_
- Add: Cauldron tweak configuration. Allows bowls to scoop water from both Wooden Cauldrons and vanilla cauldrons.
- Adjust: Patchouli page for Decay Agapanthus.
- Fix: Wooden Cauldrons could not be filled with fluids by Pointed Dripstone.
- Fix: Fluid-filled Wooden Cauldrons not mapping to items correctly.

_for developer:_
- Refactored Wooden Cauldron: 
  - CauldronBlock (original) → WaterCauldronBlock (now)
  - EmptyCauldronBlock (original) → CauldronBlock (now)
  - WoodenVariantRegistry (removed)
  - VerdantCauldronFamily (adjust member): isSpecial() → fireResistant()

**Details**: [v2.1.0...v2.1.1](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.1.0...v2.1.1)

# v2.1.0 - 2026-10-12

## What's Changed

- Add: Location predicates support for Decay recipes to check conditions at the flower's position.
- Add: **JEI** support for viewing Decay recipes.
- Add: **EMI** support for viewing Decay recipes.
- Add: **Patchouli** page for Decay Agapanthus.
- Add: Decay recipes that decay Pure Daisy outputs into Pure Daisy inputs or other blocks.
- Add: Nether's Delight integration recipes.
- Adjust: Behavior of Decay Agapanthus: 
  - The Decay Agapanthus now only applies the Wither effect to nearby mobs when it successfully decays a block. This means it is no longer a cheap, superior alternative to the Wither Rose.
  - The Decay Agapanthus now only applies the Wither effect when the configured Wither duration is a positive value.
- Adjust: Reduced the duration of the Wither effect applied by the Decay Agapanthus.
- Adjust: Reduced the Decay Agapanthus's maximum mana buffer.
- Adjust: Reduced the mana cost per block decayed.
- Remove: The recipe that decays Dirt into Rooted Dirt.

**Details**: [v2.0.11...v2.1.0](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.11...v2.1.0)

# v2.0.11 - 2026-10-08

## What's Changed

_compared with v2.0.10:_
- Fix: Incorrect loot table of vine and tall seagrass.
- Fix: **Floating Island** world type incorrectly generating oceans.

**Details**: [v2.0.10...v2.0.11](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.10...v2.0.11)

## What's in this mod?

v2.0.11 is the first release of verdant sky on GitHub. Please note that earlier versions are already available. Here is an overview for those who haven't heard of it before.

_Total content since v1.0.0:_
- Add: Two types of world preset.
- Add: Wooden Cauldron.
- Add: Wooden Petal Apothecary.
- Add: Decay Agapanthus and Floating Decay Agapanthus.
- Add: Some modifications (disabled by default).

**Details**: [v2.0.11](https://github.com/sunny-verdant-leaves/verdant-sky/tree/v2.0.11)

# v2.0.10 - 2026-09-30

## What's Changed

- Fix: Initialization error when registering block entities.
- Fix: Missing floating flower model.
- Fix: Incorrect decay recipe on Fabric.

**Details**: [v2.0.9...v2.0.10](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.9...v2.0.10)

# v2.0.9 - 2026-09-28

## What's Changed

- Add: Vine support for the plant drop modification.
- Add: **Nether Start Island**.
- Add: Features placed on the Start Island in specific biomes. 
- Adjust: Rewrote the biomes of the world preset. World generation is now more evenly distributed.

**Details**: [v2.0.8...v2.0.9](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.8...v2.0.9)

# v2.0.8 - 2026-09-25

## What's Changed

_for player:_
- Add: Nether void noise setting. The Nether now also generates as a void in the Void world type.
- Adjust: Biome settings.

_for developer:_
- Add: `utils/biome_distribution.py` for estimating biome distribution across the whole world, not limited to the spawn area.

**Details**: [v2.0.7...v2.0.8](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.7...v2.0.8)

# v2.0.7 - 2026-09-24

## What's Changed

- Add: Plant drop modification. Plants that normally require Silk Touch or Shears now have a chance to drop when broken with bare hands (excluding leaves). Disabled by default.

**Details**: [v2.0.6...v2.0.7](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.6...v2.0.7)

# v2.0.6 - 2026-09-23

## What's Changed

_for player:_
- Add: World preset **Floating Islands** and **Void**. The structure Start Island can now be officially seen generating in-game.
- Add: Variants of the Start Island.

_for developer:_
- Add: `utils/spawn_estimator.py` for estimating the player's spawn biome.

**Details**: [v2.0.5...v2.0.6](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.5...v2.0.6)

# v2.0.5 - 2026-09-22

## What's Changed

- Add: The structure **Start Island**. It generates in the Overworld with the Void noise type.

**Details**: [v2.0.4...v2.0.5](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.4...v2.0.5)

# v2.0.4 - 2026-09-22

## What's Changed

- Fix: The config system didn’t work properly, so settings were ignored. Config now loads and applies correctly on startup.
- Fix: When an old config was missing newer options, they became unexpected value instead of defaults. Missing options now use the intended defaults.
- Adjust: Reverted the default Wither effect duration of the Decay Agapanthus.
- Adjust: Increased the default Mana cost per block converted by the Decay Agapanthus.

**Details**: [v2.0.3...v2.0.4](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.3...v2.0.4)

# v2.0.3 - 2026-09-22

## What's Changed

_compared with v2.0.1 (v2.0.2 was skipped):_

_for player:_
- Adjust: The **Decay Agapanthus** now applies the Wither effect to nearby mobs regardless of its Mana, and only consumes Mana when it actually converts blocks.
- Adjust: Increased the default Wither effect duration of the Decay Agapanthus.
- Adjust: Reverted the Decay Agapanthus texture.

_for developer:_
- Adjust: Cleaned up redundant code and imports.
- Adjust: The floating flower no longer redundantly implements `EntityBlock`.

**Details**: [v2.0.1...v2.0.3](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.1...v2.0.3)

# v2.0.1 - 2026-09-21

## What's Changed

_for player:_
- Adjust: Updated the Decay Agapanthus texture to High Definition.

_for developer:_
- Adjust: Moved `BotaniaIntegration` from the `block` package to the `integration` package.
- Remove: Debug console output left in the Decay Agapanthus conversion logic.

**Details**: [v2.0.0...v2.0.1](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v2.0.0...v2.0.1)

# v2.0.0 - 2026-09-21

## What's Changed

⚠️ **Breaking change**: the Decay Flower was renamed, and its block ID changed. Worlds from v1.x will lose existing Decay Flowers when upgrading.

- Rename: **Decay Flower** is now **Decay Agapanthus**. Existing blocks/items will disappear in old worlds.
- Add: Wooden Cauldrons are now included in the `minecraft:cauldrons` block tag.
- Add: Lava Wooden Cauldrons now act as heat sources for **Farmer's Delight**.
- Add: JEI recipe catalyst for the **Wooden Petal Apothecary**.
- Adjust: Collision shape for the Decay Agapanthus.
- Adjust: The petal apothecary recipe of the Decay Agapanthus has been changed.
- Adjust: Increased the default Mana cost per block converted by the Decay Agapanthus.

**Details**: [v1.1.1...v2.0.0](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v1.1.1...v2.0.0)

# v1.1.1 - 2026-09-21

## What's Changed

- Add: **Floating Decay Flower**.
- Add: The Decay Flower now supports binding with the Wand of the Forest and displays a Mana HUD.
- Add: Proper Botania functional flower integration (functional flower tags, wand HUD).
- Add: Configuration for Decay Flower.
- Add: JEI recipe catalyst for Livingwood Mana Pool.
- Add: Digging Tool support for Livingwood Pool, Wooden Petal Apothecary and Wooden Cauldron.
- Add: recipes, and missing assets/data.

**Details**: [v1.1.0...v1.1.1](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v1.1.0...v1.1.1)

# v1.1.0 - 2026-09-20

## What's Changed

_for player:_
- Add: **Decay Flower**, the first functional flower of the mod. It periodically converts nearby blocks according to data-driven Decay recipes (e.g. Dirt → Coarse Dirt, Stone → Cobblestone).
- Add: The `verdant_sky:decay` recipe type with JSON-defined block or tag inputs and block state outputs.

_for developer:_
- Adjust: Pinned the Architectury Loom version to 1.17.491.

**Details**: [v1.0.0...v1.1.0](https://github.com/sunny-verdant-leaves/verdant-sky/compare/v1.0.0...v1.1.0)

# v1.0.0 - 2026-09-19

## What's in this mod?

v1.0.0 is the first version of verdant sky.

- Add: **Livingwood Mana Pool**.
- Add: **Wooden Petal Apothecary**.
- Add: **Wooden Cauldrons** (multiple wood variants).
- The mod does not modify or replace any original Botania blocks or items.

**Details**: [v1.0.0](https://github.com/sunny-verdant-leaves/verdant-sky/tree/v1.0.0)
