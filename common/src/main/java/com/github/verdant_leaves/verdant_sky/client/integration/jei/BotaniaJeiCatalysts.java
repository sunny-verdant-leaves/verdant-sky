package com.github.verdant_leaves.verdant_sky.client.integration.jei;

import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import dev.architectury.registry.registries.RegistrySupplier;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.integration.jei.ManaPoolRecipeCategory;
import vazkii.botania.client.integration.jei.PetalApothecaryRecipeCategory;

import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

/**
 * Botania 相关的 JEI 催化剂注册。
 *
 * <p>仅当 Botania 加载时有效
 */
final class BotaniaJeiCatalysts {

    private BotaniaJeiCatalysts() {}

    static void register(@NotNull IRecipeCatalystRegistration registration) {
        // 魔力灌注
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.LIVINGWOOD_POOL.get()),
            ManaPoolRecipeCategory.TYPE
        );

        // 花瓣炼制：所有木材变种都注册为催化剂
        for (RegistrySupplier<Block> apothecary : ModBlocks.PETAL_APOTHECARIES.values()) {
            registration.addRecipeCatalyst(
                new ItemStack(apothecary.get()),
                PetalApothecaryRecipeCategory.TYPE
            );
        }
    }
}