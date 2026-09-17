package com.github.verdant_leaves.verdant_sky.mixin;

import java.util.function.BiFunction;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.lib.LibBlockNames;

import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

@Mixin(value = BotaniaBlockEntities.class, remap = false)
public class BotaniaBlockEntitiesMixin {

    @ModifyArg(
        method = "<clinit>",
        at = @At(value = "INVOKE",
            target = "Lvazkii/botania/common/block/block_entity/BotaniaBlockEntities;type(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/BiFunction;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/entity/BlockEntityType;",
            remap = true),
        index = 2,
        require = 0
    )
    private static <T extends BlockEntity> Block[] addVerdantPool(
            ResourceLocation id,
            BiFunction<BlockPos, BlockState, T> func,
            Block... blocks) {
        if (id.equals(prefix(LibBlockNames.POOL))) {
            blocks = ArrayUtils.add(blocks, ModBlocks.VERDANT_DILUTED_POOL.get());
        }
        return blocks;
    }
}