package com.github.verdant_leaves.verdant_sky.registry;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.block.entity.DecayAgapanthusBlockEntity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<DecayAgapanthusBlockEntity>> DECAY_AGAPANTHUS =
        BLOCK_ENTITIES.register("decay_agapanthus", () ->
            BlockEntityType.Builder.of(
                DecayAgapanthusBlockEntity::new,
                ModBlocks.DECAY_AGAPANTHUS.get(),
                ModBlocks.FLOATING_DECAY_AGAPANTHUS.get()
            ).build(null)
        );

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}