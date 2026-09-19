package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public class WoodenEmptyCauldronBlock extends AbstractCauldronBlock implements VerdantCauldronFamily {

    private final String waterBlockName;

    public WoodenEmptyCauldronBlock(BlockBehaviour.Properties props, String waterBlockName) {
        // 使用自定义的空锅交互表，而不是 CauldronInteraction.EMPTY
        super(props, VerdantCauldronBehavior.VERDANT_EMPTY_BEHAVIOR);
        this.waterBlockName = waterBlockName;
    }

    @Override
    public boolean isFull(BlockState state) {
        return false;
    }

    @Override
    public Block getEmptyBlock() {
        return this;
    }

    @Override
    public Block getWaterBlock() {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(VerdantSky.MOD_ID, waterBlockName));
    }
}