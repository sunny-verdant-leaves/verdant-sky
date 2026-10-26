package com.github.verdant_leaves.verdant_sky.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

import java.util.function.Supplier;

public class PlatformBlocks {
    @ExpectPlatform
    public static Block createSpecialFlowerBlock(
        MobEffect effect, 
        int effectDuration, 
        BlockBehaviour.Properties props, 
        Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> beType
    ) {
        throw new AssertionError("This method should be replaced by Architectury!");
    }
}