package com.github.verdant_leaves.verdant_sky.client.integration.jei;

import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import dev.architectury.platform.Platform;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;

@JeiPlugin
public class VerdantSkyJEIPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(VerdantSky.MOD_ID, "jei_plugin");

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    // ══════════════════════════════════════════════════════════════
    //  注册配方类别
    // ══════════════════════════════════════════════════════════════

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new DecayRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    // ══════════════════════════════════════════════════════════════
    //  注册配方数据
    // ══════════════════════════════════════════════════════════════

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        List<DecayRecipe> decayRecipes = level.getRecipeManager()
            .getAllRecipesFor(ModRecipeTypes.DECAY.get());

        if (!decayRecipes.isEmpty()) {
            registration.addRecipes(DecayRecipeCategory.TYPE, decayRecipes);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  注册配方催化剂
    // ══════════════════════════════════════════════════════════════

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        // 本模组的催化剂
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.DECAY_AGAPANTHUS.get()),
            DecayRecipeCategory.TYPE
        );

        // 仅 Botania 加载时才注册
        if (Platform.isModLoaded("botania")) {
            BotaniaJeiCatalysts.register(registration);
        }
    }
}