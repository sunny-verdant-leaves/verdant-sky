package com.github.verdant_leaves.verdant_sky.fabric.client;

import net.fabricmc.api.ClientModInitializer;

import vazkii.botania.api.BotaniaFabricClientCapabilities;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;

import com.github.verdant_leaves.verdant_sky.block.entity.DecayFlowerBlockEntity;
import com.github.verdant_leaves.verdant_sky.client.VerdantSkyClient;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;

public final class VerdantSkyFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 通用客户端初始化
        VerdantSkyClient.init();

        // 平台特有：给 Botania 注册腐朽花的 WandHUD
        registerBotaniaWandHud();
    }

    private static void registerBotaniaWandHud() {
        BotaniaFabricClientCapabilities.WAND_HUD.registerForBlockEntities(
            (be, ctx) -> new BindableSpecialFlowerBlockEntity
                .BindableFlowerWandHud<>((DecayFlowerBlockEntity) be),
            ModBlockEntities.DECAY_FLOWER.get()
        );
    }
}