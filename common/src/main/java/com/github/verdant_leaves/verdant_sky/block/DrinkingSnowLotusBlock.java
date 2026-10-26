package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DrinkingSnowLotusBlock extends FlowerBlock {

    public DrinkingSnowLotusBlock(BlockBehaviour.Properties properties) {
        super(MobEffects.FIRE_RESISTANCE, 3, properties);
    }
}