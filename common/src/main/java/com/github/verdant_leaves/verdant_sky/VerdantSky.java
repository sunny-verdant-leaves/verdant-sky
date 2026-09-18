package com.github.verdant_leaves.verdant_sky;

import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;

public final class VerdantSky {
    public static final String MOD_ID = "verdant_sky";

    public static void init() {
    }

    public static void blocksRegister() {
        ModBlocks.register();
    }

    public static void blocksEntitiesRegister() {
        ModBlockEntities.register();
    }
}
