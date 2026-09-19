package com.github.verdant_leaves.verdant_sky.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> VERDANT_TAB =
        TABS.register("verdant_sky", () ->
            CreativeTabRegistry.create(
                Component.translatable("itemGroup.verdant_sky"),
                () -> new ItemStack(Items.FLOWERING_AZALEA_LEAVES) // 原版的 flowering_azalea_leaves
            )
        );
        

    public static void register() {
        TABS.register();
        
        CreativeTabRegistry.append(
            VERDANT_TAB, 
            ModBlocks.LIVINGWOOD_POOL_ITEM, 
            ModBlocks.APOTHECARY_WOODEN_ITEM,
            ModBlocks.EMPTY_CAULDRON_ITEM,
            ModBlocks.WATER_CAULDRON_ITEM,
            ModBlocks.LAVA_CAULDRON_ITEM,
            ModBlocks.POWDER_SNOW_CAULDRON_ITEM
        );
    }
}