package com.github.verdant_leaves.verdant_sky.block;

import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

import com.github.verdant_leaves.verdant_sky.block.entity.DecayAgapanthusBlockEntity;

public class DecayAgapanthusBlock extends FlowerBlock implements EntityBlock {

    public DecayAgapanthusBlock(Properties properties) {
        super(MobEffects.WITHER, 8, properties);
    }

    // 类里加一个常量
    private static final VoxelShape SHAPE = Block.box(
        3.0, 0.0, 3.0,      // minX, minY, minZ
        13.0, 14.0, 13.0    // maxX, maxY, maxZ
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DecayAgapanthusBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof DecayAgapanthusBlockEntity flower) {
                // 缓存 Level 给我们自己的逻辑用（绕开 getLevel 的 remap 问题）
                flower.setCachedLevel(lvl);
                // 走 Botania 的标准 tick 入口：红绳转接器、附魔土加速、ticksExisted++ 全在这
                SpecialFlowerBlockEntity.commonTick(lvl, pos, st, flower);
            }
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() > 0.2F) return;

        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
        double y = pos.getY() + 0.1 + random.nextDouble() * 0.3;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;

        level.addParticle(ParticleTypes.SCULK_SOUL, x, y, z, 0.0, 0.02, 0.0);

        if (random.nextFloat() < 0.3F) {
            level.addParticle(ParticleTypes.ASH, x, y, z, 0.0, 0.01, 0.0);
        }
    }
}