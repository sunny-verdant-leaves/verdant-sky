package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.github.verdant_leaves.verdant_sky.api.WoodVariant;

public class WoodenPowderSnowCauldronBlock extends LayeredCauldronBlock implements VerdantCauldronFamily {

    private final WoodVariant variant;

    public WoodenPowderSnowCauldronBlock(BlockBehaviour.Properties props, WoodVariant variant) {
        super(props, LayeredCauldronBlock.SNOW, VerdantCauldronBehavior.VERDANT_POWDER_SNOW_BEHAVIOR);
        this.variant = variant;
    }

    @Override public WoodVariant variant() { return variant; }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return getCloneStack();
    }
}