package com.github.verdant_leaves.verdant_sky.block.entity;

import java.util.Arrays;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;

public class CorruptBelladonnaBlockEntity extends SpecialFlowerBlockEntity {

    // 3x3 去掉中心 = 8 个相邻位置，环状顺序
    private static final BlockPos[] NEIGHBOR_OFFSETS = {
        new BlockPos(-1, 0, -1),
        new BlockPos(-1, 0,  0),
        new BlockPos(-1, 0,  1),
        new BlockPos( 0, 0,  1),
        new BlockPos( 1, 0,  1),
        new BlockPos( 1, 0,  0),
        new BlockPos( 1, 0, -1),
        new BlockPos( 0, 0, -1),
    };

    private static final int CHECK_INTERVAL = 5;
    private static final int PROGRESS_PER_CHECK = NEIGHBOR_OFFSETS.length * CHECK_INTERVAL; // 40
    private static final int PROGRESS_REQUIRED = 20 * 60; // 1200 ticks = 60秒

    private static final String TAG_POSITION = "positionAt";
    private static final String TAG_PROGRESS = "progress";

    private final int[] positionProgress = new int[NEIGHBOR_OFFSETS.length];
    private final int[] lastSnapshot = new int[NEIGHBOR_OFFSETS.length];
    private int rotationIndex = 0;

    public CorruptBelladonnaBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CORRUPT_BELLADONNA.get(), pos, state);
        Arrays.fill(positionProgress, -1);
        Arrays.fill(lastSnapshot, -1);
    }
    
    // ══════════════════════════════════════════════════════════════
    //  核心逻辑
    // ══════════════════════════════════════════════════════════════
    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null) {
            return;
        }

        if (level.isClientSide) {
            emitProgressParticles();
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (ticksExisted % CHECK_INTERVAL != 0) {
            return;
        }

        rotationIndex = (rotationIndex + 1) % NEIGHBOR_OFFSETS.length;

        BlockPos targetPos = getEffectivePos().offset(NEIGHBOR_OFFSETS[rotationIndex]);
        BlockState targetState = serverLevel.getBlockState(targetPos);

        if (targetState.isAir()) {
            positionProgress[rotationIndex] = -1;
            saveAndSyncIfChanged();
            return;
        }

        DecayRecipe matchedRecipe = searchRecipe(serverLevel, targetPos, targetState);
        if (matchedRecipe == null) {
            positionProgress[rotationIndex] = -1;
            saveAndSyncIfChanged();
            return;
        }

        if (positionProgress[rotationIndex] < 0) {
            positionProgress[rotationIndex] = 0;
        }
        positionProgress[rotationIndex] += PROGRESS_PER_CHECK;

        if (positionProgress[rotationIndex] >= PROGRESS_REQUIRED) {
            positionProgress[rotationIndex] = -1;

            serverLevel.setBlockAndUpdate(targetPos, matchedRecipe.outputState());
            serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                targetPos.getX() + 0.5,
                targetPos.getY() + 1.1,
                targetPos.getZ() + 0.5,
                12, 0.4, 0.3, 0.4, 0.01
            );
            playSoulEscape(serverLevel);
            sync();
        }

        saveAndSyncIfChanged();
    }

    /* 客户端粒子 */
    private void emitProgressParticles() {
        for (int i = 0; i < NEIGHBOR_OFFSETS.length; i++) {
            if (positionProgress[i] <= 0) {
                continue;
            }
            BlockPos coords = getEffectivePos().offset(NEIGHBOR_OFFSETS[i]);
            double x = coords.getX() + Math.random();
            double y = coords.getY() + Math.random() + 0.5;
            double z = coords.getZ() + Math.random();
            level.addParticle(ParticleTypes.SCULK_SOUL, x, y, z, 0, 0.01, 0);
        }
    }
    
    // ══════════════════════════════════════════════════════════════
    //  辅助方法
    // ══════════════════════════════════════════════════════════════
    
    /* 配方查找 */
    private DecayRecipe searchRecipe(ServerLevel level, BlockPos targetPos, BlockState targetState) {
        BlockPos flowerPos = getEffectivePos();
        
        List<DecayRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DECAY.get());

        for (DecayRecipe recipe : allRecipes) {
            if (!recipe.matchesLocation(level, flowerPos)) {
                continue;
            }
            if (!recipe.matchesState(targetState)) {
                continue;
            }
            if (recipe.outputState() == targetState) {
                continue;
            }
            return recipe;
        }
        return null;
    }

    /* 是否播放音效  */
    private void playSoulEscape(ServerLevel level) {
        level.playSound(null, getEffectivePos(), SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /* 对比快照，变化才保存并同步  */
    private void saveAndSyncIfChanged() {
        if (Arrays.equals(positionProgress, lastSnapshot)) {
            return;
        }
        System.arraycopy(positionProgress, 0, lastSnapshot, 0, NEIGHBOR_OFFSETS.length);
        setChanged();
        sync();
    }
    
    // ══════════════════════════════════════════════════════════════
    //  NBT 读写
    // ══════════════════════════════════════════════════════════════
    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);
        tag.putInt(TAG_POSITION, rotationIndex);
        tag.putIntArray(TAG_PROGRESS, positionProgress);
    }

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);
        rotationIndex = tag.getInt(TAG_POSITION);

        int[] loaded = tag.getIntArray(TAG_PROGRESS);
        if (loaded.length == NEIGHBOR_OFFSETS.length) {
            System.arraycopy(loaded, 0, positionProgress, 0, loaded.length);
        }
        System.arraycopy(positionProgress, 0, lastSnapshot, 0, NEIGHBOR_OFFSETS.length);
    }

    // ══════════════════════════════════════════════════════════════
    //  魔力与范围
    // ══════════════════════════════════════════════════════════════
    public int getColor() {
        return 0x5A2A6B;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), 1); // 3x3
    }
}