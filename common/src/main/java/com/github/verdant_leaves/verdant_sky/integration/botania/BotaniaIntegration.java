package com.github.verdant_leaves.verdant_sky.integration.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import com.github.verdant_leaves.verdant_sky.VerdantSkyConfig;
import com.github.verdant_leaves.verdant_sky.block.VerdantCauldronBehavior;
import com.github.verdant_leaves.verdant_sky.block.VerdantCauldronFamily;

public final class BotaniaIntegration {
    private BotaniaIntegration() {}

    // 通过字符串查询，避免在类加载时直接依赖 Botania 类
    private static final Item WATER_BOWL = BuiltInRegistries.ITEM.get(new ResourceLocation("botania", "water_bowl"));

    public static void register() {
        if (WATER_BOWL == Items.AIR) {
            return;
        }
        if (!VerdantSkyConfig.enableWaterBowlModification()) {
            return;
        }

        // 同一个处理器，注册到自定义 map 和原版 map
        CauldronInteraction emptyHandler = emptyHandler();
        CauldronInteraction addWaterHandler = addWaterHandler();
        CauldronInteraction drainWaterHandler = drainWaterHandler();

        // 自定义锅
        VerdantCauldronBehavior.VERDANT_EMPTY_BEHAVIOR.put(WATER_BOWL, emptyHandler);
        VerdantCauldronBehavior.VERDANT_WATER_BEHAVIOR.put(WATER_BOWL, addWaterHandler);
        VerdantCauldronBehavior.VERDANT_WATER_BEHAVIOR.put(Items.BOWL, drainWaterHandler);

        // 原版锅
        CauldronInteraction.EMPTY.put(WATER_BOWL, emptyHandler);
        CauldronInteraction.WATER.put(WATER_BOWL, addWaterHandler);
        CauldronInteraction.WATER.put(Items.BOWL, drainWaterHandler);
    }

    // 空锅 + 一碗水 -> 水锅（level 1）
    private static CauldronInteraction emptyHandler() {
        return (state, level, pos, player, hand, stack) -> {
            Block waterBlock = resolveWaterBlock(state);
            if (waterBlock == null) return InteractionResult.PASS;

            if (!level.isClientSide) {
                consumeWaterBowl(level, pos, player, hand, stack);
                level.setBlockAndUpdate(pos, waterBlock.defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, 1));
                playFillSound(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    // 水锅 + 一碗水 -> 水位 +1，满则 PASS
    private static CauldronInteraction addWaterHandler() {
        return (state, level, pos, player, hand, stack) -> {
            if (!state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                return InteractionResult.PASS;
            }
            int current = state.getValue(LayeredCauldronBlock.LEVEL);
            if (current >= 3) return InteractionResult.PASS;

            if (!level.isClientSide) {
                consumeWaterBowl(level, pos, player, hand, stack);
                level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, current + 1));
                playFillSound(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    // 水锅 + 碗 -> 水位-1，归零则空锅
    private static CauldronInteraction drainWaterHandler() {
        return (state, level, pos, player, hand, stack) -> {
            if (!state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                return InteractionResult.PASS;
            }
            int current = state.getValue(LayeredCauldronBlock.LEVEL);
            if (current < 1) return InteractionResult.PASS;

            Block emptyBlock = resolveEmptyBlock(state);
            if (emptyBlock == null) return InteractionResult.PASS;

            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(WATER_BOWL)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));

                int newLevel = current - 1;
                BlockState newState = newLevel == 0
                    ? emptyBlock.defaultBlockState()
                    : state.setValue(LayeredCauldronBlock.LEVEL, newLevel);
                level.setBlockAndUpdate(pos, newState);
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
    }

    // 解析：当前方块应该变成哪个水锅
    private static Block resolveWaterBlock(BlockState state) {
        if (state.getBlock() instanceof VerdantCauldronFamily family) {
            return family.getWaterBlock();
        }
        if (state.is(Blocks.CAULDRON)) {
            return Blocks.WATER_CAULDRON;
        }
        return null;
    }

    // 解析：当前水锅应该变成哪个空锅
    private static Block resolveEmptyBlock(BlockState state) {
        if (state.getBlock() instanceof VerdantCauldronFamily family) {
            return family.getEmptyBlock();
        }
        if (state.is(Blocks.WATER_CAULDRON)) {
            return Blocks.CAULDRON;
        }
        return null;
    }

    private static void consumeWaterBowl(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        Item item = stack.getItem();
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BOWL)));
        player.awardStat(Stats.USE_CAULDRON);
        player.awardStat(Stats.ITEM_USED.get(item));
    }

    private static void playFillSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
    }
}