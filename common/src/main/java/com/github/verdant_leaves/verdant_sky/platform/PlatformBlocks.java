package com.github.verdant_leaves.verdant_sky.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

import java.util.function.Supplier;

public final class PlatformBlocks {
    private PlatformBlocks() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 跨平台方法，用于创建 SpecialFlower 方块。
     *
     * @param effect              放入炖菜时给予的药水效果
     * @param effectDuration      药水效果持续时间，单位：秒
     * @param props               方块属性
     * @param beType              方块实体类型 Supplier
     * @param shape               轮廓箱形状，null = Botania 的默认值
     * @param hasComparatorOutput 是否支持红石比较器输出
     * @return 平台对应的 SpecialFlower 方块实例
     */
    @ExpectPlatform
    public static Block createSpecialFlowerBlock(
        MobEffect effect, 
        int effectDuration, 
        BlockBehaviour.Properties props, 
        Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> beType, 
        @Nullable VoxelShape shape, 
        boolean hasComparatorOutput
    ) {
        throw new AssertionError("This method should be replaced by Architectury!");
    }

    /**
     * 跨平台方法，用于创建 SpecialFlower 方块。
     * <p>默认轮廓箱形状，无比较器输出</p>
     *
     * @param effect              放入炖菜时给予的药水效果
     * @param effectDuration      药水效果持续时间，单位：秒
     * @param props               方块属性
     * @param beType              方块实体类型 Supplier
     * @return 平台对应的特殊花方块实例
     */
    public static Block createSpecialFlowerBlock(
        MobEffect effect, 
        int effectDuration, 
        BlockBehaviour.Properties props, 
        Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> beType
    ) {
        return createSpecialFlowerBlock(effect, effectDuration, props, beType, null, false);
    }

    /**
     * 跨平台方法，用于创建 SpecialFlower 方块。
     * <p>自定义轮廓箱形状，无比较器输出</p>
     *
     * @param effect              放入炖菜时给予的药水效果
     * @param effectDuration      药水效果持续时间，单位：秒
     * @param props               方块属性
     * @param beType              方块实体类型 Supplier
     * @param shape               轮廓箱形状，null = Botania 的默认值
     * @return 平台对应的特殊花方块实例
     */
    public static Block createSpecialFlowerBlock(
        MobEffect effect, 
        int effectDuration, 
        BlockBehaviour.Properties props, 
        Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> beType, 
        @Nullable VoxelShape shape
    ) {
        return createSpecialFlowerBlock(effect, effectDuration, props, beType, shape, false);
    }

    /**
     * 跨平台方法，用于创建 SpecialFlower 方块。
     * <p>默认轮廓箱形状，自定义比较器输出</p>
     *
     * @param effect              放入炖菜时给予的药水效果
     * @param effectDuration      药水效果持续时间，单位：秒
     * @param props               方块属性
     * @param beType              方块实体类型 Supplier
     * @param hasComparatorOutput 是否支持红石比较器输出
     * @return 平台对应的特殊花方块实例
     */
    public static Block createSpecialFlowerBlock(
        MobEffect effect, 
        int effectDuration, 
        BlockBehaviour.Properties props, 
        Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> beType, 
        boolean hasComparatorOutput
    ) {
        return createSpecialFlowerBlock(effect, effectDuration, props, beType, null, hasComparatorOutput);
    }
}