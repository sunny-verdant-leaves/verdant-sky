package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModWoods;

public class WoodenWaterCauldronBlock extends LayeredCauldronBlock implements VerdantCauldronFamily {

    private final WoodVariant variant;

    public WoodenWaterCauldronBlock(BlockBehaviour.Properties props, WoodVariant variant) {
        super(props, LayeredCauldronBlock.RAIN, VerdantCauldronBehavior.VERDANT_WATER_BEHAVIOR);
        this.variant = variant;
    }

    public WoodenWaterCauldronBlock(BlockBehaviour.Properties props, String wood) {
        this(props, ModWoods.requireByName(wood));
    }

    @Override public WoodVariant variant() { return variant; }

    @Override
    public Item asItem() {
        Item item = ModBlocks.CAULDRON_ITEMS.get(wood()).get();
        return item;
    }
}