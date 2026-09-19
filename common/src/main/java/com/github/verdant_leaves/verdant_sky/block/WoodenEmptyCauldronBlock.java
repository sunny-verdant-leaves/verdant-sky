package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WoodenEmptyCauldronBlock extends AbstractCauldronBlock implements VerdantCauldronFamily {

    public WoodenEmptyCauldronBlock(BlockBehaviour.Properties props) {
        super(props, VerdantCauldronBehavior.VERDANT_EMPTY_BEHAVIOR);
    }

    @Override public boolean isFull(BlockState state) { return false; }

    @Override public String selfName() { return VerdantCauldronNames.EMPTY; }
    @Override public String emptyName() { return VerdantCauldronNames.EMPTY; }
    @Override public String waterName() { return VerdantCauldronNames.WATER; }
    @Override public String lavaName() { return VerdantCauldronNames.LAVA; }
    @Override public String powderSnowName() { return VerdantCauldronNames.POWDER_SNOW; }
}