package com.github.verdant_leaves.verdant_sky.world;

import dev.architectury.event.events.common.LifecycleEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
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

    private static final ResourceLocation VOID_NOISE_SETTINGS = new ResourceLocation("verdant_sky", "void");

    /** 新存档的 GameTime 阈值。超过这个值就认为是旧存档。 */
    private static final long NEW_WORLD_GAME_TIME_THRESHOLD = 20;
    private static final int XZ_OFFSET = 8;

    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(StartIslandSpawner::onServerStarted);
    }

    private static void onServerStarted(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        StartIslandData data = StartIslandData.get(overworld);
        if (data.isGenerated()) return;

        if (!isMyWorldPreset(overworld)) {
            data.markGenerated();
            return;
        }
        if (overworld.getGameTime() > NEW_WORLD_GAME_TIME_THRESHOLD) {
            data.markGenerated();
            return;
        }

        // ── XZ 偏移（按世界种子） ──
        BlockPos spawnPos = overworld.getSharedSpawnPos();
        RandomSource random = RandomSource.create(overworld.getSeed());
        int offsetX = Mth.nextInt(random, -XZ_OFFSET, XZ_OFFSET);
        int offsetZ = Mth.nextInt(random, -XZ_OFFSET, XZ_OFFSET);

        int targetX = spawnPos.getX() + offsetX;
        int targetY = spawnPos.getY();
        int targetZ = spawnPos.getZ() + offsetZ;

        // ── 按生物群系选模板 ──
        String templateId = pickTemplateForBiome(overworld, targetX, targetZ);

        // ── 生成 ──
        String command = String.format(
            "place template %s %d %d %d",
            templateId, targetX, targetY, targetZ
        );

        server.getCommands().performPrefixedCommand(
            server.createCommandSourceStack().withSuppressedOutput(),
            command
        );

        data.markGenerated();

        System.out.println("[Verdant Sky] Start island (" + templateId + ") at "
            + targetX + " " + targetY + " " + targetZ);
    }
    /**
     * 按自定义生物群系标签选择初始岛模板。
     * 标签文件在 data/verdant_sky/tags/worldgen/biome/ 下。
     */
    private static String pickTemplateForBiome(ServerLevel level, int x, int z) {
        BlockPos pos = new BlockPos(x, level.getMinBuildHeight(), z);
        Holder<Biome> biome = level.getBiome(pos);

        // ── 第一层：独特标签（每个树种一个专属初始岛） ──
        if (biome.is(biomeTag("start_island_jungle")))   return "verdant_sky:start_island_jungle";
        if (biome.is(biomeTag("start_island_dark_oak"))) return "verdant_sky:start_island_dark_oak";
        if (biome.is(biomeTag("start_island_birch")))    return "verdant_sky:start_island_birch";
        if (biome.is(biomeTag("start_island_acacia")))   return "verdant_sky:start_island_acacia";
        if (biome.is(biomeTag("start_island_spruce")))   return "verdant_sky:start_island_spruce";
        if (biome.is(biomeTag("start_island_oak")))      return "verdant_sky:start_island_oak";

        // ── 第二层：森林兜底 ──
        if (biome.is(biomeTag("start_island_forest")))   return "verdant_sky:start_island_forest";
        
        // ── 第三层：默认兜底 ──
        return "verdant_sky:start_island";
    }

    private static TagKey<Biome> biomeTag(String path) {
        return TagKey.create(Registries.BIOME,
            new ResourceLocation("verdant_sky", path));
    }

    private static boolean isMyWorldPreset(ServerLevel level) {
        var generator = level.getChunkSource().getGenerator();
        if (!(generator instanceof NoiseBasedChunkGenerator noiseGen)) {
            return false;
        }
        var key = noiseGen.generatorSettings().unwrapKey();
        if (key.isEmpty()) return false;
        return key.get().location().equals(VOID_NOISE_SETTINGS);
    }
}