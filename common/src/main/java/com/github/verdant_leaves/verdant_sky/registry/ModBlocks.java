package com.github.verdant_leaves.verdant_sky.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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
import com.github.verdant_leaves.verdant_sky.block.VerdantCauldronFamily;
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
    /** 支持的木材列表。加新木材在这里加一行。 */
    public static final String[] CAULDRON_WOODS = { 
        "cherry", 
        "crimson", 
        "oak"
    };
    public static final Map<String, RegistrySupplier<Block>> EMPTY_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Block>> WATER_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Block>> LAVA_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Block>> POWDER_SNOW_CAULDRONS = new LinkedHashMap<>();

    /** 所有炼药锅的物品，供创造模式物品栏遍历。 */
    public static final List<RegistrySupplier<Item>> CAULDRON_ITEMS = new ArrayList<>();

    static {
        for (String wood : CAULDRON_WOODS) {
            registerCauldronSet(wood);
        }
    }

    private static void registerCauldronSet(String wood) {
        RegistrySupplier<Block> empty = BLOCKS.register(VerdantCauldronNames.empty(wood),
            () -> new WoodenEmptyCauldronBlock(props(wood), wood));
        EMPTY_CAULDRONS.put(wood, empty);

        RegistrySupplier<Block> water = BLOCKS.register(VerdantCauldronNames.water(wood),
            () -> new WoodenCauldronBlock(props(wood), wood));
        WATER_CAULDRONS.put(wood, water);

        RegistrySupplier<Block> lava = BLOCKS.register(VerdantCauldronNames.lava(wood),
            () -> new WoodenLavaCauldronBlock(props(wood), wood));
        LAVA_CAULDRONS.put(wood, lava);

        RegistrySupplier<Block> powder = BLOCKS.register(VerdantCauldronNames.powderSnow(wood),
            () -> new WoodenPowderSnowCauldronBlock(props(wood), wood));
        POWDER_SNOW_CAULDRONS.put(wood, powder);

        CAULDRON_ITEMS.add(ITEMS.register(VerdantCauldronNames.empty(wood),
            () -> new BlockItem(empty.get(), new Item.Properties())));
    }

    public static int burnTimeFor(String wood) {
        if (VerdantCauldronFamily.SPECIAL_WOODS.contains(wood)) {
            return 0;   // 特殊物品不燃烧
        }
        return 300;
    }

    private static BlockBehaviour.Properties props(String wood) {
        boolean nether = wood.equals("crimson") || wood.equals("warped");
        return BlockBehaviour.Properties.of()
            .mapColor(nether ? MapColor.NETHER : MapColor.WOOD)
            .strength(2.0F)
            .sound(nether ? SoundType.NETHER_WOOD : SoundType.WOOD)
            .noOcclusion();
    }


    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
