package com.github.verdant_leaves.verdant_sky.integration;

import com.github.verdant_leaves.verdant_sky.block.VerdantCauldronBehavior;
import com.github.verdant_leaves.verdant_sky.block.VerdantCauldronFamily;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public final class BotaniaIntegration {
    private BotaniaIntegration() {}

    // 通过字符串查询，避免在类加载时直接依赖 Botania 类
    private static final Item WATER_BOWL = BuiltInRegistries.ITEM.get(new ResourceLocation("botania", "water_bowl"));

    public static void register() {
        // 如果 Botania 没装，直接返回，什么都不做
        if (WATER_BOWL == Items.AIR) {
            return;
        }

        // ── 空锅 + 一碗水 -> level 1 水锅 ──
        VerdantCauldronBehavior.VERDANT_EMPTY_BEHAVIOR.put(WATER_BOWL,
                (state, level, pos, player, hand, stack) -> {
            if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                        new ItemStack(Items.BOWL)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(pos, family.getWaterBlock().defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 1));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // ── 水锅 + 一碗水 -> 水位 +1，满则 PASS ──
        VerdantCauldronBehavior.VERDANT_WATER_BEHAVIOR.put(WATER_BOWL,
                (state, level, pos, player, hand, stack) -> {
            if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
                return InteractionResult.PASS;
            }
            int current = state.getValue(LayeredCauldronBlock.LEVEL);
            if (current >= 3) {
                return InteractionResult.PASS;  // 满，拒绝
            }
            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                        new ItemStack(Items.BOWL)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, current + 1));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // ── 水锅 + 碗 -> 水位 -1，归零变空锅 ──
        VerdantCauldronBehavior.VERDANT_WATER_BEHAVIOR.put(Items.BOWL,
                (state, level, pos, player, hand, stack) -> {
            if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
                return InteractionResult.PASS;
            }
            if (state.getValue(LayeredCauldronBlock.LEVEL) < 1) {
                return InteractionResult.PASS;  // 没水，拒绝
            }
            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                        new ItemStack(WATER_BOWL)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));

                int newLevel = state.getValue(LayeredCauldronBlock.LEVEL) - 1;
                BlockState newState = newLevel == 0
                        ? family.getEmptyBlock().defaultBlockState()
                        : state.setValue(LayeredCauldronBlock.LEVEL, newLevel);
                level.setBlockAndUpdate(pos, newState);
                level.playSound(null, pos, SoundEvents.BUCKET_FILL,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
    }
}