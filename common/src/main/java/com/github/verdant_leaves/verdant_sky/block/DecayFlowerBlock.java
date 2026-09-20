package com.github.verdant_leaves.verdant_sky.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.github.verdant_leaves.verdant_sky.block.entity.DecayFlowerBlockEntity;

public class DecayFlowerBlock extends FlowerBlock implements EntityBlock {

    public DecayFlowerBlock(Properties properties) {
        super(MobEffects.WITHER, 8, properties);
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
        return (lvl, pos, st, be) -> {
            if (be instanceof DecayFlowerBlockEntity flower) {
                flower.setCachedLevel(lvl);
                flower.tickFlower();
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