package com.github.verdant_leaves.verdant_sky.client.integration.emi;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;

import vazkii.botania.client.integration.emi.BotaniaEmiPlugin;

import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

/**
 * Botania 相关的 EMI 催化剂注册。
 *
 * <p>仅当 Botania 加载时有效
 */
final class BotaniaEmiCatalysts {

    private BotaniaEmiCatalysts() {}

    static void register(EmiRegistry registry) {
        // 魔力灌注
        registry.addWorkstation(
            BotaniaEmiPlugin.MANA_INFUSION,
            EmiStack.of(ModBlocks.LIVINGWOOD_POOL.get())
        );
        
        // 花瓣炼制
        registry.addWorkstation(
            BotaniaEmiPlugin.PETAL_APOTHECARY,
            EmiStack.of(ModBlocks.APOTHECARY_WOODEN.get())
        );
    }
}