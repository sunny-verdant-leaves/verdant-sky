package com.github.verdant_leaves.verdant_sky.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> VERDANT_TAB =
        TABS.register("verdant_sky", () ->
            CreativeTabRegistry.create(
                Component.translatable("itemGroup.verdant_sky"),
                () -> new ItemStack(ModBlocks.LIVINGWOOD_POOL_ITEM.get())
            )
        );

    public static void register() {
        TABS.register();
        CreativeTabRegistry.append(VERDANT_TAB, ModBlocks.LIVINGWOOD_POOL_ITEM);
    }
}