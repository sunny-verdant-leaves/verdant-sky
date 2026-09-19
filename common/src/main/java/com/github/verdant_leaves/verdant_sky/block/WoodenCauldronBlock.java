package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoodenCauldronBlock extends LayeredCauldronBlock implements VerdantCauldronFamily {

    private final String wood;

    public WoodenCauldronBlock(BlockBehaviour.Properties props, String wood) {
        super(props, LayeredCauldronBlock.RAIN, VerdantCauldronBehavior.VERDANT_WATER_BEHAVIOR);
        this.wood = wood;
    }

    @Override public String wood() { return wood; }
}