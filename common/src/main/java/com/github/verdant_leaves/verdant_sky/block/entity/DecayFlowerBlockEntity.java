package com.github.verdant_leaves.verdant_sky.block.entity;

import java.util.List;

import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DecayFlowerBlockEntity extends BlockEntity {

    /** 触发间隔（tick），40 = 2 秒 */
    private static final int INTERVAL = 40;

    /** 5×5×5 → offset -2..2 */
    private static final int RADIUS = 2;

    private int tickCounter = 0;

    public DecayFlowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECAY_FLOWER.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (++tickCounter < INTERVAL) return;
        tickCounter = 0;

        List<DecayRecipe> recipes =
            serverLevel.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DECAY.get());
        if (recipes.isEmpty()) return;

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    BlockPos targetPos = pos.offset(dx, dy, dz);
                    BlockState targetState = serverLevel.getBlockState(targetPos);

                    // 空气无需转换，尽早跳过
                    if (targetState.isAir()) continue;

                    for (DecayRecipe recipe : recipes) {
                        if (recipe.matchesState(targetState)) {
                            serverLevel.setBlockAndUpdate(targetPos, recipe.outputState());
                            break;
                        }
                    }
                }
            }
        }
    }
}