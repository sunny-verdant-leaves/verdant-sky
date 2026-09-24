package com.github.verdant_leaves.verdant_sky;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;

import com.github.verdant_leaves.verdant_sky.integration.BotaniaIntegration;
import com.github.verdant_leaves.verdant_sky.loot.PlantLootModifier;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModCreativeTabs;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;
import com.github.verdant_leaves.verdant_sky.world.StartIslandSpawner;

public final class VerdantSky {
    public static final String MOD_ID = "verdant_sky";

    public static void init() {

        VerdantSkyConfig.load();

        ModBlocks.register();
        ModBlockEntities.register();
        ModRecipeTypes.register();
        ModCreativeTabs.register();

        PlantLootModifier.register();
        StartIslandSpawner.register();

        // 安全地执行软依赖集成
        // 此事件在公共设置阶段触发，此时所有注册表都已填充完毕
        LifecycleEvent.SETUP.register(() -> {
            // 再次确认 Botania 是否加载，避免不必要的类加载
            if (Platform.isModLoaded("botania")) {
                BotaniaIntegration.register();
            }
        });
    }
}
