package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoodenPowderSnowCauldronBlock extends LayeredCauldronBlock implements VerdantCauldronFamily {

    public WoodenPowderSnowCauldronBlock(BlockBehaviour.Properties props) {
        super(props, LayeredCauldronBlock.SNOW, VerdantCauldronBehavior.VERDANT_POWDER_SNOW_BEHAVIOR);
    }

    @Override public String selfName() { return VerdantCauldronNames.POWDER_SNOW; }
    @Override public String emptyName() { return VerdantCauldronNames.EMPTY; }
    @Override public String waterName() { return VerdantCauldronNames.WATER; }
    @Override public String lavaName() { return VerdantCauldronNames.LAVA; }
    @Override public String powderSnowName() { return VerdantCauldronNames.POWDER_SNOW; }
}