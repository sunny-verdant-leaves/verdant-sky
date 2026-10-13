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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import vazkii.botania.common.item.block.SpecialFlowerBlockItem;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.block.DecayAgapanthusBlock;
import com.github.verdant_leaves.verdant_sky.block.FloatingDecayAgapanthusBlock;
import com.github.verdant_leaves.verdant_sky.block.VerdantCauldronNames;
import com.github.verdant_leaves.verdant_sky.block.VerdantPoolBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenCauldronBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenWaterCauldronBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenLavaCauldronBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenPowderSnowCauldronBlock;
import com.github.verdant_leaves.verdant_sky.block.WoodenPetalApothecaryBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(VerdantSky.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(VerdantSky.MOD_ID, Registries.ITEM);
    
    public static final int WOOD_BURN_TIME = 300;

    // ══════════════════════════════════════════════════════════════
    //  腐朽百子莲 Decay Agapanthus
    // ══════════════════════════════════════════════════════════════

    private static final BlockBehaviour.Properties FLOWER_PROPS =
        BlockBehaviour.Properties.copy(Blocks.POPPY);

    private static final BlockBehaviour.Properties FLOATING_PROPS =
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .strength(0.5F)
            .sound(SoundType.GRAVEL)
            .lightLevel(s -> 15);

    public static final RegistrySupplier<Block> DECAY_AGAPANTHUS =
        BLOCKS.register("decay_agapanthus", () ->
            new DecayAgapanthusBlock(FLOWER_PROPS)
        );

    public static final RegistrySupplier<Item> DECAY_AGAPANTHUS_ITEM =
        ITEMS.register("decay_agapanthus", () ->
            new SpecialFlowerBlockItem(DECAY_AGAPANTHUS.get(), new Item.Properties())
        );

    public static final RegistrySupplier<Block> FLOATING_DECAY_AGAPANTHUS =
        BLOCKS.register("floating_decay_agapanthus", () ->
            new FloatingDecayAgapanthusBlock(FLOATING_PROPS)
        );

    public static final RegistrySupplier<Item> FLOATING_DECAY_AGAPANTHUS_ITEM =
        ITEMS.register("floating_decay_agapanthus", () ->
            new SpecialFlowerBlockItem(FLOATING_DECAY_AGAPANTHUS.get(), new Item.Properties())
        );

    // ══════════════════════════════════════════════════════════════
    //  活木魔力池
    // ══════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════
    //  木花药台
    // ══════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════
    //  木炼药锅
    // ══════════════════════════════════════════════════════════════

    public static final Map<String, RegistrySupplier<Block>> EMPTY_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Block>> WATER_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Block>> LAVA_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Block>> POWDER_SNOW_CAULDRONS = new LinkedHashMap<>();

    /** 所有炼药锅的物品，供创造模式物品栏遍历。 */
    public static final List<RegistrySupplier<Item>> CAULDRON_ITEMS = new ArrayList<>();

    static {
        for (WoodVariant wood : ModWoods.ALL) {
            registerCauldronSet(wood);
        }
    }

    private static void registerCauldronSet(WoodVariant wood) {
        String id = wood.name();

        RegistrySupplier<Block> empty = BLOCKS.register(VerdantCauldronNames.empty(id), () -> new WoodenCauldronBlock(props(wood), wood));
        EMPTY_CAULDRONS.put(id, empty);
        RegistrySupplier<Block> water = BLOCKS.register(VerdantCauldronNames.water(id), () -> new WoodenWaterCauldronBlock(props(wood), wood));
        WATER_CAULDRONS.put(id, water);
        RegistrySupplier<Block> lava = BLOCKS.register(VerdantCauldronNames.lava(id), () -> new WoodenLavaCauldronBlock(props(wood), wood));
        LAVA_CAULDRONS.put(id, lava);
        RegistrySupplier<Block> powder = BLOCKS.register(VerdantCauldronNames.powderSnow(id), () -> new WoodenPowderSnowCauldronBlock(props(wood), wood));
        POWDER_SNOW_CAULDRONS.put(id, powder);

        CAULDRON_ITEMS.add(
            ITEMS.register(
                VerdantCauldronNames.empty(id),
                () -> new BlockItem(empty.get(), new Item.Properties())
            )
        );
    }

    private static BlockBehaviour.Properties props(WoodVariant wood) {
        return BlockBehaviour.Properties.of()
            .mapColor(wood.mapColor())
            .strength(2.0F)
            .sound(wood.soundType())
            .noOcclusion();
    }

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
