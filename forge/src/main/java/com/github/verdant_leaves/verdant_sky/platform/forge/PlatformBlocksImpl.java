package com.github.verdant_leaves.verdant_sky.platform.forge;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.forge.block.ForgeSpecialFlowerBlock;

import java.util.function.Supplier;

public class PlatformBlocksImpl {
    public static Block createSpecialFlowerBlock(
            MobEffect effect, 
            int effectDuration, 
            BlockBehaviour.Properties props, 
            Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> beType) {
        return new ForgeSpecialFlowerBlock(effect, effectDuration, props, beType);
    }
}