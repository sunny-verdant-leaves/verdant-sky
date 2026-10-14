package com.github.verdant_leaves.verdant_sky.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModWoods;

@SuppressWarnings("removal")
@Mod(VerdantSky.MOD_ID)
public final class VerdantSkyForge {
    public VerdantSkyForge() {
        EventBuses.registerModEventBus(VerdantSky.MOD_ID,
            FMLJavaModLoadingContext.get().getModEventBus());

        VerdantSky.init();

        MinecraftForge.EVENT_BUS.addListener(this::onFuelBurnTime);
    }

    private void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.is(ModBlocks.LIVINGWOOD_POOL_ITEM.get())) {
            event.setBurnTime(ModBlocks.WOOD_BURN_TIME);
            return;
        }

        for (WoodVariant wood : ModWoods.ALL) {
            if (wood.fireResistant()) continue;

            // 花药台
            var apothecary = ModBlocks.PETAL_APOTHECARY_ITEMS.get(wood.name());
            if (apothecary != null && stack.is(apothecary.get())) {
                event.setBurnTime(ModBlocks.WOOD_BURN_TIME);
                return;
            }

            // 炼药锅
            var empty = ModBlocks.EMPTY_CAULDRONS.get(wood.name());
            if (empty == null) continue;
            if (stack.is(empty.get().asItem())) {
                event.setBurnTime(ModBlocks.WOOD_BURN_TIME);
                return;
            }
        }
    }
}