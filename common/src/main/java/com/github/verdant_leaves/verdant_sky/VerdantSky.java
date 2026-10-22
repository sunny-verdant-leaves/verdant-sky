package com.github.verdant_leaves.verdant_sky;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;

import com.github.verdant_leaves.verdant_sky.integration.botania.BotaniaIntegration;
import com.github.verdant_leaves.verdant_sky.loot.PlantLootModifier;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModCreativeTabs;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;
import com.github.verdant_leaves.verdant_sky.registry.ModSoundEvents;
import com.github.verdant_leaves.verdant_sky.world.StartIslandSpawner;

public final class VerdantSky {
    public static final String MOD_ID = "verdant_sky";

    public static void init() {

        VerdantSkyConfig.load();
        
        ModSoundEvents.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModRecipeTypes.register();
        ModCreativeTabs.register();

        PlantLootModifier.register();
        StartIslandSpawner.register();

        // 此事件在公共设置阶段触发，此时所有注册表都已填充完毕

        LifecycleEvent.SETUP.register(() -> {
            // 再次确认 Botania 是否加载，避免不必要的类加载
            if (Platform.isModLoaded("botania")) {
                ModBlockEntities.injectIntoBotania();
                BotaniaIntegration.register();
            }
            
            // 统一注册方块的可燃性
            // for (var wood : ModWoods.ALL) {
            //     if (wood.fireResistant()) continue;
            //     var apothecaryBlock = ModBlocks.PETAL_APOTHECARIES.get(wood.name());
            //     if (apothecaryBlock != null) {
            //         PlatformFlammable.Holder.INSTANCE.register(apothecaryBlock.get(), 20, 5);
            //     }
            // }
        });
        
        LifecycleEvent.SETUP.register(ModBlocks::bindCauldronItems);
    }
}
