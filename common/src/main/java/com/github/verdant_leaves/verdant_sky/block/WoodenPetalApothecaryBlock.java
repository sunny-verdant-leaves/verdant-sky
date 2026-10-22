package com.github.verdant_leaves.verdant_sky.block;

import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.registry.ModWoods;
import net.minecraft.world.level.block.state.BlockBehaviour;
import vazkii.botania.common.block.PetalApothecaryBlock;

public class WoodenPetalApothecaryBlock extends PetalApothecaryBlock {
    @SuppressWarnings("unused")
    private final WoodVariant variant;

    public WoodenPetalApothecaryBlock(BlockBehaviour.Properties props, WoodVariant variant) {
        super(Variant.DEFAULT, props);
        this.variant = variant;
    }

    public WoodenPetalApothecaryBlock(BlockBehaviour.Properties props, String wood) {
        this(props, ModWoods.requireByName(wood));
    }
}