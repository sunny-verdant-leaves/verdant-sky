package com.github.verdant_leaves.verdant_sky.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

@Mod(VerdantSky.MOD_ID)
public final class VerdantSkyForge {
    public VerdantSkyForge(IEventBus modEventBus) {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(VerdantSky.MOD_ID, modEventBus);

        // Run our common setup.
        VerdantSky.init();

        // 注册燃烧时间事件监听
        MinecraftForge.EVENT_BUS.addListener(this::onFuelBurnTime);
    }

    private void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().is(ModBlocks.LIVINGWOOD_POOL_ITEM.get())) {
            event.setBurnTime(1600);
        }
        else if (event.getItemStack().is(ModBlocks.APOTHECARY_WOODEN_ITEM.get())) {
            event.setBurnTime(1600);
        }
    }
}
