package com.github.verdant_leaves.verdant_sky.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.platform.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import vazkii.botania.common.block.FloatingSpecialFlowerBlock;
import vazkii.botania.common.item.block.SpecialFlowerBlockItem;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.block.*;
import com.github.verdant_leaves.verdant_sky.platform.PlatformBlocks;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(VerdantSky.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(VerdantSky.MOD_ID, Registries.ITEM);
    public static final Map<String, Block> FABRIC_PRE_INSTANTIATED = new LinkedHashMap<>();

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
    //  饮雪莲 Drinking Snow Lotus
    // ══════════════════════════════════════════════════════════════
    public static final RegistrySupplier<Block> DRINKING_SNOW_LOTUS =
        BLOCKS.register("drinking_snow_lotus", () ->
            PlatformBlocks.createSpecialFlowerBlock(
                MobEffects.SATURATION, 1, FLOWER_PROPS, 
                () -> ModBlockEntities.DRINKING_SNOW_LOTUS.get(),
                Block.box(3.0, 0.0, 3.0,  13.0, 12.0, 13.0)
            )
        );

    public static final RegistrySupplier<Item> DRINKING_SNOW_LOTUS_ITEM =
        ITEMS.register("drinking_snow_lotus", () ->
            new SpecialFlowerBlockItem(DRINKING_SNOW_LOTUS.get(), new Item.Properties())
        );

    public static final RegistrySupplier<Block> FLOATING_DRINKING_SNOW_LOTUS =
        BLOCKS.register("floating_drinking_snow_lotus", () ->
            new FloatingSpecialFlowerBlock(FLOATING_PROPS, () -> ModBlockEntities.DRINKING_SNOW_LOTUS.get())
        );

    public static final RegistrySupplier<Item> FLOATING_DRINKING_SNOW_LOTUS_ITEM =
        ITEMS.register("floating_drinking_snow_lotus", () ->
            new SpecialFlowerBlockItem(FLOATING_DRINKING_SNOW_LOTUS.get(), new Item.Properties())
        );

    // ══════════════════════════════════════════════════════════════
    //  活木魔力池
    // ══════════════════════════════════════════════════════════════
    public static final Block LIVINGWOOD_POOL_BLOCK;

    static {
        // Fabric 端：提前实例化
        if (Platform.isFabric()) {
            LIVINGWOOD_POOL_BLOCK = new VerdantPoolBlock(
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            );
        } else {
            LIVINGWOOD_POOL_BLOCK = null;
        }
    }

    public static final RegistrySupplier<Block> LIVINGWOOD_POOL = 
        BLOCKS.register("livingwood_pool", () -> {
            // Fabric 直接返回提前 new 好的实例
            if (LIVINGWOOD_POOL_BLOCK != null) {
                return LIVINGWOOD_POOL_BLOCK;
            }
            // Forge 端在安全的注册窗口期内实例化
            return new VerdantPoolBlock(
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            );
        });

    public static final RegistrySupplier<Item> LIVINGWOOD_POOL_ITEM = 
        ITEMS.register("livingwood_pool", () -> 
            new BlockItem(LIVINGWOOD_POOL.get(), new Item.Properties())
        );

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
    // ══════════════════════════════════════════════════════════════
    //  装饰方块
    // ══════════════════════════════════════════════════════════════

    // private static BlockBehaviour.Properties stoneBrickProps() {
    //     return BlockBehaviour.Properties.of()
    //         .mapColor(MapColor.STONE)
    //         .strength(1.5F, 6.0F)
    //         .requiresCorrectToolForDrops()
    //         .sound(SoundType.STONE);
    // }
    
    private static BlockBehaviour.Properties snowBrickProps() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW)
            .strength(0.6F, 0.6F)
            .requiresCorrectToolForDrops()
            .sound(ModSoundTypes.snowBrick());
    }

    public static final RegistrySupplier<Block> SNOW_BRICKS =
        BLOCKS.register("snow_bricks", () -> 
            new Block(snowBrickProps())
        );

    public static final RegistrySupplier<Item> SNOW_BRICKS_ITEM =
        ITEMS.register("snow_bricks", () ->
            new BlockItem(SNOW_BRICKS.get(), new Item.Properties())
        );

    public static final RegistrySupplier<Block> SNOW_BRICK_STAIRS =
        BLOCKS.register("snow_brick_stairs", () ->
            new StairBlock(Blocks.STONE_BRICKS.defaultBlockState(), snowBrickProps())
        );

    public static final RegistrySupplier<Item> SNOW_BRICK_STAIRS_ITEM =
        ITEMS.register("snow_brick_stairs", () ->
            new BlockItem(SNOW_BRICK_STAIRS.get(), new Item.Properties())
        );

    public static final RegistrySupplier<Block> SNOW_BRICK_SLAB =
        BLOCKS.register("snow_brick_slab", () -> 
            new SlabBlock(snowBrickProps())
        );

    public static final RegistrySupplier<Item> SNOW_BRICK_SLAB_ITEM =
        ITEMS.register("snow_brick_slab", () ->
            new BlockItem(SNOW_BRICK_SLAB.get(), new Item.Properties())
        );

    public static final RegistrySupplier<Block> SNOW_BRICK_WALL =
        BLOCKS.register("snow_brick_wall", () -> 
            new WallBlock(snowBrickProps())
        );

    public static final RegistrySupplier<Item> SNOW_BRICK_WALL_ITEM =
        ITEMS.register("snow_brick_wall", () ->
            new BlockItem(SNOW_BRICK_WALL.get(), new Item.Properties())
        );

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
