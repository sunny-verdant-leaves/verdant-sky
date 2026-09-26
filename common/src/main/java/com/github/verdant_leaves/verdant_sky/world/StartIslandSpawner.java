package com.github.verdant_leaves.verdant_sky.world;

import dev.architectury.platform.Platform;
import dev.architectury.event.events.common.LifecycleEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class StartIslandSpawner {

    private static final ResourceLocation VOID_NOISE_SETTINGS =
        new ResourceLocation("verdant_sky", "void/overworld");

    private static final long NEW_WORLD_GAME_TIME_THRESHOLD = 20;
    private static final int XZ_OFFSET = 8;

    /** 下界起始岛的默认 Y 坐标（下界岩顶上方） */
    private static final int NETHER_TARGET_Y = 128;

    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(StartIslandSpawner::onServerStarted);
    }

    private static void onServerStarted(MinecraftServer server) {
        // ── 主世界 ──
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            spawnStartIslandOverworld(server, overworld);
        }

        // ── 下界 ──
        ServerLevel nether = server.getLevel(Level.NETHER);
        if (nether != null) {
            spawnStartIslandNether(server, nether);
        }
    }

    // ─────────────────────────────────────────────
    // 主世界
    // ─────────────────────────────────────────────

    private static void spawnStartIslandOverworld(MinecraftServer server, ServerLevel level) {
        StartIslandData data = StartIslandData.get(level);
        if (data.isGenerated()) return;

        if (!isMyWorldPreset(level)) {
            data.markGenerated();
            return;
        }
        if (level.getGameTime() > NEW_WORLD_GAME_TIME_THRESHOLD) {
            data.markGenerated();
            return;
        }

        BlockPos spawnPos = level.getSharedSpawnPos();
        RandomSource random = RandomSource.create(level.getSeed());
        int offsetX = Mth.nextInt(random, -XZ_OFFSET, XZ_OFFSET);
        int offsetZ = Mth.nextInt(random, -XZ_OFFSET, XZ_OFFSET);

        int targetX = spawnPos.getX() + offsetX;
        int targetY = spawnPos.getY();
        int targetZ = spawnPos.getZ() + offsetZ;

        BlockPos targetPos = new BlockPos(targetX, targetY, targetZ);
        Holder<Biome> biome = level.getBiome(targetPos);

        // 放置结构
        String structureId = pickOverworldStructureForBiome(biome);
        String structureCommand = String.format(
            "place structure %s %d %d %d",
            structureId, targetX, targetY, targetZ
        );
        server.getCommands().performPrefixedCommand(
            server.createCommandSourceStack().withSuppressedOutput(),
            structureCommand
        );

        System.out.println("[Verdant Sky] Start island (" + structureId + ") at " + targetX + " " + targetY + " " + targetZ);

        // 放置地物（featureIndex = 0）
        if (biome.is(biomeTag("start_flowers"))) {
            placeFeature(level, targetX, targetZ, "minecraft:forest_flowers", 0);
        }

        data.markGenerated();
    }

    // ─────────────────────────────────────────────
    // 下界
    // ─────────────────────────────────────────────

    private static void spawnStartIslandNether(MinecraftServer server, ServerLevel level) {
        StartIslandData data = StartIslandData.get(level);
        if (data.isGenerated()) return;

        if (level.getGameTime() > NEW_WORLD_GAME_TIME_THRESHOLD) {
            data.markGenerated();
            return;
        }

        BlockPos spawnPos = level.getSharedSpawnPos();
        // 用与主世界不同的种子组合，避免结果与主世界完全相同
        RandomSource random = RandomSource.create(level.getSeed() ^ 0x9E3779B97F4A7C15L);
        int offsetX = Mth.nextInt(random, -XZ_OFFSET, XZ_OFFSET);
        int offsetZ = Mth.nextInt(random, -XZ_OFFSET, XZ_OFFSET);

        int targetX = spawnPos.getX() + offsetX;
        int targetY = NETHER_TARGET_Y;
        int targetZ = spawnPos.getZ() + offsetZ;

        BlockPos targetPos = new BlockPos(targetX, targetY, targetZ);
        Holder<Biome> biome = level.getBiome(targetPos);

        // 放置结构
        String structureId = pickNetherStructureForBiome(biome);
        String structureCommand = String.format(
            "place structure %s %d %d %d",
            structureId, targetX, targetY, targetZ
        );
        server.getCommands().performPrefixedCommand(
            server.createCommandSourceStack().withSuppressedOutput(),
            structureCommand
        );

        System.out.println("[Verdant Sky] Nether start island (" + structureId + ") at " + targetX + " " + targetY + " " + targetZ);

        // 放置地物（featureIndex = 1，与主世界区分）
        if (biome.is(biomeTag("start_flowers"))) {
            if (Platform.isModLoaded("botania")) {
                placeFeature(level, targetX, targetZ, "botania:mystical_mushrooms", 1);
            } else {
                placeFeature(level, targetX, targetZ, "minecraft:patch_brown_mushroom", 1);
            }
        }

        data.markGenerated();
    }

    // ─────────────────────────────────────────────
    // 通用地物放置（可控随机）
    // ─────────────────────────────────────────────

    /**
     * 用 MOTION_BLOCKING_NO_LEAVES 高度图定位 Y 坐标，
     * 并用世界种子构造可复现的 WorldgenRandom 放置指定 PlacedFeature。
     *
     * @param featureId    PlacedFeature 的完整 ID，如 "minecraft:forest_flowers"
     * @param featureIndex 用于区分不同地物的种子索引（主世界=0，下界=1）
     */
    private static void placeFeature(ServerLevel level, int x, int z, String featureId, int featureIndex) {
        // 1. 解析 featureId
        ResourceLocation loc = parseResourceLocation(featureId);
        ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, loc);

        var registry = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> feature = registry.getHolderOrThrow(key);

        // 2. 用 MOTION_BLOCKING_NO_LEAVES 计算放置起点
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos placePos = new BlockPos(x, y, z);

        // 3. 构造可控随机源
        long seed = level.getSeed();
        WorldgenRandom random = new WorldgenRandom(RandomSource.create(seed));
        random.setFeatureSeed(
            seed,
            featureIndex,
            GenerationStep.Decoration.VEGETAL_DECORATION.ordinal()
        );

        // 4. 执行放置
        boolean placed = feature.value().place(
            level,
            level.getChunkSource().getGenerator(),
            random,
            placePos
        );

        System.out.println("[Verdant Sky] Feature " + featureId  + " at " + placePos + " (success=" + placed + ")");
    }

    private static ResourceLocation parseResourceLocation(String id) {
        int i = id.indexOf(':');
        if (i >= 0) {
            return new ResourceLocation(id.substring(0, i), id.substring(i + 1));
        }
        return new ResourceLocation("minecraft", id);
    }

    // ─────────────────────────────────────────────
    // 结构 / 地物选择
    // ─────────────────────────────────────────────

    private static String pickOverworldStructureForBiome(Holder<Biome> biome) {
        if (biome.is(biomeTag("start_island_jungle")))   return "verdant_sky:overworld/start_island_jungle";
        if (biome.is(biomeTag("start_island_dark_oak"))) return "verdant_sky:overworld/start_island_dark_oak";
        if (biome.is(biomeTag("start_island_birch")))    return "verdant_sky:overworld/start_island_birch";
        if (biome.is(biomeTag("start_island_acacia")))   return "verdant_sky:overworld/start_island_acacia";
        if (biome.is(biomeTag("start_island_spruce")))   return "verdant_sky:overworld/start_island_spruce";
        if (biome.is(biomeTag("start_island_oak")))      return "verdant_sky:overworld/start_island_oak";
        if (biome.is(biomeTag("start_island_forest")))   return "verdant_sky:start_island_forest";
        return "verdant_sky:start_island";
    }

    private static String pickNetherStructureForBiome(Holder<Biome> biome) {
        if (biome.is(biomeTag("start_island_crimson"))) return "verdant_sky:the_nether/start_island_crimson";
        if (biome.is(biomeTag("start_island_warped")))  return "verdant_sky:the_nether/start_island_warped";
        if (biome.is(biomeTag("start_island_nether")))  return "verdant_sky:the_nether/start_island_nether";
        return "verdant_sky:start_island";
    }

    // ─────────────────────────────────────────────
    // 工具
    // ─────────────────────────────────────────────

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