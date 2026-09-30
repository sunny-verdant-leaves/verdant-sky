package com.github.verdant_leaves.verdant_sky.registry;

import java.util.HashSet;
import java.util.Set;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.block.entity.DecayAgapanthusBlockEntity;
import com.github.verdant_leaves.verdant_sky.mixin.BlockEntityTypeAccessor;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;

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
        // 注意：injectIntoBotania() 不在这里调用
        // 它必须在 LifecycleEvent.SETUP 阶段执行，那时所有注册都已完成
    }

    /**
     * 往 Botania 已有的 BE 类型里追加我们的方块。
     * 必须在 LifecycleEvent.SETUP 阶段调用，因为那时注册已完成。
     */
    public static void injectIntoBotania() {
        if (!Platform.isModLoaded("botania")) return;

        // ── 追加木花药台到 ALTAR ──
        BlockEntityType<?> altar = BotaniaBlockEntities.ALTAR;
        Set<Block> altarBlocks = new HashSet<>(
            ((BlockEntityTypeAccessor) (Object) altar).getValidBlocks()
        );
        altarBlocks.add(ModBlocks.APOTHECARY_WOODEN.get());
        ((BlockEntityTypeAccessor) (Object) altar).setValidBlocks(altarBlocks);

        // ── 追加活木魔力池到 POOL ──
        BlockEntityType<?> pool = BotaniaBlockEntities.POOL;
        Set<Block> poolBlocks = new HashSet<>(
            ((BlockEntityTypeAccessor) (Object) pool).getValidBlocks()
        );
        poolBlocks.add(ModBlocks.LIVINGWOOD_POOL.get());
        ((BlockEntityTypeAccessor) (Object) pool).setValidBlocks(poolBlocks);

        System.out.println("[Verdant Sky] Injected blocks into Botania BE types");
    }
}