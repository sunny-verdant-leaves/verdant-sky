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
        // Run our common setup.
        VerdantSky.init();

        // 注册燃料燃烧时间
        FuelRegistry.INSTANCE.add(ModBlocks.LIVINGWOOD_POOL_ITEM.get(), ModBlocks.WOOD_BURN_TIME);

        for (WoodVariant wood : ModWoods.ALL) {
            if (wood.fireResistant()) continue;

            // 花药台
            var apothecary = ModBlocks.PETAL_APOTHECARY_ITEMS.get(wood.name());
            if (apothecary != null) {
                FuelRegistry.INSTANCE.add(apothecary.get(), ModBlocks.WOOD_BURN_TIME);
            }

            // 炼药锅
            var empty = ModBlocks.EMPTY_CAULDRONS.get(wood.name());
            if (empty == null) continue;
            FuelRegistry.INSTANCE.add(empty.get().asItem(), ModBlocks.WOOD_BURN_TIME);
        }
    }
}