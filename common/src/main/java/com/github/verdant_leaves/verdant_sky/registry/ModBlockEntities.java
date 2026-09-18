package com.github.verdant_leaves.verdant_sky.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.block.entity.VerdantPoolBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<VerdantPoolBlockEntity>> VERDANT_POOL =
        BLOCK_ENTITIES.register("verdant_pool", () ->
            BlockEntityType.Builder.of(VerdantPoolBlockEntity::new,
                ModBlocks.LIVINGWOOD_POOL.get()
            ).build(null)
        );

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}