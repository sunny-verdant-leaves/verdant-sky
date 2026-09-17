package com.github.verdant_leaves.verdant_sky.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.block.VerdantDilutedPoolBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.BLOCK);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Block> VERDANT_DILUTED_POOL =
        BLOCKS.register("verdant_diluted_pool", () ->
            new VerdantDilutedPoolBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F)
                .sound(SoundType.STONE)
                .noOcclusion()
            )
        );

    public static final RegistrySupplier<Item> VERDANT_DILUTED_POOL_ITEM =
        ITEMS.register("verdant_diluted_pool", () ->
            new BlockItem(VERDANT_DILUTED_POOL.get(), new Item.Properties())
        );

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}