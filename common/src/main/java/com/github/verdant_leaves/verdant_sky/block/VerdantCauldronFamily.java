package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public interface VerdantCauldronFamily {

    String wood();

    default Block lookup(String name) {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(VerdantSky.MOD_ID, name));
    }

    default Block getEmptyBlock()      { return lookup(VerdantCauldronNames.empty(wood())); }
    default Block getWaterBlock()      { return lookup(VerdantCauldronNames.water(wood())); }
    default Block getLavaBlock()       { return lookup(VerdantCauldronNames.lava(wood())); }
    default Block getPowderSnowBlock() { return lookup(VerdantCauldronNames.powderSnow(wood())); }
}