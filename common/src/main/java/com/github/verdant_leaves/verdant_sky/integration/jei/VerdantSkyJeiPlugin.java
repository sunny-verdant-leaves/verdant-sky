package com.github.verdant_leaves.verdant_sky.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.integration.jei.ManaPoolRecipeCategory;
import vazkii.botania.client.integration.jei.PetalApothecaryRecipeCategory;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

@JeiPlugin
public class VerdantSkyJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID =
        new ResourceLocation(VerdantSky.MOD_ID, "jei_plugin");

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registry) {
        // 把魔力池注册为"魔力灌注"的 JEI 催化剂
        registry.addRecipeCatalyst(
            new ItemStack(ModBlocks.LIVINGWOOD_POOL.get()),
            ManaPoolRecipeCategory.TYPE
        );
        registry.addRecipeCatalyst(
            new ItemStack(ModBlocks.APOTHECARY_WOODEN.get()),
            PetalApothecaryRecipeCategory.TYPE
        );
    }

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }
}