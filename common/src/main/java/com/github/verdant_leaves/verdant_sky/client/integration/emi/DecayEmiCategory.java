package com.github.verdant_leaves.verdant_sky.client.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

import net.minecraft.resources.ResourceLocation;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

public final class DecayEmiCategory {

    private DecayEmiCategory() {}

    public static final ResourceLocation ID = new ResourceLocation(VerdantSky.MOD_ID, "decay");

    public static final EmiRecipeCategory INSTANCE = new EmiRecipeCategory(ID, EmiStack.of(ModBlocks.DECAY_AGAPANTHUS.get()));
}