package com.github.verdant_leaves.verdant_sky.integration.farmersdelight;

import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;

public final class FarmersDelightIntegration {
    private static final String MOD_ID = "farmersdelight";
    public static final ResourceLocation NOURISHMENT_ID = new ResourceLocation(MOD_ID, "nourishment");

    private FarmersDelightIntegration() {}

    public static void register() {
        if (!Platform.isModLoaded(MOD_ID)) {
            return;
        }
    }
}