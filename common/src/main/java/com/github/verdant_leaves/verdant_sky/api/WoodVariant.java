package com.github.verdant_leaves.verdant_sky.api;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public record WoodVariant(
    String name,
    boolean fireResistant,
    MapColor mapColor,
    SoundType soundType
) {
    /** 标准主世界木材。 */
    public static WoodVariant normal(String name) {
        return new WoodVariant(name, false, MapColor.WOOD, SoundType.WOOD);
    }

    /** 下界木材（防火）。 */
    public static WoodVariant nether(String name) {
        return new WoodVariant(name, true, MapColor.NETHER, SoundType.NETHER_WOOD);
    }
}