package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import vazkii.botania.common.block.mana.ManaPoolBlock;

public class VerdantPoolBlock extends ManaPoolBlock {
    public VerdantPoolBlock(BlockBehaviour.Properties props) {
        super(Variant.DILUTED, props);
    }
}