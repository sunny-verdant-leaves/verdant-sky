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

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.api.WoodenVariantRegistry;
import com.github.verdant_leaves.verdant_sky.block.VerdantPoolBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenPetalApothecaryBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenCauldronBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.BLOCK);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.ITEM);

    
    /** 活木魔力池 */
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
    

    /** 木花药台 */
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
    

    /** 木炼药锅 */
    public static final List<RegistrySupplier<Item>> WOODEN_CAULDRON_ITEMS = new ArrayList<>();
    static {
        WoodenVariantRegistry.registerAll(
            BLOCKS, ITEMS, WOODEN_CAULDRON_ITEMS,
            "cauldron",
            (wood, props) -> new WoodenCauldronBlock(props),
            WoodenVariantRegistry.WOOD_PROPERTIES
        );
    }


    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
