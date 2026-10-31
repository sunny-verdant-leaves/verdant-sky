package com.github.verdant_leaves.verdant_sky.fabric.client;

import net.fabricmc.api.ClientModInitializer;

import vazkii.botania.api.BotaniaFabricClientCapabilities;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;

import com.github.verdant_leaves.verdant_sky.block.entity.DecayAgapanthusBlockEntity;
import com.github.verdant_leaves.verdant_sky.block.entity.DrinkingSnowLotusBlockEntity;
import com.github.verdant_leaves.verdant_sky.client.VerdantSkyClient;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;

public final class VerdantSkyFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 通用客户端初始化
        VerdantSkyClient.init();

        // 给 Botania 注册 WandHUD
        registerBotaniaWandHud();
    }

    private static void registerBotaniaWandHud() {
        // -- 腐朽百子莲 --
        BotaniaFabricClientCapabilities.WAND_HUD.registerForBlockEntities(
            (be, ctx) -> new BindableSpecialFlowerBlockEntity
                .BindableFlowerWandHud<>((DecayAgapanthusBlockEntity) be),
            ModBlockEntities.DECAY_AGAPANTHUS.get()
        );
        
        // -- 饮雪莲 --
        BotaniaFabricClientCapabilities.WAND_HUD.registerForBlockEntities(
            (be, ctx) -> new BindableSpecialFlowerBlockEntity
                .BindableFlowerWandHud<>((DrinkingSnowLotusBlockEntity) be),
            ModBlockEntities.DRINKING_SNOW_LOTUS.get()
        );
    }
}