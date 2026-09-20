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

    // ── 腐朽花 ──

    /** 腐朽花作用半径。2 表示 5×5×5，3 表示 7×7×7。 */
    public int decayFlowerRadius = 2;

    /** 单次扫描最多转换多少个方块，防止卡顿。 */
    public int decayFlowerMaxConversionsPerTick = 4;

    /** 腐朽花触发间隔，单位 tick。 */
    public int decayFlowerInterval = 60;

    /** 每次转换方块消耗的魔力。 */
    public int decayFlowerManaCost = 450;

    /** 内部魔力缓存上限。 */
    public int decayFlowerMaxMana = 10000;

    /** 每次工作时给范围内生物施加的凋零持续时长，单位 tick。 */
    public int decayFlowerWitherDuration = 100;

    /** 凋零效果等级。0 = I 级，1 = II 级。 */
    public int decayFlowerWitherAmplifier = 1;

    // ── 加载 / 保存 ──

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

    // ── 访问器 ──

    public static int decayFlowerRadius() {
        return Math.max(0, INSTANCE.decayFlowerRadius);
    }

    public static int decayFlowerMaxConversionsPerTick() {
        return Math.max(1, INSTANCE.decayFlowerMaxConversionsPerTick);
    }

    public static int decayFlowerInterval() {
        return Math.max(1, INSTANCE.decayFlowerInterval);
    }

    public static int decayFlowerManaCost() {
        return Math.max(1, INSTANCE.decayFlowerManaCost);
    }

    public static int decayFlowerMaxMana() {
        return Math.max(1, INSTANCE.decayFlowerMaxMana);
    }

    public static int decayFlowerWitherDuration() {
        return Math.max(1, INSTANCE.decayFlowerWitherDuration);
    }

    public static int decayFlowerWitherAmplifier() {
        return Math.max(0, INSTANCE.decayFlowerWitherAmplifier);
    }
}