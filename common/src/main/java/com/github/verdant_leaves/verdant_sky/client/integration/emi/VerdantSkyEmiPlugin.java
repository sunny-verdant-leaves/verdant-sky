package com.github.verdant_leaves.verdant_sky.client.integration.emi;

import java.util.Comparator;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;

import dev.architectury.platform.Platform;

import net.minecraft.resources.ResourceLocation;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;

@EmiEntrypoint
public class VerdantSkyEmiPlugin implements EmiPlugin {

    private static final Comparator<EmiRecipe> BY_ID = Comparator.comparing(EmiRecipe::getId);

    public static final EmiRecipeCategory DECAY = createCategory(
        "decay",
        EmiStack.of(ModBlocks.DECAY_AGAPANTHUS.get()),
        BY_ID
    );

    private static EmiRecipeCategory createCategory(String idPath, EmiRenderable icon, Comparator<EmiRecipe> comp) {
        return new EmiRecipeCategory(
            new ResourceLocation(VerdantSky.MOD_ID, idPath),
            icon, icon, comp
        );
    }

    @Override
    public void register(EmiRegistry registry) {
        // 注册类别
        registry.addCategory(DECAY);

        // 注册催化剂
        registry.addWorkstation(DECAY, EmiStack.of(ModBlocks.DECAY_AGAPANTHUS.get()));
        registry.addWorkstation(DECAY, EmiStack.of(ModBlocks.FLOATING_DECAY_AGAPANTHUS.get()));

        // 注册配方
        for (DecayRecipe recipe : registry.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.DECAY.get())) {
            registry.addRecipe(new DecayEmiRecipe(recipe));
        }
        if (Platform.isModLoaded("botania")) {
            BotaniaEmiCatalysts.register(registry);
        }
    }
}