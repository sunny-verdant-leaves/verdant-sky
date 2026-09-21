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
    public int decayAgapanthusRadius = 2;

    /** 单次扫描最多转换多少个方块，防止卡顿。 */
    public int decayAgapanthusMaxConversionsPerTick = 2;

    /** 腐朽花触发间隔，单位 tick。 */
    public int decayAgapanthusInterval = 100;

    /** 每次转换方块消耗的魔力。 */
    public int decayAgapanthusManaCost = 800;

    /** 内部魔力缓存上限。 */
    public int decayAgapanthusMaxMana = 10000;

    /** 每次工作时给范围内生物施加的凋零持续时长，单位 tick。 */
    public int decayAgapanthusWitherDuration = 400;

    /** 凋零效果等级。0 = I 级，1 = II 级。 */
    public int decayAgapanthusWitherAmplifier = 1;

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

    public static int decayAgapanthusRadius() {
        return Math.max(0, INSTANCE.decayAgapanthusRadius);
    }

    public static int decayAgapanthusMaxConversionsPerTick() {
        return Math.max(1, INSTANCE.decayAgapanthusMaxConversionsPerTick);
    }

    public static int decayAgapanthusInterval() {
        return Math.max(1, INSTANCE.decayAgapanthusInterval);
    }

    public static int decayAgapanthusManaCost() {
        return Math.max(1, INSTANCE.decayAgapanthusManaCost);
    }

    public static int decayAgapanthusMaxMana() {
        return Math.max(1, INSTANCE.decayAgapanthusMaxMana);
    }

    public static int decayAgapanthusWitherDuration() {
        return Math.max(1, INSTANCE.decayAgapanthusWitherDuration);
    }

    public static int decayAgapanthusWitherAmplifier() {
        return Math.max(0, INSTANCE.decayAgapanthusWitherAmplifier);
    }
}