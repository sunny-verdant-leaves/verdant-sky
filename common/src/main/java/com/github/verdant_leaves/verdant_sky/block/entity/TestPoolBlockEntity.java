package com.github.verdant_leaves.verdant_sky.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

public class TestPoolBlockEntity extends ManaPoolBlockEntity {

    public TestPoolBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        System.out.println("[VerdantSky] ==========================================");
        System.out.println("[VerdantSky] TestPoolBlockEntity.onUsedByWand called!");
        System.out.println("[VerdantSky] Player: " + (player != null ? player.getName().getString() : "null"));
        System.out.println("[VerdantSky] Is Shift Down: " + (player != null && player.isShiftKeyDown()));
        System.out.println("[VerdantSky] ==========================================");

        // 保留原版逻辑（切换输入/输出模式）
        return super.onUsedByWand(player, stack, side);
    }
}