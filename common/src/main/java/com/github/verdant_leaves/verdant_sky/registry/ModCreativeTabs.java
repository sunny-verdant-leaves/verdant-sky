package com.github.verdant_leaves.verdant_sky.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(VerdantSky.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> VERDANT_TAB = TABS.register("verdant_sky", () ->
        CreativeTabRegistry.create(
            Component.translatable("itemGroup.verdant_sky"),
            () -> new ItemStack(ModBlocks.FLOATING_DECAY_AGAPANTHUS_ITEM.get())
        )
    );
        
    @SuppressWarnings("unchecked")
    public static void register() {
        TABS.register();
        
        // 魔力系统
        CreativeTabRegistry.append(
            VERDANT_TAB,
            ModBlocks.DECAY_AGAPANTHUS_ITEM,
            ModBlocks.FLOATING_DECAY_AGAPANTHUS_ITEM,
            ModBlocks.DRINKING_SNOW_LOTUS_ITEM,
            ModBlocks.FLOATING_DRINKING_SNOW_LOTUS_ITEM,
            ModBlocks.LIVINGWOOD_POOL_ITEM
        );
        
        // 花药台
        for (RegistrySupplier<Item> item : ModBlocks.PETAL_APOTHECARY_ITEMS.values()) {
            CreativeTabRegistry.append(VERDANT_TAB, item);
        }
        
        // 炼药锅
        for (RegistrySupplier<Item> item : ModBlocks.CAULDRON_ITEMS.values()) {
            CreativeTabRegistry.append(VERDANT_TAB, item);
        }

        CreativeTabRegistry.append(
            VERDANT_TAB,
            
            // 雪砖装饰变种
            ModBlocks.SNOW_BRICKS_ITEM,
            ModBlocks.SNOW_BRICK_STAIRS_ITEM,
            ModBlocks.SNOW_BRICK_SLAB_ITEM,
            ModBlocks.SNOW_BRICK_WALL_ITEM,
            
            // 食物
            ModItems.SEED_SNOWBALL,
            ModItems.SNOW_TOSSED_GREENS,
            ModItems.SNOW_PACKED_SHOOTS
        );

        // 原版标签页 - 建筑方块
        CreativeTabRegistry.append(
            CreativeModeTabs.BUILDING_BLOCKS,
            ModBlocks.SNOW_BRICKS_ITEM,
            ModBlocks.SNOW_BRICK_STAIRS_ITEM,
            ModBlocks.SNOW_BRICK_SLAB_ITEM,
            ModBlocks.SNOW_BRICK_WALL_ITEM
        );
        
        // 原版标签页 - 食物与饮品
        CreativeTabRegistry.append(
            CreativeModeTabs.FOOD_AND_DRINKS,
            ModItems.SEED_SNOWBALL,
            ModItems.SNOW_TOSSED_GREENS,
            ModItems.SNOW_PACKED_SHOOTS
        );
    }
}