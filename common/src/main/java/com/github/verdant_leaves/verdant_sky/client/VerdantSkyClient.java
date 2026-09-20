package com.github.verdant_leaves.verdant_sky.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.registries.RegistrySupplier;

import vazkii.botania.client.render.block_entity.SpecialFlowerBlockEntityRenderer;

import com.github.verdant_leaves.verdant_sky.registry.ModBlockEntities;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

@Environment(EnvType.CLIENT)
public final class VerdantSkyClient {

    private VerdantSkyClient() {}

    public static void init() {
        registerDecayFlowerRenderType();
        registerDecayFlowerRenderer();
        registerCauldronWaterColor();
    }

    // ══════════════════════════════════════════════════════════════
    //  腐朽花的渲染层（透明）
    // ══════════════════════════════════════════════════════════════

    /**
     * 使用 cutout 渲染层，让十字模型（cross）的透明部分正确显示。
     * Forge 端其实可以从模型自动推断，但 Fabric 端必须显式声明，
     * 走 Architectury 的 RenderTypeRegistry 可以两端通吃。
     */
    private static void registerDecayFlowerRenderType() {
        RenderTypeRegistry.register(
            RenderType.cutout(),
            ModBlocks.DECAY_FLOWER.get()
        );
    }

    // ══════════════════════════════════════════════════════════════
    //  腐朽花的范围渲染器
    // ══════════════════════════════════════════════════════════════

    /**
     * 注册 Botania 的花渲染器，让窥魔之镜 / 森林法杖绑定提示
     * 能调用 DecayFlowerBlockEntity.getRadius() 并绘制范围框。
     */
    private static void registerDecayFlowerRenderer() {
        BlockEntityRendererRegistry.register(
            ModBlockEntities.DECAY_FLOWER.get(),
            ctx -> new SpecialFlowerBlockEntityRenderer<>(ctx)
        );
    }

    // ══════════════════════════════════════════════════════════════
    //  炼药锅水色
    // ══════════════════════════════════════════════════════════════

    private static void registerCauldronWaterColor() {
        if (ModBlocks.WATER_CAULDRONS.isEmpty()) {
            return;
        }

        Block[] waterCauldrons = ModBlocks.WATER_CAULDRONS.values().stream()
                .map(RegistrySupplier::get)
                .toArray(Block[]::new);

        // tintIndex 0 对应模型里的水面
        // 与原版水锅一致，使用生物群系水色
        BlockColor waterColor = (state, level, pos, tintIndex) -> {
            if (tintIndex != 0) {
                return -1;
            }
            if (level == null || pos == null) {
                return 0x3F76E4;  // 无世界上下文时，回退到默认水色
            }
            return BiomeColors.getAverageWaterColor(level, pos);
        };

        ColorHandlerRegistry.registerBlockColors(waterColor, waterCauldrons);
    }
}