package com.github.verdant_leaves.verdant_sky.block;

import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public interface VerdantCauldronFamily {

    /**
     * 特殊木材列表。列在这里的木材：
     * <ul>
     *  <li>防火</li>
     *  <li>允许用熔岩桶填充</li>
     * </ul>
     * <br>
     * 加新木材时，普通木材不用动；下界型/特殊木材往这里加一行。
     */
    Set<String> SPECIAL_WOODS = Set.of(
        "crimson", 
        "warped"
    );

    String wood();

    default boolean isSpecial() {
        return SPECIAL_WOODS.contains(wood());
    }

    default Block lookup(String name) {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(VerdantSky.MOD_ID, name));
    }

    default Block getEmptyBlock()      { return lookup(VerdantCauldronNames.empty(wood())); }
    default Block getWaterBlock()      { return lookup(VerdantCauldronNames.water(wood())); }
    default Block getLavaBlock()       { return lookup(VerdantCauldronNames.lava(wood())); }
    default Block getPowderSnowBlock() { return lookup(VerdantCauldronNames.powderSnow(wood())); }
}