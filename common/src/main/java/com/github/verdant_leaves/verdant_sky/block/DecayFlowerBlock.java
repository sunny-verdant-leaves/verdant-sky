package com.github.verdant_leaves.verdant_sky.block;

import com.github.verdant_leaves.verdant_sky.block.entity.DecayFlowerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 腐朽花：每 2 秒扫描以自身为中心的 5×5×5 范围，
 * 匹配 DecayRecipe 并把范围内的方块转换为配方指定的输出方块。
 */
public class DecayFlowerBlock extends BaseEntityBlock {

    public DecayFlowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // BaseEntityBlock 默认返回 INVISIBLE，需显式改为 MODEL 才会渲染模型
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DecayFlowerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        // 只在服务端执行转换逻辑
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof DecayFlowerBlockEntity flower) {
                flower.serverTick(lvl, pos, st);
            }
        };
    }
}