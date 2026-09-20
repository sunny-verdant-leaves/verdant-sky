package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import vazkii.botania.common.block.FloatingSpecialFlowerBlock;

import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;

public class FloatingDecayFlowerBlock extends FloatingSpecialFlowerBlock {
    public FloatingDecayFlowerBlock(BlockBehaviour.Properties properties) {
        super(properties, () -> ModBlockEntities.DECAY_FLOWER.get());
    }
}