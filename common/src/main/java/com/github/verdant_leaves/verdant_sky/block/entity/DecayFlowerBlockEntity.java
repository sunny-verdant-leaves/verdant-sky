package com.github.verdant_leaves.verdant_sky.block.entity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.block.WandBindable;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.mana.ManaPool;

import com.github.verdant_leaves.verdant_sky.VerdantSkyConfig;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;

public class DecayFlowerBlockEntity extends FunctionalFlowerBlockEntity implements WandBindable {

    private static final int MANA_COST_PER_CONVERSION = 100;
    private static final int MAX_MANA = 10000;
    private static final int DELAY = 40;

    private Level cachedLevel;

    @Nullable
    private BlockPos boundPoolPos;

    public DecayFlowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECAY_FLOWER.get(), pos, state);
    }

    public void setCachedLevel(Level level) {
        this.cachedLevel = level;
    }

    // ══════════════════════════════════════════════════════════════
    //  WandBindable 接口实现
    // ══════════════════════════════════════════════════════════════

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        if (player.level().isClientSide()) {
            return false;
        }

        BlockEntity targetEntity = player.level().getBlockEntity(pos);
        if (targetEntity instanceof ManaPool) {
            this.setBoundPool(pos);
            player.displayClientMessage(Component.literal("腐朽花已绑定到魔力池"), true);
            return true;
        }

        return false;
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
    //  魔力绑定
    // ══════════════════════════════════════════════════════════════

    public void setBoundPool(@Nullable BlockPos poolPos) {
        this.boundPoolPos = poolPos;
        // ⚠️ 不要调用 setChanged()，它会被 remap 成 SRG 名，运行时找不到
        // sync() 是 Botania 定义的方法，它内部会处理 setChanged
        sync();
    }

    @Nullable
    public BlockPos getBoundPool() {
        return boundPoolPos;
    }

    // ══════════════════════════════════════════════════════════════
    //  核心 Tick 逻辑
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
    //  随机转换逻辑
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
                        if (recipe.matchesState(recipe.outputState())) continue;
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

    // ══════════════════════════════════════════════════════════════
    //  NBT 数据存取
    // ══════════════════════════════════════════════════════════════

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);
        if (tag.contains("boundPool")) {
            boundPoolPos = BlockPos.of(tag.getLong("boundPool"));
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);
        if (boundPoolPos != null) {
            tag.putLong("boundPool", boundPoolPos.asLong());
        }
    }
}