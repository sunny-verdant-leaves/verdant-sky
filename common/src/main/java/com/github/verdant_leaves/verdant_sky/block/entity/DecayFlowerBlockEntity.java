package com.github.verdant_leaves.verdant_sky.block.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import com.github.verdant_leaves.verdant_sky.VerdantSkyConfig;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;

public class DecayFlowerBlockEntity extends FunctionalFlowerBlockEntity {

    private static final int MANA_COST_PER_CONVERSION = 100;
    private static final int MAX_MANA = 10000;
    private static final int DELAY = 40;

    /** 由 DecayFlowerBlock.getTicker 每次 tick 更新，避免调用 getLevel() */
    private Level cachedLevel;

    public DecayFlowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECAY_FLOWER.get(), pos, state);
    }

    public void setCachedLevel(Level level) {
        this.cachedLevel = level;
    }

    // ══════════════════════════════════════════════════════════════
    //  必须实现（基类 abstract）
    // ══════════════════════════════════════════════════════════════

    @Override
    public int getColor() {
        return 0x6B3A6B; // 暗紫色
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public RadiusDescriptor getRadius() {
        // 窥魔之镜显示：水平面 5×5（Botania 只渲染 X-Z 平面）
        // 实际作用范围：立体 5×5×5（见 performDecayConversion）
        return RadiusDescriptor.Rectangle.square(
            getEffectivePos(),
            VerdantSkyConfig.decayFlowerRadius()
        );
    }

    @Override
    public ItemStack getDefaultHudIcon() {
        // 森林法杖 HUD 左上角显示的图标
        return new ItemStack(ModBlocks.DECAY_FLOWER_ITEM.get());
    }

    // ══════════════════════════════════════════════════════════════
    //  可选重写
    // ══════════════════════════════════════════════════════════════

    @Override
    public boolean acceptsRedstone() {
        return true; // 让基类每 tick 刷新 redstoneSignal
    }

    // ══════════════════════════════════════════════════════════════
    //  核心逻辑
    // ══════════════════════════════════════════════════════════════

    @Override
    public void tickFlower() {
        // super.tickFlower() 链条：
        //   1. FunctionalFlowerBlockEntity  -> drawManaFromPool() + 刷新 redstoneSignal + 客户端粒子
        //   2. BindableSpecialFlowerBlockEntity -> 首次放置自动绑定魔力池
        //   3. SpecialFlowerBlockEntity -> ticksExisted++
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
        if (getMana() < MANA_COST_PER_CONVERSION) {
            return;
        }

        int converted = performDecayConversion();
        if (converted > 0) {
            addMana(-MANA_COST_PER_CONVERSION * converted);
            sync();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  随机转换逻辑（立体 5×5×5）
    // ══════════════════════════════════════════════════════════════

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

        // 第一步：收集所有可转换的坐标
        List<BlockPos> candidates = new ArrayList<>();
        RandomSource random = serverLevel.getRandom();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    BlockPos targetPos = pos.offset(dx, dy, dz);
                    BlockState targetState = serverLevel.getBlockState(targetPos);

                    if (targetState.isAir()) continue;

                    for (DecayRecipe recipe : recipes) {
                        if (!recipe.matchesState(targetState)) continue;
                        // 防止输出仍匹配同一条配方（如 #logs → stripped_logs）
                        if (recipe.matchesState(recipe.outputState())) continue;
                        // 防止原地"转换"
                        if (recipe.outputState() == targetState) continue;

                        candidates.add(targetPos.immutable());
                        break;
                    }
                }
            }
        }

        if (candidates.isEmpty()) return 0;

        // 第二步：随机抽取并转换
        int converted = 0;
        int attempts = Math.min(maxConversions, candidates.size());

        for (int i = 0; i < attempts; i++) {
            int index = random.nextInt(candidates.size());
            BlockPos targetPos = candidates.remove(index);

            BlockState targetState = serverLevel.getBlockState(targetPos);

            for (DecayRecipe recipe : recipes) {
                if (!recipe.matchesState(targetState)) continue;
                if (recipe.matchesState(recipe.outputState())) continue;
                if (recipe.outputState() == targetState) continue;

                serverLevel.setBlockAndUpdate(targetPos, recipe.outputState());
                serverLevel.sendParticles(
                    ParticleTypes.SCULK_SOUL,
                    targetPos.getX() + 0.5,
                    targetPos.getY() + 1.05,
                    targetPos.getZ() + 0.5,
                    6, 0.25, 0.15, 0.25, 0.01
                );
                converted++;
                break;
            }
        }

        return converted;
    }
}