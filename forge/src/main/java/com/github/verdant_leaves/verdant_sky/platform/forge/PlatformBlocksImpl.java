package com.github.verdant_leaves.verdant_sky.platform.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.forge.block.ForgeSpecialFlowerBlock;

import java.util.function.Supplier;

public class PlatformBlocksImpl {
    public static Block createSpecialFlowerBlock(
        MobEffect effect, 
        int effectDuration, 
        BlockBehaviour.Properties props, 
        Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> beType, 
        @Nullable VoxelShape shape, 
        boolean hasComparatorOutput
    ) {
        if (shape == null) {
            return new ForgeSpecialFlowerBlock(effect, effectDuration, props, beType, hasComparatorOutput);
        }

        return new ForgeSpecialFlowerBlock(effect, effectDuration, props, beType, hasComparatorOutput) {
            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                Vec3 shift = state.getOffset(level, pos);
                return shape.move(shift.x, shift.y, shift.z);
            }
        };
    }
}