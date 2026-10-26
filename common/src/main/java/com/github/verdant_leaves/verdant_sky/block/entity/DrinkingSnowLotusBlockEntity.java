package com.github.verdant_leaves.verdant_sky.block.entity;

import com.github.verdant_leaves.verdant_sky.VerdantSkyConfig;
import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModSoundEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import java.util.List;

public class DrinkingSnowLotusBlockEntity extends GeneratingFlowerBlockEntity {
    
    public static final TagKey<Item> FUEL_TAG = TagKey.create(
        Registries.ITEM,
        new ResourceLocation("verdant_sky", "drinking_snow_lotus_fuel")
    );

    private static final String TAG_WORK_TICKS = "WorkTicks";
    private static final String TAG_MANA_BUFFER = "ManaBuffer";
    private static final String TAG_SCAN_COOLDOWN = "ScanCooldown";

    private static final int START_CONSUME_EVENT = 0; // 自定义方块事件 ID

    private int workTicks = 0;
    private double manaBuffer = 0.0;
    private int scanCooldown = 0;
    private static final int SCAN_INTERVAL = 20; // 扫描掉落物的时间间隔，单位：tick

    public DrinkingSnowLotusBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRINKING_SNOW_LOTUS.get(), pos, state);
    }

    // ══════════════════════════════════════════════════════════════
    //  核心逻辑
    // ══════════════════════════════════════════════════════════════
    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null) return;

        // 客户端逻辑：工作粒子
        if (level.isClientSide) {
            if (workTicks > 0 && level.random.nextInt(8) == 0) {
                double x = worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
                double y = worldPosition.getY() + 0.3 + level.random.nextDouble() * 0.5;
                double z = worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
                level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0, 0.02, 0);
            }
            return; // 客户端不执行产魔逻辑
        }

        // 扫描掉落物
        scanCooldown++;
        if (scanCooldown >= SCAN_INTERVAL) {
            scanCooldown = 0;
            tryConsumeDroppedFuel();
        }

        // 如果没有工作时间，直接返回
        if (workTicks <= 0) return;

        boolean coldBiome = isColdBiome();
        boolean snowing = isSnowingHere();
        boolean consumeWork = !(coldBiome && snowing);
        
        double baseManaPerTick = coldBiome
            ? VerdantSkyConfig.drinkingSnowLotusColdManaPerTick()
            : VerdantSkyConfig.drinkingSnowLotusNormalManaPerTick();

        // 计算随机波动
        double variance = VerdantSkyConfig.drinkingSnowLotusManaVariance();
        double randomOffset = (level.random.nextDouble() * 2.0 - 1.0) * variance;
        double actualManaPerTick = baseManaPerTick * (1.0 + randomOffset);
        
        if (actualManaPerTick < 0) actualManaPerTick = 0;

        // 生产魔力
        manaBuffer += actualManaPerTick;
        int manaToAdd = (int) Math.floor(manaBuffer);
        if (manaToAdd > 0) {
            addMana(manaToAdd);
            manaBuffer -= manaToAdd;
        }

        // 扣除工作时间
        if (consumeWork) {
            workTicks--;
        }
    }
    
    // ══════════════════════════════════════════════════════════════
    //  客户端事件
    // ══════════════════════════════════════════════════════════════
    @Override
    public boolean triggerEvent(int event, int param) {
        if (event == START_CONSUME_EVENT) {
            if (level != null && level.isClientSide) {
                // 根据传入的实体 ID 找到即将消失的掉落物
                Entity entity = level.getEntity(param);
                if (entity != null) {
                    double ex = entity.getX();
                    double ey = entity.getY() + 0.1;
                    double ez = entity.getZ();

                    // 在物品位置生成粒子并飞向花朵
                    for (int i = 0; i < 8; i++) {
                        level.addParticle(ParticleTypes.SNOWFLAKE, 
                            ex, ey, ez, 
                            (level.random.nextDouble() - 0.5) * 0.1, 
                            level.random.nextDouble() * 0.1, 
                            (level.random.nextDouble() - 0.5) * 0.1
                        );
                    }
                    for (int i = 0; i < 4; i++) {
                        level.addParticle(ParticleTypes.END_ROD, 
                            ex, ey, ez, 
                            (worldPosition.getX() + 0.5 - ex) * 0.1,
                            (worldPosition.getY() + 0.5 - ey) * 0.1,
                            (worldPosition.getZ() + 0.5 - ez) * 0.1
                        );
                    }
                }
            }
            return true;
        }
        return super.triggerEvent(event, param);
    }

    // ══════════════════════════════════════════════════════════════
    //  辅助方法
    // ══════════════════════════════════════════════════════════════
    
    /* 尝试消耗掉落物 */
    private void tryConsumeDroppedFuel() {
        if (level == null || level.isClientSide()) return;

        int r = VerdantSkyConfig.drinkingSnowLotusRadius();
        AABB area = new AABB(
            worldPosition.getX() - r, worldPosition.getY() - r, worldPosition.getZ() - r,
            worldPosition.getX() + r + 1, worldPosition.getY() + r + 1, worldPosition.getZ() + r + 1
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);
        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty() || !stack.is(FUEL_TAG)) continue;

            int ticksToAdd = getWorkTicksForItem(stack);
            if (ticksToAdd <= 0) continue;

            int maxTicks = VerdantSkyConfig.drinkingSnowLotusMaxWorkTicks();
            if (workTicks >= maxTicks) break;

            // 记录实体 ID 以便向客户端广播事件
            int entityId = itemEntity.getId();

            // 消耗与同步
            if (stack.getCount() > 1) {
                ItemStack newStack = stack.copy();
                newStack.shrink(1);
                itemEntity.setItem(newStack);
            } else {
                itemEntity.discard();
            }
            workTicks = Math.min(maxTicks, workTicks + ticksToAdd);

            // 播放音效
            level.playSound(null, worldPosition, ModSoundEvents.DRINKING_SNOW_LOTUS_CONSUME.get(), SoundSource.BLOCKS, 1.0f, 1.2f + level.random.nextFloat() * 0.2f);

            // 向周围客户端广播事件，并传入实体 ID
            level.blockEvent(getBlockPos(), getBlockState().getBlock(), START_CONSUME_EVENT, entityId);

            setChanged();
            sync();
            break; 
        }
    }

    /* 获取掉落物对应的工作时长 */
    private int getWorkTicksForItem(ItemStack stack) {
        if (stack.is(Items.SNOWBALL)) {
            return VerdantSkyConfig.drinkingSnowLotusSnowballWorkTicks();
        } else if (stack.is(Items.SNOW)) {
            return VerdantSkyConfig.drinkingSnowLotusSnowLayerWorkTicks();
        } else if (stack.is(Items.SNOW_BLOCK)) {
            return VerdantSkyConfig.drinkingSnowLotusSnowBlockWorkTicks();
        }
        return VerdantSkyConfig.drinkingSnowLotusSnowballWorkTicks();
    }

    /* 是否为寒冷生物群系 */
    private boolean isColdBiome() {
        if (level == null) return false;
        Biome biome = level.getBiome(worldPosition).value();
        return biome.getBaseTemperature() <= 0.15f;
    }
    
    /* 是否正在降雪  */
    private boolean isSnowingHere() {
        if (level == null) return false;
        return level.isRainingAt(worldPosition.above())
            && level.getBiome(worldPosition).value().coldEnoughToSnow(worldPosition);
    }

    // ══════════════════════════════════════════════════════════════
    //  NBT 读写
    // ══════════════════════════════════════════════════════════════
    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        workTicks = cmp.getInt(TAG_WORK_TICKS);
        manaBuffer = cmp.getDouble(TAG_MANA_BUFFER);
        scanCooldown = cmp.getInt(TAG_SCAN_COOLDOWN);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_WORK_TICKS, workTicks);
        cmp.putDouble(TAG_MANA_BUFFER, manaBuffer);
        cmp.putInt(TAG_SCAN_COOLDOWN, scanCooldown);
    }

    // ══════════════════════════════════════════════════════════════
    //  魔力与范围
    // ══════════════════════════════════════════════════════════════
    @Override
    public int getMaxMana() {
        return 10000;
    }

    @Override
    public int getColor() {
        return 0x88CCFF;
    }

    @Nullable
    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(
            getEffectivePos(),
            VerdantSkyConfig.drinkingSnowLotusRadius()
        );
    }
}