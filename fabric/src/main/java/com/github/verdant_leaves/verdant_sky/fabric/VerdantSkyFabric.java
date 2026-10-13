package com.github.verdant_leaves.verdant_sky.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FuelRegistry;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModWoods;

public final class VerdantSkyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        VerdantSky.init();

        // 注册燃料燃烧时间
        FuelRegistry.INSTANCE.add(ModBlocks.LIVINGWOOD_POOL_ITEM.get(), ModBlocks.WOOD_BURN_TIME);
        FuelRegistry.INSTANCE.add(ModBlocks.APOTHECARY_WOODEN_ITEM.get(), ModBlocks.WOOD_BURN_TIME);

        for (WoodVariant wood : ModWoods.ALL) {
            if (wood.fireResistant()) continue;
            var empty = ModBlocks.EMPTY_CAULDRONS.get(wood.name());
            if (empty == null) continue;
            FuelRegistry.INSTANCE.add(empty.get().asItem(), ModBlocks.WOOD_BURN_TIME);
        }
    }
}