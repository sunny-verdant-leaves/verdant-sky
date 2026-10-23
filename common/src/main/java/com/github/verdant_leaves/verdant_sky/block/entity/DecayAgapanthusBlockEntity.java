package com.github.verdant_leaves.verdant_sky.block.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import com.github.verdant_leaves.verdant_sky.VerdantSkyConfig;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;

public class DecayAgapanthusBlockEntity extends FunctionalFlowerBlockEntity {

    // 由 DecayAgapanthusBlock.getTicker 每次 tick 更新，避免调用 getLevel()
    private Level cachedLevel;

    public DecayAgapanthusBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECAY_AGAPANTHUS.get(), pos, state);
    }

    public void setCachedLevel(Level level) {
        this.cachedLevel = level;
    }

    @Override
    public int getColor() {
        return 0x44509D; // 蓝紫色
    }

    @Override
    public int getMaxMana() {
        return VerdantSkyConfig.decayAgapanthusMaxMana();
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(
            getEffectivePos(),
            VerdantSkyConfig.decayAgapanthusRadius()
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
        super.tickFlower(); // 抽取魔力 + 红石刷新 + 客户端粒子

        if (cachedLevel == null || cachedLevel.isClientSide()) {
            return;
        }
        if (redstoneSignal > 0) {
            return;
        }
        if (ticksExisted % VerdantSkyConfig.decayAgapanthusInterval() != 0) {
            return;
        }
        if (!(cachedLevel instanceof ServerLevel serverLevel)) {
            return;
        }

        int manaCost = VerdantSkyConfig.decayAgapanthusManaCost();
        int configMax = VerdantSkyConfig.decayAgapanthusMaxConversionsPerTick();

        if (configMax <= 0) {
            return;
        }

        // 计算当前魔力最多进行几次转换
        int maxAffordable;
        if (manaCost <= 0) {
            maxAffordable = configMax;
        } else {
            maxAffordable = getMana() / manaCost;
        }

        if (maxAffordable <= 0) {
            return;
        }

        // 尝试转换
        BlockPos center = getEffectivePos();
        int converted = performDecayConversion(serverLevel, maxAffordable); // 传入魔力可进行转换的数量

        // 只有真的转换了方块 (实际转换的数量>0)，才施加凋零效果 + 响音效 + 扣魔力
        if (converted > 0) {
            playWorkSound(serverLevel, center);
            
            // 只有正常消耗模式才扣除魔力
            if (manaCost > 0) {
                addMana(-manaCost * converted);
            }
            
            applyWitherToNearbyEntities(serverLevel, center);
            sync();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  音效
    // ══════════════════════════════════════════════════════════════

    private void playWorkSound(ServerLevel level, BlockPos pos) {
        level.playSound(
            null,
            pos,
            SoundEvents.SOUL_ESCAPE,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );
    }

    // ══════════════════════════════════════════════════════════════
    //  凋零效果
    // ══════════════════════════════════════════════════════════════

    private void applyWitherToNearbyEntities(ServerLevel level, BlockPos center) {
        int duration = VerdantSkyConfig.decayAgapanthusWitherDuration();
        if (duration <= 0) {
            return;
        }
    
        int radius = VerdantSkyConfig.decayAgapanthusRadius();
        AABB area = new AABB(
            center.getX() - radius,
            center.getY() - radius,
            center.getZ() - radius,
            center.getX() + radius + 1,
            center.getY() + radius + 1,
            center.getZ() + radius + 1
        );
    
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        if (entities.isEmpty()) return;
    
        MobEffectInstance wither = new MobEffectInstance(
            MobEffects.WITHER,
            duration,
            VerdantSkyConfig.decayAgapanthusWitherAmplifier()
        );
    
        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(wither));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  转换逻辑
    // ══════════════════════════════════════════════════════════════

    private int performDecayConversion(ServerLevel serverLevel, int maxAffordable) {
        List<DecayRecipe> allRecipes = serverLevel.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DECAY.get());
        if (allRecipes.isEmpty()) return 0;
    
        BlockPos flowerPos = getEffectivePos();
    
        List<DecayRecipe> recipes = new ArrayList<>();
        for (DecayRecipe recipe : allRecipes) {
            if (recipe.matchesLocation(serverLevel, flowerPos)) {
                recipes.add(recipe);
            }
        }
        if (recipes.isEmpty()) return 0;

        int radius = VerdantSkyConfig.decayAgapanthusRadius();
        
        // 实际尝试转换的上限 = 玩家配置的最大值 与 当前魔力能承受的最大值 取最小
        int maxConversions = Math.min(VerdantSkyConfig.decayAgapanthusMaxConversionsPerTick(), maxAffordable);

        List<BlockPos> candidates = new ArrayList<>();
        RandomSource random = serverLevel.getRandom();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    BlockPos targetPos = flowerPos.offset(dx, dy, dz);
                    BlockState targetState = serverLevel.getBlockState(targetPos);

                    if (targetState.isAir()) continue;

                    for (DecayRecipe recipe : recipes) {
                        if (!recipe.matchesState(targetState)) continue;
                        if (recipe.outputState() == targetState) continue;

                        candidates.add(targetPos.immutable());
                        break;
                    }
                }
            }
        }

        if (candidates.isEmpty()) return 0;

        int converted = 0;
        int attempts = Math.min(maxConversions, candidates.size());

        for (int i = 0; i < attempts; i++) {
            int index = random.nextInt(candidates.size());
            BlockPos targetPos = candidates.remove(index);

            BlockState targetState = serverLevel.getBlockState(targetPos);

            for (DecayRecipe recipe : recipes) {
                if (!recipe.matchesState(targetState)) continue;
                if (recipe.outputState() == targetState) continue;

                serverLevel.setBlockAndUpdate(targetPos, recipe.outputState());
                serverLevel.sendParticles(
                    ParticleTypes.SCULK_SOUL,
                    targetPos.getX() + 0.5,
                    targetPos.getY() + 1.1,
                    targetPos.getZ() + 0.5,
                    10, 0.4, 0.3, 0.4, 0.01
                );
                converted++;
                break;
            }
        }

        return converted;
    }
}