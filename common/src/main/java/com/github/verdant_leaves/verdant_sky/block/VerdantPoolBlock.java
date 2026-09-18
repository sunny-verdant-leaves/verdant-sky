package com.github.verdant_leaves.verdant_sky.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.mana.ManaPoolBlock;

import com.github.verdant_leaves.verdant_sky.block.entity.VerdantPoolBlockEntity;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;

public class VerdantPoolBlock extends ManaPoolBlock {
    public VerdantPoolBlock(BlockBehaviour.Properties props) {
        super(Variant.DILUTED, props);
        System.out.println("[VerdantSky] Try to VerdantPoolBlock");
    }

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        System.out.println("[VerdantSky] Try to VerdantPoolBlockEntity");
        return new VerdantPoolBlockEntity(pos, state);
	}

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.VERDANT_POOL.get(),
            level.isClientSide
                ? VerdantPoolBlockEntity::clientTick
                : VerdantPoolBlockEntity::serverTick);
    }
}