package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

public class WoodenLavaCauldronBlock extends AbstractCauldronBlock implements VerdantCauldronFamily {

    private final WoodVariant variant;

    public WoodenLavaCauldronBlock(BlockBehaviour.Properties props, WoodVariant variant) {
        super(props, VerdantCauldronBehavior.VERDANT_LAVA_BEHAVIOR);
        this.variant = variant;
    }

    @Override public boolean isFull(BlockState state) { return true; }
    @Override protected double getContentHeight(BlockState state) { return 0.9375; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return 3; }
    @Override public WoodVariant variant() { return variant; }

    @Override
    public Item asItem() {
        Item item = ModBlocks.CAULDRON_ITEMS_BY_WOOD.get(wood()).get();
        return item;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (this.isEntityInsideContent(state, pos, entity)) {
            entity.lavaHurt();
        }
    }
}