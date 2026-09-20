package com.github.verdant_leaves.verdant_sky.block.entity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import com.github.verdant_leaves.verdant_sky.VerdantSkyConfig;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;

public class DecayFlowerBlockEntity extends FunctionalFlowerBlockEntity {

    private static final int MANA_COST_PER_CONVERSION = 100;
    private static final int MAX_MANA = 10000;
    private static final int DELAY = 40;

    private Level cachedLevel;

    public DecayFlowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECAY_FLOWER.get(), pos, state);
    }

    public void setCachedLevel(Level level) {
        this.cachedLevel = level;
    }

    // ══════════════════════════════════════════════════════════════
    //  Botania 要求的抽象方法
    // ══════════════════════════════════════════════════════════════

    @Override
    public int getColor() {
        return 0x6B3A6B;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(
            getEffectivePos(),
            VerdantSkyConfig.decayFlowerRadius()
        );
    }

    @Override
    public boolean acceptsRedstone() {
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  核心逻辑
    // ══════════════════════════════════════════════════════════════

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (cachedLevel == null || cachedLevel.isClientSide()) {
            return;
        }
        if (redstoneSignal > 0) {
            return;
        }
        if (ticksExisted % DELAY != 0) {
            return;
        }
        if (getMana() == 0) {
            addMana(MAX_MANA);   // 一次性填满
        }
        if (getMana() < MANA_COST_PER_CONVERSION) {
            return;
        }

        int converted = performDecayConversion();
        if (converted > 0) {
            addMana(-MANA_COST_PER_CONVERSION * converted);
            sync();
        }
    }

    private int performDecayConversion() {
        if (!(cachedLevel instanceof ServerLevel serverLevel)) {
            return 0;
        }

        List<DecayRecipe> recipes =
            serverLevel.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DECAY.get());
        if (recipes.isEmpty()) return 0;

        BlockPos pos = getEffectivePos();
        int radius = VerdantSkyConfig.decayFlowerRadius();
        int maxConversions = VerdantSkyConfig.decayFlowerMaxConversionsPerTick();
        int converted = 0;

        RandomSource random = serverLevel.getRandom();

        outer:
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    BlockPos targetPos = pos.offset(dx, dy, dz);
                    BlockState targetState = serverLevel.getBlockState(targetPos);

                    if (targetState.isAir()) continue;

                    for (DecayRecipe recipe : recipes) {
                        if (recipe.matchesState(targetState)) {
                            serverLevel.setBlockAndUpdate(targetPos, recipe.outputState());
                            serverLevel.sendParticles(
                                ParticleTypes.SCULK_SOUL,
                                targetPos.getX() + 0.5,
                                targetPos.getY() + 1.05,
                                targetPos.getZ() + 0.5,
                                6, 0.25, 0.15, 0.25, 0.01
                            );
                            converted++;
                            if (converted >= maxConversions) break outer;
                            break;
                        }
                    }
                }
            }
        }
        return converted;
    }
}