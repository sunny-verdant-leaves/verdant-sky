package com.github.verdant_leaves.verdant_sky.world;

import dev.architectury.event.events.common.LifecycleEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

public class StartIslandSpawner {

    private static final ResourceLocation VOID_NOISE_SETTINGS =
        new ResourceLocation("verdant_sky", "void");

    private static final long NEW_WORLD_GAME_TIME_THRESHOLD = 20;

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

        int targetX = spawnPos.getX();
        int targetY = spawnPos.getY();
        int targetZ = spawnPos.getZ();

        // ── 提前拿到生物群系，供多处使用 ──
        BlockPos targetPos = new BlockPos(targetX, targetY, targetZ);
        Holder<Biome> biome = overworld.getBiome(targetPos);

        // ── 放置模板 ──
        String templateId = pickTemplateForBiome(biome);
        String templateCommand = String.format(
            "place structure %s %d %d %d",
            templateId, targetX, targetY, targetZ
        );
        server.getCommands().performPrefixedCommand(
            server.createCommandSourceStack().withSuppressedOutput(),
            templateCommand
        );

        // ── 按生物群系放花 ──
        if (biome.is(biomeTag("start_flowers"))) {
            String flowerCommand = String.format(
                "place feature minecraft:forest_flowers %d %d %d",
                targetX, targetY+4, targetZ
            );
            server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput(),
                flowerCommand
            );
            System.out.println("[Verdant Sky] Flowers placed at " + targetX + " " + targetY + " " + targetZ);
        }

        data.markGenerated();

        System.out.println("[Verdant Sky] Start island (" + templateId + ") at " + targetX + " " + targetY + " " + targetZ);
    }

    private static String pickTemplateForBiome(Holder<Biome> biome) {
        // ── 第一层：独特标签 ──
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