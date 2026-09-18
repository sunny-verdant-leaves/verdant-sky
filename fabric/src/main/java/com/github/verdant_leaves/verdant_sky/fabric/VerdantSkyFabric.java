package com.github.verdant_leaves.verdant_sky.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FuelRegistry;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

public final class VerdantSkyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        VerdantSky.init();

        // 注册燃料燃烧时间
        FuelRegistry.INSTANCE.add(ModBlocks.LIVINGWOOD_POOL_ITEM.get(), 1600);
        FuelRegistry.INSTANCE.add(ModBlocks.APOTHECARY_WOODEN_ITEM.get(), 1600);
    }
}
