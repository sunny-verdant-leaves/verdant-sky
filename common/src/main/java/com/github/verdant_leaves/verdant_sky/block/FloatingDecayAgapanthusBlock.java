package com.github.verdant_leaves.verdant_sky.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.FloatingSpecialFlowerBlock;

import com.github.verdant_leaves.verdant_sky.block.entity.DecayAgapanthusBlockEntity;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;

public class FloatingDecayAgapanthusBlock extends FloatingSpecialFlowerBlock {

    public FloatingDecayAgapanthusBlock(BlockBehaviour.Properties properties) {
        super(properties, () -> ModBlockEntities.DECAY_AGAPANTHUS.get());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DecayAgapanthusBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof DecayAgapanthusBlockEntity flower) {
                flower.setCachedLevel(lvl);
                SpecialFlowerBlockEntity.commonTick(lvl, pos, st, flower);
            }
        };
    }
}