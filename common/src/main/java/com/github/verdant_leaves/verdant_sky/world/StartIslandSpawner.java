package com.github.verdant_leaves.verdant_sky.world;

import dev.architectury.event.events.common.LifecycleEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

/**
 * 在世界启动时，检查是否需要生成初始岛屿。
 * 只在满足以下条件时生成：
 *   1. 主世界
 *   2. 使用 verdant_sky:void 噪声设置（玩家的世界预设）
 *   3. 尚未生成过（SavedData 标记）
 *   4. 是新存档（GameTime 很小）
 */
public class StartIslandSpawner {

    private static final String STRUCTURE_ID = "verdant_sky:start_island";

    /** 世界预设使用的噪声设置 */
    private static final ResourceLocation VOID_NOISE_SETTINGS = new ResourceLocation("verdant_sky", "void");

    /** 新存档的 GameTime 阈值。超过这个值就认为是旧存档。 */
    private static final long NEW_WORLD_GAME_TIME_THRESHOLD = 20;

    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(StartIslandSpawner::onServerStarted);
    }

    private static void onServerStarted(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        // ── 1. 已生成过 → 跳过 ──
        StartIslandData data = StartIslandData.get(overworld);
        if (data.isGenerated()) {
            return;
        }

        // ── 2. 不是我的世界预设 → 标记为"已检查"，跳过 ──
        if (!isMyWorldPreset(overworld)) {
            data.markGenerated();
            return;
        }

        // ── 3. 旧存档 → 标记为"已检查"，跳过 ──
        if (overworld.getGameTime() > NEW_WORLD_GAME_TIME_THRESHOLD) {
            data.markGenerated();
            return;
        }

        // ── 4. 生成 ──
        BlockPos spawnPos = overworld.getSharedSpawnPos();

        String command = String.format(
            "place structure %s %d %d %d",
            STRUCTURE_ID,
            spawnPos.getX(),
            spawnPos.getY(),
            spawnPos.getZ()
        );

        server.getCommands().performPrefixedCommand(
            server.createCommandSourceStack().withSuppressedOutput(),
            command
        );

        data.markGenerated();

        System.out.println("[Verdant Sky] Start island generated at " + spawnPos);
    }

    /**
     * 检查当前维度是否使用了我们世界预设中定义的噪声设置。
     * 这样即使用户在主世界放岛，原版世界也不会触发。
     */
    private static boolean isMyWorldPreset(ServerLevel level) {
        var generator = level.getChunkSource().getGenerator();
        if (!(generator instanceof NoiseBasedChunkGenerator noiseGen)) {
            return false;
        }
        var key = noiseGen.generatorSettings().unwrapKey();
        if (key.isEmpty()) {
            return false;
        }
        return key.get().location().equals(VOID_NOISE_SETTINGS);
    }
}