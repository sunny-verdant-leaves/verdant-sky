package com.github.verdant_leaves.verdant_sky.registry;

import java.util.ArrayList;
import java.util.List;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.api.WoodenVariantRegistry;
import com.github.verdant_leaves.verdant_sky.block.VerdantCauldronNames;
import com.github.verdant_leaves.verdant_sky.block.VerdantPoolBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenEmptyCauldronBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenCauldronBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenLavaCauldronBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenPowderSnowCauldronBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenPetalApothecaryBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.BLOCK);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.ITEM);

    
    // 活木魔力池
    public static final RegistrySupplier<Block> LIVINGWOOD_POOL =
        BLOCKS.register("livingwood_pool", () ->
            new VerdantPoolBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
            )
        );
    
    public static final RegistrySupplier<Item> LIVINGWOOD_POOL_ITEM =
        ITEMS.register("livingwood_pool", () ->
            new BlockItem(LIVINGWOOD_POOL.get(), new Item.Properties())
        );
    

    // 木花药台
    public static final RegistrySupplier<Block> APOTHECARY_WOODEN =
    BLOCKS.register("apothecary_wooden", () ->
        new WoodenPetalApothecaryBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.5F)
            .sound(SoundType.WOOD)
            .noOcclusion()
        )
    );

    public static final RegistrySupplier<Item> APOTHECARY_WOODEN_ITEM =
        ITEMS.register("apothecary_wooden", () ->
            new BlockItem(APOTHECARY_WOODEN.get(), new Item.Properties())
        );
    

    // 木炼药锅
    public static final RegistrySupplier<Block> EMPTY_CAULDRON =
    BLOCKS.register(VerdantCauldronNames.EMPTY, () ->
        new WoodenEmptyCauldronBlock(props()));

    public static final RegistrySupplier<Block> WATER_CAULDRON =
        BLOCKS.register(VerdantCauldronNames.WATER, () ->
            new WoodenCauldronBlock(props()));

    public static final RegistrySupplier<Block> LAVA_CAULDRON =
        BLOCKS.register(VerdantCauldronNames.LAVA, () ->
            new WoodenLavaCauldronBlock(props()));

    public static final RegistrySupplier<Block> POWDER_SNOW_CAULDRON =
        BLOCKS.register(VerdantCauldronNames.POWDER_SNOW, () ->
            new WoodenPowderSnowCauldronBlock(props()));

    public static final RegistrySupplier<Item> EMPTY_CAULDRON_ITEM =
        ITEMS.register(VerdantCauldronNames.EMPTY, () ->
            new BlockItem(EMPTY_CAULDRON.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> WATER_CAULDRON_ITEM =
        ITEMS.register(VerdantCauldronNames.WATER, () ->
            new BlockItem(WATER_CAULDRON.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LAVA_CAULDRON_ITEM =
        ITEMS.register(VerdantCauldronNames.LAVA, () ->
            new BlockItem(LAVA_CAULDRON.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> POWDER_SNOW_CAULDRON_ITEM =
        ITEMS.register(VerdantCauldronNames.POWDER_SNOW, () ->
            new BlockItem(POWDER_SNOW_CAULDRON.get(), new Item.Properties()));
    
    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .noOcclusion();
    }

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
