package com.github.verdant_leaves.verdant_sky.fabric;

import net.fabricmc.api.ModInitializer;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public final class VerdantSkyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        VerdantSky.init();
        VerdantSky.blocksRegister();
        VerdantSky.blocksEntitiesRegister();
    }
}
