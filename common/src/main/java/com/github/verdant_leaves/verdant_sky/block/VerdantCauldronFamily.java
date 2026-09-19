package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.block.Block;

public interface VerdantCauldronFamily {
    // 水锅舀空后应该变成的空锅。
    Block getEmptyBlock();

    // 空锅装水后应该变成的水锅。
    Block getWaterBlock();
}