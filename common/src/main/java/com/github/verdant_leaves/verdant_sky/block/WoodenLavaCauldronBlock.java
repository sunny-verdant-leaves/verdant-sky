package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WoodenLavaCauldronBlock extends AbstractCauldronBlock implements VerdantCauldronFamily {

    private final String wood;

    public WoodenLavaCauldronBlock(BlockBehaviour.Properties props, String wood) {
        super(props, VerdantCauldronBehavior.VERDANT_LAVA_BEHAVIOR);
        this.wood = wood;
    }

    @Override public boolean isFull(BlockState state) { return true; }
    @Override protected double getContentHeight(BlockState state) { return 0.9375; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return 3; }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (this.isEntityInsideContent(state, pos, entity)) {
            entity.lavaHurt();
        }
    }

    @Override public String wood() { return wood; }
}