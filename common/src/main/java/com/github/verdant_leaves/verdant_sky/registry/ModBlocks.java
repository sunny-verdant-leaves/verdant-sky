package com.github.verdant_leaves.verdant_sky.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import vazkii.botania.common.item.block.SpecialFlowerBlockItem;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.block.*;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(VerdantSky.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(VerdantSky.MOD_ID, Registries.ITEM);

    private static final BlockBehaviour.Properties FLOWER_PROPS = BlockBehaviour.Properties.copy(Blocks.POPPY);
    private static final BlockBehaviour.Properties FLOATING_PROPS = BlockBehaviour.Properties.of()
        .mapColor(MapColor.PLANT)
        .strength(0.5F)
        .sound(SoundType.GRAVEL)
        .lightLevel(s -> 15);

    public static final int WOOD_BURN_TIME = 300;

    // ══════════════════════════════════════════════════════════════
    //  腐朽百子莲 Decay Agapanthus
    // ══════════════════════════════════════════════════════════════

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
        static {
            // 当 LIVINGWOOD_POOL 真正注册完成的瞬间，执行以下回调
            LIVINGWOOD_POOL.listen(block -> {
                if (dev.architectury.platform.Platform.isModLoaded("botania")) {
                    var poolType = vazkii.botania.common.block.block_entity.BotaniaBlockEntities.POOL;
                    var poolBlocks = new java.util.HashSet<>(
                        ((com.github.verdant_leaves.verdant_sky.mixin.BlockEntityTypeAccessor) (Object) poolType).getValidBlocks()
                    );
                    poolBlocks.add(block);
                    ((com.github.verdant_leaves.verdant_sky.mixin.BlockEntityTypeAccessor) (Object) poolType).setValidBlocks(poolBlocks);
                    System.out.println("[VerdantSky] Injected livingwood_pool into POOL validBlocks via listen()");
                }
                else{
                    System.out.println("[VerdantSky] Couldn't inject livingwood_pool into POOL validBlocks via listen()");
                }
            });
        }

    // ══════════════════════════════════════════════════════════════
    //  木花药台
    // ══════════════════════════════════════════════════════════════
    
    public static final Map<String, RegistrySupplier<Block>> PETAL_APOTHECARIES = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Item>> PETAL_APOTHECARY_ITEMS = new LinkedHashMap<>();

    private static void registerPetalApothecary(WoodVariant wood) {
        String id = "apothecary_" + wood.name();

        RegistrySupplier<Block> block = BLOCKS.register(id, () ->
            new WoodenPetalApothecaryBlock(apothecaryProps(wood), wood)
        );
        PETAL_APOTHECARIES.put(wood.name(), block);
        
        PETAL_APOTHECARY_ITEMS.put(wood.name(), ITEMS.register(id, () ->
            new BlockItem(block.get(), new Item.Properties())
        ));
    }
    
    private static BlockBehaviour.Properties apothecaryProps(WoodVariant wood) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
            .mapColor(wood.mapColor())
            .strength(2.5F)
            .sound(wood.soundType())
            .noOcclusion();
        return props;
    }

    // ══════════════════════════════════════════════════════════════
    //  木炼药锅
    // ══════════════════════════════════════════════════════════════

    public static final Map<String, RegistrySupplier<Block>> EMPTY_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Block>> WATER_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Block>> LAVA_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Block>> POWDER_SNOW_CAULDRONS = new LinkedHashMap<>();
    public static final Map<String, RegistrySupplier<Item>> CAULDRON_ITEMS = new LinkedHashMap<>();

    private static void registerCauldronSet(WoodVariant wood) {
        String id = wood.name();

        RegistrySupplier<Block> empty = BLOCKS.register(VerdantCauldronNames.empty(id), () -> new WoodenCauldronBlock(cauldronProps(wood), wood));
        EMPTY_CAULDRONS.put(id, empty);
        RegistrySupplier<Block> water = BLOCKS.register(VerdantCauldronNames.water(id), () -> new WoodenWaterCauldronBlock(cauldronProps(wood), wood));
        WATER_CAULDRONS.put(id, water);
        RegistrySupplier<Block> lava = BLOCKS.register(VerdantCauldronNames.lava(id), () -> new WoodenLavaCauldronBlock(cauldronProps(wood), wood));
        LAVA_CAULDRONS.put(id, lava);
        RegistrySupplier<Block> powder = BLOCKS.register(VerdantCauldronNames.powderSnow(id), () -> new WoodenPowderSnowCauldronBlock(cauldronProps(wood), wood));
        POWDER_SNOW_CAULDRONS.put(id, powder);

        // 只给空锅注册 BlockItem
        RegistrySupplier<Item> item = ITEMS.register(
            VerdantCauldronNames.empty(id),
            () -> new BlockItem(empty.get(), new Item.Properties())
        );
        CAULDRON_ITEMS.put(id, item);
    }
    
    // 炼药锅方块映射到同一物品
    public static void bindCauldronItems() {
        for (WoodVariant wood : ModWoods.ALL) {
            String id = wood.name();
    
            Item emptyItem = CAULDRON_ITEMS.get(id).get();
            if (emptyItem == Items.AIR) {
                continue;
            }
            
            Block water = WATER_CAULDRONS.get(id).get();
            Block lava = LAVA_CAULDRONS.get(id).get();
            Block powder = POWDER_SNOW_CAULDRONS.get(id).get();
            Item.BY_BLOCK.put(water,    emptyItem);
            Item.BY_BLOCK.put(lava,     emptyItem);
            Item.BY_BLOCK.put(powder,   emptyItem);
            
        }
    }

    private static BlockBehaviour.Properties cauldronProps(WoodVariant wood) {
        return BlockBehaviour.Properties.of()
            .mapColor(wood.mapColor())
            .strength(2.0F)
            .sound(wood.soundType())
            .noOcclusion();
    }
    
    // ══════════════════════════════════════════════════════════════
    //  注册所有木质的方块变体及物品。
    // ══════════════════════════════════════════════════════════════
    static {
        for (WoodVariant wood : ModWoods.ALL) {
            registerCauldronSet(wood);
            registerPetalApothecary(wood);
        }
    }

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
