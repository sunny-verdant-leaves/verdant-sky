package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WoodenEmptyCauldronBlock extends AbstractCauldronBlock implements VerdantCauldronFamily {

    private final String wood;

    public WoodenEmptyCauldronBlock(BlockBehaviour.Properties props, String wood) {
        super(props, VerdantCauldronBehavior.VERDANT_EMPTY_BEHAVIOR);
        this.wood = wood;
    }

    @Override public boolean isFull(BlockState state) { return false; }
    @Override public String wood() { return wood; }
}