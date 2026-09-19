package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public record VerdantCauldronRefs(String empty, String water, String lava, String powderSnow) {

    public Block resolve(String name) {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(VerdantSky.MOD_ID, name));
    }

    public Block emptyBlock() { return resolve(empty); }
    public Block waterBlock() { return resolve(water); }
    public Block lavaBlock() { return resolve(lava); }
    public Block powderSnowBlock() { return resolve(powderSnow); }
}