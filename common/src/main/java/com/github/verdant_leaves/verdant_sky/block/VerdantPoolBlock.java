package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

public class VerdantPoolBlock extends ManaPoolBlock {
    public VerdantPoolBlock(BlockBehaviour.Properties props) {
        super(Variant.DILUTED, props);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide 
            ? (BlockEntityTicker<T>) (BlockEntityTicker<ManaPoolBlockEntity>) ManaPoolBlockEntity::clientTick
            : (BlockEntityTicker<T>) (BlockEntityTicker<ManaPoolBlockEntity>) ((lvl, pos, bs, be) -> {
                ManaPoolBlockEntity.serverTick(lvl, pos, bs, be);
                // 每40 tick打印一次，确认serverTick在执行
                if (lvl.getGameTime() % 40 == 0) {
                    System.out.println("[VerdantSky] ManaPool serverTick is running at " + pos);
                }
            });
    }
}