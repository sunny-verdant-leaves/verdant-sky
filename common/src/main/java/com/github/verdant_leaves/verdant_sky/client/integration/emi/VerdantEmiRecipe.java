package com.github.verdant_leaves.verdant_sky.client.integration.emi;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;

public abstract class VerdantEmiRecipe implements EmiRecipe {

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    protected List<EmiIngredient> input = List.of();
    protected List<EmiIngredient> catalysts = List.of();
    protected List<EmiStack> output = List.of();

    public VerdantEmiRecipe(EmiRecipeCategory category, DecayRecipe recipe) {
        this.category = category;
        this.id = recipe.getId();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }
}