package com.github.verdant_leaves.verdant_sky.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.block.Block;

import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.registries.RegistrySupplier;

import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

@Environment(EnvType.CLIENT)
public final class VerdantSkyClient {

    private VerdantSkyClient() {}

    public static void init() {
        registerCauldronWaterColor();
    }

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