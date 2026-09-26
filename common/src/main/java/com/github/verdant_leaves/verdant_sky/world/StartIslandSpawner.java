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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.Vec3;

public class StartIslandSpawner {

    private static final ResourceLocation VOID_NOISE_SETTINGS = new ResourceLocation("verdant_sky", "void/overworld");

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
        
        // 区块强加载
        ChunkPos centerChunk = new ChunkPos(targetPos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.getChunk(centerChunk.x + dx, centerChunk.z + dz);
            }
        }

        // 获取生物群系
        Holder<Biome> biome = level.getBiome(targetPos);

        // 放置结构
        String structureId = pickOverworldStructureForBiome(biome);
        String structureCommand = String.format(
            "place structure %s %d %d %d",
            structureId, targetX, targetY, targetZ
        );
        server.getCommands().performPrefixedCommand(
            server.createCommandSourceStack()
                .withLevel(level)
                .withPosition(Vec3.atCenterOf(targetPos)),
            structureCommand
        );

        System.out.println("[Verdant Sky] Start island (" + structureId + ") at " + targetX + " " + targetY + " " + targetZ);

        // 放置地物（featureIndex = 0）
        if (biome.is(biomeTag("start_flowers"))) {
            placeFeature(level, targetX, targetZ, 0, "minecraft:forest_flowers");
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
        
        // 区块强加载
        ChunkPos centerChunk = new ChunkPos(targetPos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.getChunk(centerChunk.x + dx, centerChunk.z + dz);
            }
        }

        // 获取生物群系
        Holder<Biome> biome = level.getBiome(targetPos);

        // 放置结构
        String structureId = pickNetherStructureForBiome(biome);
        String structureCommand = String.format(
            "place structure %s %d %d %d",
            structureId, targetX, targetY, targetZ
        );
        server.getCommands().performPrefixedCommand(
            server.createCommandSourceStack()
                .withLevel(level)
                .withPosition(Vec3.atCenterOf(targetPos)),
            structureCommand
        );

        System.out.println("[Verdant Sky] Nether start island (" + structureId + ") at " + targetX + " " + targetY + " " + targetZ);

        // 放置地物（featureIndex = 1，与主世界区分）
        // 用可变参数列表做优先级回退：Botania 神秘蘑菇 → 原版棕色蘑菇
        if (biome.is(biomeTag("start_flowers"))) {
            if (Platform.isModLoaded("botania")) {
                placeFeature(level, targetX, targetZ, 1,
                    "botania:mystical_mushrooms",
                    "minecraft:patch_brown_mushroom");
            } else {
                placeFeature(level, targetX, targetZ, 1,
                    "minecraft:patch_brown_mushroom");
            }
        }

        data.markGenerated();
    }

    // ─────────────────────────────────────────────
    // 通用地物放置（可控随机）
    // ─────────────────────────────────────────────

    /**
     * 用 MOTION_BLOCKING_NO_LEAVES 高度图定位 Y 坐标，
     * 并用世界种子构造可复现的 WorldgenRandom 直接放置 ConfiguredFeature。
     *
     * 与原版 /place feature 命令行为一致：直接调用 ConfiguredFeature.place()，
     * 不经过 PlacedFeature 的放置修饰器链（count / in_square / heightmap / biome）。
     *
     * @param featureIndex 用于区分不同地物的种子索引（主世界=0，下界=1）
     * @param featureIds   按优先级排列的 ConfiguredFeature ID，第一个存在的被使用
     */
    private static void placeFeature(ServerLevel level, int x, int z, int featureIndex, String... featureIds) {
        var registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);

        // 按优先级依次尝试，第一个在注册表中存在的即使用
        Holder<ConfiguredFeature<?, ?>> feature = null;
        String usedId = null;
        for (String id : featureIds) {
            ResourceLocation loc = parseResourceLocation(id);
            ResourceKey<ConfiguredFeature<?, ?>> key =
                ResourceKey.create(Registries.CONFIGURED_FEATURE, loc);
            var holder = registry.getHolder(key);
            if (holder.isPresent()) {
                feature = holder.get();
                usedId = id;
                break;
            }
        }

        if (feature == null) return;

        // 用 MOTION_BLOCKING_NO_LEAVES 计算放置起点
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos placePos = new BlockPos(x, y, z);

        // 构造可控随机源，种子完全由世界种子 + featureIndex + 装饰阶段决定
        long seed = level.getSeed();
        WorldgenRandom random = new WorldgenRandom(RandomSource.create(seed));
        random.setFeatureSeed(
            seed,
            featureIndex,
            GenerationStep.Decoration.VEGETAL_DECORATION.ordinal()
        );

        // 直接调用 ConfiguredFeature.place()，绕过 PlacedFeature 的修饰器链
        boolean placed = feature.value().place(
            level,
            level.getChunkSource().getGenerator(),
            random,
            placePos
        );

        System.out.println("[Verdant Sky] Feature " + usedId + " at " + placePos + " (success=" + placed + ")");
    }

    private static ResourceLocation parseResourceLocation(String id) {
        int i = id.indexOf(':');
        if (i >= 0) {
            return new ResourceLocation(id.substring(0, i), id.substring(i + 1));
        }
        return new ResourceLocation("minecraft", id);
    }

    // ─────────────────────────────────────────────
    // 结构选择
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