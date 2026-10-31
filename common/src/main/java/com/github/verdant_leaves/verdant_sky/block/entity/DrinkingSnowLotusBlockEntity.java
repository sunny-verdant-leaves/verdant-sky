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
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import java.util.List;

public class DrinkingSnowLotusBlockEntity extends GeneratingFlowerBlockEntity {
    
    public static final TagKey<Item> FUEL_TAG = TagKey.create(
        Registries.ITEM,
        new ResourceLocation("verdant_sky", "drinking_snow_lotus_fuel")
    );

    private static final String TAG_WORK_TICKS = "workTicks";
    private static final int START_CONSUME_EVENT = 0; // 自定义方块事件 ID

    //  配置缓存
    private final int maxWorkTicks;
    private final int normalAmount;
    private final int normalInterval;
    private final int coldAmount;
    private final int coldInterval;
    private final int radius;
    
    private final int snowballTicks;
    private final int snowTicks;
    private final int snowBlockTicks;

    private int workTicks = 0;

    public DrinkingSnowLotusBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRINKING_SNOW_LOTUS.get(), pos, state);
        
        this.maxWorkTicks = VerdantSkyConfig.drinkingSnowLotusMaxWorkTicks();
        this.normalAmount = VerdantSkyConfig.drinkingSnowLotusNormalManaAmount();
        this.normalInterval = VerdantSkyConfig.drinkingSnowLotusNormalManaInterval();
        this.coldAmount = VerdantSkyConfig.drinkingSnowLotusColdManaAmount();
        this.coldInterval = VerdantSkyConfig.drinkingSnowLotusColdManaInterval();
        this.radius = VerdantSkyConfig.drinkingSnowLotusRadius();
        
        this.snowballTicks = VerdantSkyConfig.drinkingSnowLotusSnowballWorkTicks();
        this.snowTicks = VerdantSkyConfig.drinkingSnowLotusSnowLayerWorkTicks();
        this.snowBlockTicks = VerdantSkyConfig.drinkingSnowLotusSnowBlockWorkTicks();
    }

    // ══════════════════════════════════════════════════════════════
    //  核心逻辑
    // ══════════════════════════════════════════════════════════════
    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null) return;

        // 判断环境
        boolean coldBiome = isColdBiome();
        boolean snowing = isSnowingHere();
        boolean isFreeWorking = coldBiome && snowing; // 免费工作状态

        // 客户端逻辑：工作粒子
        if (level.isClientSide) {
            if ((workTicks > 0 || isFreeWorking) && level.random.nextInt(12) == 0) {
                double x = worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
                double y = worldPosition.getY() + 0.3 + level.random.nextDouble() * 0.5;
                double z = worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
                level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0, 0.02, 0);
            }
            return; // 客户端不执行产魔逻辑
        }

        // 扫描掉落物
        if (ticksExisted % 20 == 0) {
            if (workTicks < maxWorkTicks && getMana() < getMaxMana()) {
                tryConsumeDroppedFuel();
            }
        }

        if (workTicks <= 0 && !isFreeWorking) {
            return;
        }

        int baseAmount = coldBiome ? coldAmount : normalAmount;
        int interval = coldBiome ? coldInterval : normalInterval;

        // 生产魔力
        if (ticksExisted % interval == 0) {
            addMana(baseAmount);
        }

        // 扣除工作时间
        if (!isFreeWorking) {
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

                    // 位置偏移矫正
                    Vec3 offset = getBlockState().getOffset(level, worldPosition);
                    double targetX = worldPosition.getX() + 0.5 + offset.x;
                    double targetY = worldPosition.getY() + 0.5 + offset.y;
                    double targetZ = worldPosition.getZ() + 0.5 + offset.z;

                    // 在物品位置生成粒子并飞向花朵
                    for (int i = 0; i < 5; i++) {
                        level.addParticle(ParticleTypes.SNOWFLAKE, 
                            ex, ey, ez, 
                            (level.random.nextDouble() - 0.5) * 0.1, 
                            level.random.nextDouble() * 0.1, 
                            (level.random.nextDouble() - 0.5) * 0.1
                        );
                    }
                    level.addParticle(ParticleTypes.END_ROD, 
                        ex, ey, ez, 
                        (targetX - ex) * 0.1,
                        (targetY - ey) * 0.1,
                        (targetZ - ez) * 0.1
                    );
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

        AABB area = new AABB(
            worldPosition.getX() - radius, worldPosition.getY() - radius, worldPosition.getZ() - radius,
            worldPosition.getX() + radius + 1, worldPosition.getY() + radius + 1, worldPosition.getZ() + radius + 1
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);
        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty() || !stack.is(FUEL_TAG)) continue;

            int ticksToAdd = getWorkTicksForItem(stack);
            if (ticksToAdd <= 0) continue;

            // 记录实体 ID 以便向客户端广播事件
            int entityId = itemEntity.getId();

            // 消耗与同步
            if (stack.getCount() > 1) {
                ItemStack newStack = stack.copy();
                newStack.shrink(1);
                itemEntity.setItem(newStack);
            } else {
                itemEntity.setItem(ItemStack.EMPTY); 
            }
            workTicks = Math.min(maxWorkTicks, workTicks + ticksToAdd);

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
            return snowballTicks;
        } else if (stack.is(Items.SNOW)) {
            return snowTicks;
        } else if (stack.is(Items.SNOW_BLOCK)) {
            return snowBlockTicks;
        }
        return snowballTicks;
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
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_WORK_TICKS, workTicks);
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
            radius
        );
    }
}