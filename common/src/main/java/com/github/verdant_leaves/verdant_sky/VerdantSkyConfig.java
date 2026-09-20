package com.github.verdant_leaves.verdant_sky;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.architectury.platform.Platform;

public class VerdantSkyConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
        Platform.getConfigFolder().resolve("verdant_sky.json");

    private static VerdantSkyConfig INSTANCE = new VerdantSkyConfig();

    public int decayFlowerRadius = 2;
    public int decayFlowerMaxConversionsPerTick = 4;

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    VerdantSkyConfig loaded = GSON.fromJson(reader, VerdantSkyConfig.class);
                    if (loaded != null) INSTANCE = loaded;
                }
            }
            save();
        } catch (IOException e) {
            System.err.println("[verdant_sky] Failed to load config: " + e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            System.err.println("[verdant_sky] Failed to save config: " + e);
        }
    }

    public static int decayFlowerRadius() {
        return Math.max(0, INSTANCE.decayFlowerRadius);
    }

    public static int decayFlowerMaxConversionsPerTick() {
        return Math.max(1, INSTANCE.decayFlowerMaxConversionsPerTick);
    }
}