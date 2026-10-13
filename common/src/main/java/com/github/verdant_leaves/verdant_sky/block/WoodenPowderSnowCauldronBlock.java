package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

public class WoodenPowderSnowCauldronBlock extends LayeredCauldronBlock implements VerdantCauldronFamily {

    private final WoodVariant variant;

    public WoodenPowderSnowCauldronBlock(BlockBehaviour.Properties props, WoodVariant variant) {
        super(props, LayeredCauldronBlock.SNOW, VerdantCauldronBehavior.VERDANT_POWDER_SNOW_BEHAVIOR);
        this.variant = variant;
    }

    @Override public WoodVariant variant() { return variant; }

    @Override
    public Item asItem() {
        Item item = ModBlocks.CAULDRON_ITEMS_BY_WOOD.get(wood()).get();
        return item;
    }
}