package com.github.verdant_leaves.verdant_sky.block.entity;

import java.util.Arrays;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.client.fx.SparkleParticleData;

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
    private static final int PROGRESS_REQUIRED = 20 * 60;   // 1200 ticks = 60秒

    private static final String TAG_POSITION = "positionAt";
    private static final String TAG_PROGRESS = "progress";
    private static final int RECIPE_COMPLETE_EVENT = 0;     // 自定义方块事件 ID

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
            serverLevel.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, targetPos, Block.getId(matchedRecipe.outputState()));
            serverLevel.gameEvent(null, GameEvent.BLOCK_CHANGE, targetPos);
            serverLevel.blockEvent(getBlockPos(), getBlockState().getBlock(), RECIPE_COMPLETE_EVENT, rotationIndex);
            
            sync();
        }

        saveAndSyncIfChanged();
    }
    
    // ══════════════════════════════════════════════════════════════
    //  客户端
    // ══════════════════════════════════════════════════════════════

    /* 客户端粒子 */
    private void emitProgressParticles() {
        for (int i = 0; i < NEIGHBOR_OFFSETS.length; i++) {
            if (positionProgress[i] <= 0) {
                continue;
            }
            BlockPos coords = getEffectivePos().offset(NEIGHBOR_OFFSETS[i]);
            SparkleParticleData data = SparkleParticleData.sparkle((float) Math.random(), 0.51F, 0.11F, 0.65F, 5);
            level.addParticle(data, coords.getX() + Math.random(), coords.getY() + Math.random(), coords.getZ() + Math.random(), 0, 0, 0);
        }
    }

    @Override
    public boolean triggerEvent(int type, int param) {
        switch (type) {
            case RECIPE_COMPLETE_EVENT: {
                if (level != null && level.isClientSide) {
                    BlockPos coords = getEffectivePos().offset(NEIGHBOR_OFFSETS[param]);
                    
                    for (int i = 0; i < 15; i++) {
                        double x = coords.getX() + 0.5 + (Math.random() - 0.5) * 0.8;
                        double y = coords.getY() + 0.5 + Math.random() * 0.8;
                        double z = coords.getZ() + 0.5 + (Math.random() - 0.5) * 0.8;

                        level.addParticle(ParticleTypes.SCULK_SOUL, x, y, z, 0, 0.01, 0);
                    }
                }
                return true;
            }
            default: {
                return super.triggerEvent(type, param);
            }
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
        return 0x831BA7;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), 1); // 3x3
    }
}