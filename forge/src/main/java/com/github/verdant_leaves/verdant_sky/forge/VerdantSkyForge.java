package com.github.verdant_leaves.verdant_sky.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

@Mod(VerdantSky.MOD_ID)
public final class VerdantSkyForge {
    public VerdantSkyForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(VerdantSky.MOD_ID,
            FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        VerdantSky.init();

        // 注册燃烧时间事件监听
        MinecraftForge.EVENT_BUS.addListener(this::onFuelBurnTime);
    }

    private void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.is(ModBlocks.LIVINGWOOD_POOL_ITEM.get())) {
            event.setBurnTime(300);
        }
        else if (stack.is(ModBlocks.APOTHECARY_WOODEN_ITEM.get())) {
            event.setBurnTime(300);
        }
        for (String wood : ModBlocks.CAULDRON_WOODS) {
            int time = ModBlocks.burnTimeFor(wood);
            if (time <= 0) continue;

            // 检查是否是这一种木材的某个炼药锅
            if (stack.is(ModBlocks.EMPTY_CAULDRONS.get(wood).get().asItem())) {
                event.setBurnTime(time);
                return;
            }
        }
    }
}
