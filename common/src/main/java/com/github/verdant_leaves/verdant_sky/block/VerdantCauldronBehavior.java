package com.github.verdant_leaves.verdant_sky.block;

import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class VerdantCauldronBehavior {

    public static final Map<Item, CauldronInteraction> VERDANT_EMPTY_BEHAVIOR =
            CauldronInteraction.newInteractionMap();
    public static final Map<Item, CauldronInteraction> VERDANT_WATER_BEHAVIOR =
            CauldronInteraction.newInteractionMap();
    public static final Map<Item, CauldronInteraction> VERDANT_LAVA_BEHAVIOR =
            CauldronInteraction.newInteractionMap();
    public static final Map<Item, CauldronInteraction> VERDANT_POWDER_SNOW_BEHAVIOR =
            CauldronInteraction.newInteractionMap();

    static {
        initEmpty();
        initWater();
        initLava();
        initPowderSnow();
    }

    // ==================== 空锅 ====================
    private static void initEmpty() {
        VERDANT_EMPTY_BEHAVIOR.putAll(CauldronInteraction.EMPTY);

        // 水桶 -> 自己的水锅（满水位）
        VERDANT_EMPTY_BEHAVIOR.put(Items.WATER_BUCKET, (state, level, pos, player, hand, stack) -> {
            if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
                return InteractionResult.PASS;
            }
            return emptyBucket(level, pos, player, hand, stack,
                    family.getWaterBlock().defaultBlockState()
                            .setValue(LayeredCauldronBlock.LEVEL, 3),
                    SoundEvents.BUCKET_EMPTY);
        });

        // 熔岩桶 -> 只有特殊木材（crimson / warped）才允许
        VERDANT_EMPTY_BEHAVIOR.put(Items.LAVA_BUCKET, (state, level, pos, player, hand, stack) -> {
            if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
                return InteractionResult.PASS;
            }
            if (!family.isSpecial()) {
                return InteractionResult.PASS;
            }
            return emptyBucket(level, pos, player, hand, stack,
                    family.getLavaBlock().defaultBlockState(),
                    SoundEvents.BUCKET_EMPTY_LAVA);
        });

        // 细雪桶 -> 自己的细雪锅（满水位）
        VERDANT_EMPTY_BEHAVIOR.put(Items.POWDER_SNOW_BUCKET, (state, level, pos, player, hand, stack) -> {
            if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
                return InteractionResult.PASS;
            }
            return emptyBucket(level, pos, player, hand, stack,
                    family.getPowderSnowBlock().defaultBlockState()
                            .setValue(LayeredCauldronBlock.LEVEL, 3),
                    SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
        });

        // 水瓶 -> 自己的水锅（默认 level 1）
        VERDANT_EMPTY_BEHAVIOR.put(Items.POTION, (state, level, pos, player, hand, stack) -> {
            if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
                return InteractionResult.PASS;
            }
            if (PotionUtils.getPotion(stack) != Potions.WATER) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                        new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(pos, family.getWaterBlock().defaultBlockState());
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
    }

    // ==================== 水锅 ====================
    private static void initWater() {
        VERDANT_WATER_BEHAVIOR.putAll(CauldronInteraction.WATER);

        // 水桶继续加水 -> 直接拉到满水位
        VERDANT_WATER_BEHAVIOR.put(Items.WATER_BUCKET,
                (state, level, pos, player, hand, stack) ->
                        emptyBucket(level, pos, player, hand, stack,
                                state.setValue(LayeredCauldronBlock.LEVEL, 3),
                                SoundEvents.BUCKET_EMPTY));

        // 空桶舀水 -> 自己的空锅
        VERDANT_WATER_BEHAVIOR.put(Items.BUCKET,
                (state, level, pos, player, hand, stack) ->
                        fillBucket(state, level, pos, player, hand, stack,
                                new ItemStack(Items.WATER_BUCKET),
                                s -> s.getValue(LayeredCauldronBlock.LEVEL) == 3,
                                SoundEvents.BUCKET_FILL));

        // 玻璃瓶装水 -> 水位减 1，归零则自己的空锅
        VERDANT_WATER_BEHAVIOR.put(Items.GLASS_BOTTLE, (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                        PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                lowerFillLevel(state, level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // 皮革盔甲清洗
        VERDANT_WATER_BEHAVIOR.put(Items.LEATHER_BOOTS, verdantDyedItem());
        VERDANT_WATER_BEHAVIOR.put(Items.LEATHER_LEGGINGS, verdantDyedItem());
        VERDANT_WATER_BEHAVIOR.put(Items.LEATHER_CHESTPLATE, verdantDyedItem());
        VERDANT_WATER_BEHAVIOR.put(Items.LEATHER_HELMET, verdantDyedItem());
        VERDANT_WATER_BEHAVIOR.put(Items.LEATHER_HORSE_ARMOR, verdantDyedItem());

        // 旗帜清洗
        CauldronInteraction verdantBanner = verdantBanner();
        VERDANT_WATER_BEHAVIOR.put(Items.WHITE_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.GRAY_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.BLACK_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.BLUE_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.BROWN_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.CYAN_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.GREEN_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.LIGHT_BLUE_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.LIGHT_GRAY_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.LIME_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.MAGENTA_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.ORANGE_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.PINK_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.PURPLE_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.RED_BANNER, verdantBanner);
        VERDANT_WATER_BEHAVIOR.put(Items.YELLOW_BANNER, verdantBanner);

        // 潜影盒清洗
        CauldronInteraction verdantShulker = verdantShulkerBox();
        VERDANT_WATER_BEHAVIOR.put(Items.WHITE_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.GRAY_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.BLACK_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.BLUE_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.BROWN_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.CYAN_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.GREEN_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.LIGHT_BLUE_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.LIGHT_GRAY_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.LIME_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.MAGENTA_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.ORANGE_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.PINK_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.PURPLE_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.RED_SHULKER_BOX, verdantShulker);
        VERDANT_WATER_BEHAVIOR.put(Items.YELLOW_SHULKER_BOX, verdantShulker);
    }

    // ==================== 熔岩锅 ====================
    private static void initLava() {
        VERDANT_LAVA_BEHAVIOR.putAll(CauldronInteraction.LAVA);

        // 空桶舀熔岩 -> 自己的空锅
        VERDANT_LAVA_BEHAVIOR.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> {
            if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                        new ItemStack(Items.LAVA_BUCKET)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(pos, family.getEmptyBlock().defaultBlockState());
                level.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // 其他桶 -> PASS，避免被替换
        VERDANT_LAVA_BEHAVIOR.put(Items.LAVA_BUCKET,
                (state, level, pos, player, hand, stack) -> InteractionResult.PASS);
        VERDANT_LAVA_BEHAVIOR.put(Items.WATER_BUCKET,
                (state, level, pos, player, hand, stack) -> InteractionResult.PASS);
        VERDANT_LAVA_BEHAVIOR.put(Items.POWDER_SNOW_BUCKET,
                (state, level, pos, player, hand, stack) -> InteractionResult.PASS);
    }

    // ==================== 细雪锅 ====================
    private static void initPowderSnow() {
        VERDANT_POWDER_SNOW_BEHAVIOR.putAll(CauldronInteraction.POWDER_SNOW);

        // 空桶：满时才能舀，舀完 -> 自己的空锅
        VERDANT_POWDER_SNOW_BEHAVIOR.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> {
            if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
                return InteractionResult.PASS;
            }
            if (state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                        new ItemStack(Items.POWDER_SNOW_BUCKET)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(pos, family.getEmptyBlock().defaultBlockState());
                level.playSound(null, pos, SoundEvents.BUCKET_FILL_POWDER_SNOW,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // 其他桶 -> PASS
        VERDANT_POWDER_SNOW_BEHAVIOR.put(Items.LAVA_BUCKET,
                (state, level, pos, player, hand, stack) -> InteractionResult.PASS);
        VERDANT_POWDER_SNOW_BEHAVIOR.put(Items.WATER_BUCKET,
                (state, level, pos, player, hand, stack) -> InteractionResult.PASS);
        VERDANT_POWDER_SNOW_BEHAVIOR.put(Items.POWDER_SNOW_BUCKET,
                (state, level, pos, player, hand, stack) -> InteractionResult.PASS);
    }

    // ==================== 通用工具 ====================

    private static void lowerFillLevel(BlockState state, Level level, BlockPos pos) {
        int newLevel = state.getValue(LayeredCauldronBlock.LEVEL) - 1;
        BlockState newState;
        if (newLevel == 0) {
            Block emptyBlock = state.getBlock() instanceof VerdantCauldronFamily family
                    ? family.getEmptyBlock()
                    : Blocks.CAULDRON;
            newState = emptyBlock.defaultBlockState();
        } else {
            newState = state.setValue(LayeredCauldronBlock.LEVEL, newLevel);
        }
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
    }

    private static InteractionResult emptyBucket(Level level, BlockPos pos, Player player,
                                                  InteractionHand hand, ItemStack stack,
                                                  BlockState newState, SoundEvent sound) {
        if (!level.isClientSide) {
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                    new ItemStack(Items.BUCKET)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static InteractionResult fillBucket(BlockState state, Level level, BlockPos pos,
                                                 Player player, InteractionHand hand, ItemStack stack,
                                                 ItemStack result, Predicate<BlockState> predicate,
                                                 SoundEvent sound) {
        if (!predicate.test(state)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            Block emptyBlock = state.getBlock() instanceof VerdantCauldronFamily family
                    ? family.getEmptyBlock()
                    : Blocks.CAULDRON;
            level.setBlockAndUpdate(pos, emptyBlock.defaultBlockState());
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // ==================== 自定义清洗行为 ====================

    private static CauldronInteraction verdantDyedItem() {
        return (state, level, pos, player, hand, stack) -> {
            if (!(stack.getItem() instanceof DyeableLeatherItem dyeable)) {
                return InteractionResult.PASS;
            }
            if (!dyeable.hasCustomColor(stack)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                dyeable.clearColor(stack);
                player.awardStat(Stats.CLEAN_ARMOR);
                lowerFillLevel(state, level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    private static CauldronInteraction verdantBanner() {
        return (state, level, pos, player, hand, stack) -> {
            if (BannerBlockEntity.getPatternCount(stack) <= 0) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                ItemStack cleaned = stack.copyWithCount(1);
                BannerBlockEntity.removeLastPattern(cleaned);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, cleaned);
                } else if (player.getInventory().add(cleaned)) {
                    player.inventoryMenu.sendAllDataToRemote();
                } else {
                    player.drop(cleaned, false);
                }
                player.awardStat(Stats.CLEAN_BANNER);
                lowerFillLevel(state, level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    private static CauldronInteraction verdantShulkerBox() {
        return (state, level, pos, player, hand, stack) -> {
            Block block = Block.byItem(stack.getItem());
            if (!(block instanceof ShulkerBoxBlock)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                ItemStack cleaned = new ItemStack(Blocks.SHULKER_BOX);
                if (stack.hasTag()) {
                    cleaned.setTag(stack.getTag().copy());
                }
                player.setItemInHand(hand, cleaned);
                player.awardStat(Stats.CLEAN_SHULKER_BOX);
                lowerFillLevel(state, level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }
}