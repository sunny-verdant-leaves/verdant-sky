package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoodenPowderSnowCauldronBlock extends LayeredCauldronBlock implements VerdantCauldronFamily {

    private final String wood;

    public WoodenPowderSnowCauldronBlock(BlockBehaviour.Properties props, String wood) {
        super(props, LayeredCauldronBlock.SNOW, VerdantCauldronBehavior.VERDANT_POWDER_SNOW_BEHAVIOR);
        this.wood = wood;
    }

    @Override public String wood() { return wood; }
}