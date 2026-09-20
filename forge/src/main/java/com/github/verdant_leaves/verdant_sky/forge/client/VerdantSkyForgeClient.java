package com.github.verdant_leaves.verdant_sky.forge.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.forge.CapabilityUtil;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.block.entity.DecayAgapanthusBlockEntity;
import com.github.verdant_leaves.verdant_sky.client.VerdantSkyClient;

@Mod.EventBusSubscriber(
        modid = VerdantSky.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class VerdantSkyForgeClient {

    private VerdantSkyForgeClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 通用客户端初始化
        event.enqueueWork(VerdantSkyClient::init);
    }

    /**
     * 平台特有：通过 capability 附加 WandHUD。
     * AttachCapabilitiesEvent 在 FORGE bus 上，与 MOD bus 不同，所以用嵌套类。
     */
    @Mod.EventBusSubscriber(
            modid = VerdantSky.MOD_ID,
            value = Dist.CLIENT,
            bus = Mod.EventBusSubscriber.Bus.FORGE
    )
    public static final class CapabilityHandler {

        private CapabilityHandler() {}

        @SubscribeEvent
        public static void attachWandHud(AttachCapabilitiesEvent<BlockEntity> event) {
            BlockEntity be = event.getObject();
            if (be instanceof DecayAgapanthusBlockEntity flower) {
                event.addCapability(
                    new ResourceLocation(VerdantSky.MOD_ID, "wand_hud"),
                    CapabilityUtil.makeProvider(
                        BotaniaForgeClientCapabilities.WAND_HUD,
                        new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>(flower)
                    )
                );
            }
        }
    }
}