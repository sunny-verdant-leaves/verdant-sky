package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public class WoodenCauldronBlock extends LayeredCauldronBlock implements VerdantCauldronFamily {

    private final String emptyBlockName;

    public WoodenCauldronBlock(BlockBehaviour.Properties props, String emptyBlockName) {
        // 使用自定义的空锅交互表
        super(props, LayeredCauldronBlock.RAIN, VerdantCauldronBehavior.VERDANT_WATER_BEHAVIOR);
        this.emptyBlockName = emptyBlockName;
    }

    @Override
    public Block getEmptyBlock() {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(VerdantSky.MOD_ID, emptyBlockName));
    }

    @Override
    public Block getWaterBlock() {
        return this;
    }
}