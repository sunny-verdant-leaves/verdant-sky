package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WoodenLavaCauldronBlock extends AbstractCauldronBlock implements VerdantCauldronFamily {

    public WoodenLavaCauldronBlock(BlockBehaviour.Properties props) {
        super(props, VerdantCauldronBehavior.VERDANT_LAVA_BEHAVIOR);
    }

    @Override public boolean isFull(BlockState state) { return true; }

    @Override protected double getContentHeight(BlockState state) { return 0.9375; }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (this.isEntityInsideContent(state, pos, entity)) {
            entity.lavaHurt();
        }
    }

    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return 3; }

    @Override public String selfName() { return VerdantCauldronNames.LAVA; }
    @Override public String emptyName() { return VerdantCauldronNames.EMPTY; }
    @Override public String waterName() { return VerdantCauldronNames.WATER; }
    @Override public String lavaName() { return VerdantCauldronNames.LAVA; }
    @Override public String powderSnowName() { return VerdantCauldronNames.POWDER_SNOW; }
}