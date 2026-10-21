package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.github.verdant_leaves.verdant_sky.block.entity.TestPoolBlockEntity;

import vazkii.botania.common.block.mana.ManaPoolBlock;

public class VerdantPoolBlock extends ManaPoolBlock {
    public VerdantPoolBlock(BlockBehaviour.Properties props) {
        super(Variant.DILUTED, props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TestPoolBlockEntity(pos, state);
    }
}