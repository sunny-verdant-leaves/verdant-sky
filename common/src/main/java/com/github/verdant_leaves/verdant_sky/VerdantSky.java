package com.github.verdant_leaves.verdant_sky;

import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModCreativeTabs;

public final class VerdantSky {
    public static final String MOD_ID = "verdant_sky";

    public static void init() {
        ModBlocks.register();
        ModCreativeTabs.register();
    }
}
