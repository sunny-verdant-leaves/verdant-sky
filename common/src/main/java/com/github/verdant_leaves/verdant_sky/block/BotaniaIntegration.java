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
        

    // ── 空锅 + 一碗水 -> 满水锅，一碗水变碗 ──
    VerdantCauldronBehavior.VERDANT_EMPTY_BEHAVIOR.put(WATER_BOWL,
            (state, level, pos, player, hand, stack) -> {
        if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                    new ItemStack(Items.BOWL)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));

            level.setBlockAndUpdate(pos, family.getWaterBlock().defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, 3));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    });

    // ── 水锅 + 一碗水 -> 水位拉满，一碗水变碗 ──
    VerdantCauldronBehavior.VERDANT_WATER_BEHAVIOR.put(WATER_BOWL,
            (state, level, pos, player, hand, stack) -> {
        if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
            return InteractionResult.PASS;
        }
        // 已经满，PASS
        if (state.getValue(LayeredCauldronBlock.LEVEL) == 3) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                    new ItemStack(Items.BOWL)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));

            level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, 3));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    });

    // ── 水锅 + 碗 -> 舀水，一碗水；满水锅才允许 ──
    VerdantCauldronBehavior.VERDANT_WATER_BEHAVIOR.put(Items.BOWL,
            (state, level, pos, player, hand, stack) -> {
        if (!(state.getBlock() instanceof VerdantCauldronFamily family)) {
            return InteractionResult.PASS;
        }
        // 只有满水锅能舀，对齐水桶逻辑
        if (state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                    new ItemStack(WATER_BOWL)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));

            // 满 -> 直接变空锅（对齐水桶）
            level.setBlockAndUpdate(pos, family.getEmptyBlock().defaultBlockState());
            level.playSound(null, pos, SoundEvents.BUCKET_FILL,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    });
    }
}