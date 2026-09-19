package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public interface VerdantCauldronFamily {

    // 本方块自己是哪一种锅。
    String selfName();

    // 邻居的注册名。
    String emptyName();
    String waterName();
    String lavaName();
    String powderSnowName();

    default Block lookup(String name) {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(VerdantSky.MOD_ID, name));
    }

    default Block getEmptyBlock() { return lookup(emptyName()); }
    default Block getWaterBlock() { return lookup(waterName()); }
    default Block getLavaBlock() { return lookup(lavaName()); }
    default Block getPowderSnowBlock() { return lookup(powderSnowName()); }
}