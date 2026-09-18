package com.github.verdant_leaves.verdant_sky.api;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * 描述一种木材类型。
 *
 * <p>这是一个数据类，不包含任何注册逻辑。
 * 其他方块类型（炼药锅、工作台、箱子等）都可以复用这些定义。</p>
 */
public record WoodVariant(
        String name,
        boolean fireResistant,
        MapColor mapColor,
        SoundType soundType
) {
    /** 构造一个标准主世界木材。 */
    public static WoodVariant normal(String name) {
        return new WoodVariant(name, false, MapColor.WOOD, SoundType.WOOD);
    }

    /** 构造一个下界木材（防火）。 */
    public static WoodVariant nether(String name) {
        return new WoodVariant(name, true, MapColor.NETHER, SoundType.NETHER_WOOD);
    }
}