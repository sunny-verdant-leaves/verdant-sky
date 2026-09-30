package com.github.verdant_leaves.verdant_sky.mixin;

import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 让我们能读写 BlockEntityType 的 validBlocks 集合。
 * 用于在 Botania 初始化完成后，往它已有的 BE 类型里追加我们的方块。
 */
@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {

    @Accessor("validBlocks")
    Set<Block> getValidBlocks();

    @Accessor("validBlocks")
    @Mutable
    void setValidBlocks(Set<Block> blocks);
}