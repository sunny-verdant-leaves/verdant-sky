package com.github.verdant_leaves.verdant_sky.client.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;

import dev.architectury.platform.Platform;

import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;

@EmiEntrypoint
public class VerdantSkyEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        // 注册类别
        registry.addCategory(DecayEmiCategory.INSTANCE);

        // 注册催化剂
        registry.addWorkstation(
            DecayEmiCategory.INSTANCE,
            EmiStack.of(ModBlocks.DECAY_AGAPANTHUS.get())
        );
        registry.addWorkstation(
            DecayEmiCategory.INSTANCE,
            EmiStack.of(ModBlocks.FLOATING_DECAY_AGAPANTHUS.get())
        );

        // 注册配方
        for (DecayRecipe recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DECAY.get())) {
            registry.addRecipe(new DecayEmiRecipe(recipe));
        }
        if (Platform.isModLoaded("botania")) {
            BotaniaEmiCatalysts.register(registry);
        }
    }
}