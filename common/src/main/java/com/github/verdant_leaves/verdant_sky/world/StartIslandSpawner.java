package com.github.verdant_leaves.verdant_sky.world;

import dev.architectury.event.events.common.LifecycleEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StartIslandSpawner {

    private static final ResourceLocation VOID_NOISE_SETTINGS = new ResourceLocation("verdant_sky", "void/overworld");

    private static final long NEW_WORLD_GAME_TIME_THRESHOLD = 20;
    private static final int XZ_OFFSET = 8;

    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(StartIslandSpawner::onServerStarted);
    }

    private static void onServerStarted(MinecraftServer server) {
        // ── 主世界 ──
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        BlockPos overworldTarget = null;
        if (overworld != null) {
            overworldTarget = spawnStartIslandOverworld(server, overworld);
        }

        // ── 下界（传入主世界目标坐标用于换算） ──
        ServerLevel nether = server.getLevel(Level.NETHER);
        if (nether != null) {
            spawnStartIslandNether(server, nether, overworldTarget);
        }
    }

    // ─────────────────────────────────────────────
    // 主世界
    // ─────────────────────────────────────────────

    private static BlockPos spawnStartIslandOverworld(MinecraftServer server, ServerLevel level) {
        StartIslandData data = StartIslandData.get(level);
        if (data.isGenerated()) return null;

        if (!isMyWorldPreset(level)) {
            data.markGenerated();
            return null;
        }
        if (level.getGameTime() > NEW_WORLD_GAME_TIME_THRESHOLD) {
            data.markGenerated();
            return null;
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
        boolean placed = placeStructureDirectly(level, structureId, targetPos);

        if (placed) {
            // Enable during debugging
            // System.out.println("[Verdant Sky] Start island (" + structureId + ") at " + targetX + " " + targetY + " " + targetZ);
            // System.out.println("[Verdant Sky] Actual Biome at this pos: " + biome.unwrapKey().map(k -> k.location().toString()).orElse("Unknown"));

            data.markGenerated();
            return targetPos;
        }

        return null;
    }

    // ─────────────────────────────────────────────
    // 下界
    // ─────────────────────────────────────────────

    private static void spawnStartIslandNether(MinecraftServer server, ServerLevel level, BlockPos overworldTarget) {
        StartIslandData data = StartIslandData.get(level);
        if (data.isGenerated()) return;

        if (level.getGameTime() > NEW_WORLD_GAME_TIME_THRESHOLD) {
            data.markGenerated();
            return;
        }

        // ── 坐标换算 ──
        int targetX;
        int targetZ;
        if (overworldTarget != null) {
            // 主世界坐标除以 8 得到下界对应坐标（正确处理负数）
            targetX = Math.floorDiv(overworldTarget.getX(), 8);
            targetZ = Math.floorDiv(overworldTarget.getZ(), 8);
        } else {
            // 回退：主世界未生成时，使用下界出生点加偏移
            BlockPos spawnPos = level.getSharedSpawnPos();
            RandomSource random = RandomSource.create(level.getSeed() ^ 0x9E3779B97F4A7C15L);
            int offsetX = Mth.nextInt(random, -XZ_OFFSET, XZ_OFFSET);
            int offsetZ = Mth.nextInt(random, -XZ_OFFSET, XZ_OFFSET);
            targetX = spawnPos.getX() + offsetX;
            targetZ = spawnPos.getZ() + offsetZ;
        }
        
        BlockPos spawnPos = level.getSharedSpawnPos();
        int targetY = spawnPos.getY(); 

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
        boolean placed = placeStructureDirectly(level, structureId, targetPos);

        if (placed) {
            // Enable during debugging
            // System.out.println("[Verdant Sky] Nether start island (" + structureId + ") at " + targetX + " " + targetY + " " + targetZ);

            data.markGenerated();
        }
    }

    // ─────────────────────────────────────────────
    // 结构放置
    // ─────────────────────────────────────────────

    private static boolean placeStructureDirectly(ServerLevel level, String structureId, BlockPos pos) {
        boolean success = false;
        
        try {
            var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            ResourceLocation loc = parseResourceLocation(structureId);
            Structure structure = registry.get(loc);

            if (structure != null) {
                ChunkPos chunkPos = new ChunkPos(pos);

                // 生成结构起始点
                StructureStart start = structure.generate(
                    level.registryAccess(),
                    level.getChunkSource().getGenerator(),
                    level.getChunkSource().getGenerator().getBiomeSource(),
                    level.getChunkSource().randomState(),
                    level.getStructureManager(),
                    level.getSeed(),
                    chunkPos,
                    0,
                    level,
                    biome -> true
                );

                if (start.isValid()) {
                    BoundingBox box = start.getBoundingBox();
                    ChunkPos min = new ChunkPos(box.minX() >> 4, box.minZ() >> 4);
                    ChunkPos max = new ChunkPos(box.maxX() >> 4, box.maxZ() >> 4);

                    for (int x = min.x; x <= max.x; x++) {
                        for (int z = min.z; z <= max.z; z++) {
                            ChunkPos currentChunk = new ChunkPos(x, z);
                            start.placeInChunk(
                                level,
                                level.structureManager(),
                                level.getChunkSource().getGenerator(),
                                level.getRandom(),
                                new BoundingBox(
                                    currentChunk.getMinBlockX(), level.getMinBuildHeight(), currentChunk.getMinBlockZ(),
                                    currentChunk.getMaxBlockX(), level.getMaxBuildHeight(), currentChunk.getMaxBlockZ()
                                ),
                                currentChunk
                            );
                        }
                    }
                    success = true;
                }
            }
        } catch (Exception e) {
            System.err.println("[Verdant Sky] Exception while placing structure: " + structureId);
            e.printStackTrace();
        }

        // 兜底逻辑
        if (!success) {
            System.err.println("[Verdant Sky] Failed to place structure (" + structureId + ") at " + pos + ". Generating emergency platform.");
            
            // 在原定位置的正上方放置石头平台
            BlockPos fallbackCenter = pos;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    level.setBlockAndUpdate(fallbackCenter.offset(x, 0, z), Blocks.STONE.defaultBlockState());
                }
            }

            // 在平台中心放置告示牌
            BlockPos signPos = fallbackCenter.above();
            level.setBlockAndUpdate(signPos, Blocks.OAK_SIGN.defaultBlockState()
                    .setValue(StandingSignBlock.ROTATION, 0));

            if (level.getBlockEntity(signPos) instanceof SignBlockEntity sign) {
                Component[] lines = new Component[] {
                    Component.translatable("sign.verdant_sky.bug.line1"),
                    Component.translatable("sign.verdant_sky.bug.line2"),
                    Component.translatable("sign.verdant_sky.bug.line3"),
                    Component.translatable("sign.verdant_sky.bug.line4")
                };

                SignText signText = new SignText()
                        .setMessage(0, lines[0])
                        .setMessage(1, lines[1])
                        .setMessage(2, lines[2])
                        .setMessage(3, lines[3]);

                sign.setText(signText, true);
                sign.setChanged();
            }
        }

        return success;
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
        if (biome.is(biomeTag("start_island_forest")))   return "verdant_sky:overworld/start_island_forest";
        
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