package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.cauldron.CauldronInteraction;

public class WoodenCauldronBlock extends LayeredCauldronBlock {
    public WoodenCauldronBlock(BlockBehaviour.Properties props) {
        super(props, LayeredCauldronBlock.RAIN, CauldronInteraction.WATER);
    }
}