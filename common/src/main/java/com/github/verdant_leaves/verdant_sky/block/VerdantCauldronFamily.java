package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.api.WoodVariant;

public interface VerdantCauldronFamily {
    /** 该炼药锅对应的木材定义。 */
    WoodVariant variant();

    default String wood() {
        return variant().name();
    }

    /** 是否防火。防火的木材才允许用熔岩桶填充。 */
    default boolean fireResistant() {
        return variant().fireResistant();
    }

    default Block lookup(String name) {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(VerdantSky.MOD_ID, name));
    }

    default Block getEmptyBlock()      { return lookup(VerdantCauldronNames.empty(wood())); }
    default Block getWaterBlock()      { return lookup(VerdantCauldronNames.water(wood())); }
    default Block getLavaBlock()       { return lookup(VerdantCauldronNames.lava(wood())); }
    default Block getPowderSnowBlock() { return lookup(VerdantCauldronNames.powderSnow(wood())); }
    default ItemStack getCloneStack() {
        return new ItemStack(getEmptyBlock().asItem());
    }
}