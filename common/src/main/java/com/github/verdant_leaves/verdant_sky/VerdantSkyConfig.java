package com.github.verdant_leaves.verdant_sky;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.architectury.platform.Platform;

public class VerdantSkyConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = Platform.getConfigFolder().resolve("verdant_sky.json");

    private static VerdantSkyConfig INSTANCE = new VerdantSkyConfig();

    // ══════════════════════════════════════════════════════════════
    //  腐朽百子莲 Decay Agapanthus
    // ══════════════════════════════════════════════════════════════
    /** 腐朽百子莲作用半径（以花为中心的正方体边长）。2 表示 5×5×5。  */
    public int decayAgapanthusRadius = 2;

    /** 单次工作最多转换多少个方块，防止卡顿。 */
    public int decayAgapanthusMaxConversionsPerTick = 2;

    /** 腐朽百子莲触发间隔，单位 tick。 */
    public int decayAgapanthusInterval = 100;

    /** 每次转换方块消耗的魔力。 */
    public int decayAgapanthusManaCost = 900;

    /** 内部魔力缓存上限。 */
    public int decayAgapanthusMaxMana = 5000;

    /** 每次工作时给范围内生物施加的凋零持续时长，单位 tick。 */
    public int decayAgapanthusWitherDuration = 120;

    /** 凋零效果等级。0 = I 级，1 = II 级。 */
    public int decayAgapanthusWitherAmplifier = 1;

    // ══════════════════════════════════════════════════════════════
    //  饮雪莲 Drinking Snow Lotus
    // ══════════════════════════════════════════════════════════════
    /** 饮雪莲工作半径（以花为中心的正方体边长）。3 表示 7×7×7。 */
    public int drinkingSnowLotusRadius = 3;
    
    /** 雪球提供的工作时间，单位：tick。 */
    public int drinkingSnowLotusSnowballWorkTicks = 40;

    /** 顶层雪提供的工作时间，单位：tick。 */
    public int drinkingSnowLotusSnowLayerWorkTicks = 80;

    /** 雪块提供的工作时间，单位：tick。 */
    public int drinkingSnowLotusSnowBlockWorkTicks = 160;

    /** 普通生物群系：单次产出的魔力量 */
    public int drinkingSnowLotusNormalManaAmount = 3;

    /** 普通生物群系：产出间隔（tick） */
    public int drinkingSnowLotusNormalManaInterval = 2;

    /** 寒冷生物群系：单次产出的魔力量 */
    public int drinkingSnowLotusColdManaAmount = 9;

    /** 寒冷生物群系：产出间隔（tick） */
    public int drinkingSnowLotusColdManaInterval = 4;

    /** 产魔速率随机波动范围。0.2 = ±20% 的波动。 */
    public double drinkingSnowLotusManaVariance = 0.2;

    /** 工作时间缓冲上限，单位：tick。 */
    public int drinkingSnowLotusMaxWorkTicks = 72000;


    // ══════════════════════════════════════════════════════════════
    //  修改项 Modification
    // ══════════════════════════════════════════════════════════════
    /** 是否启用植物掉落修改（空手打草掉草/蕨等）。 */
    public boolean enablePlantLootModification = false;

    /** 是否启用一碗水修改（从炼药锅里舀水），仅在 water_bowl 被注册时生效。 */
    public boolean enableWaterBowlModification = false;


    // ══════════════════════════════════════════════════════════════
    //  加载 / 保存
    // ══════════════════════════════════════════════════════════════
    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    JsonObject onDisk = GSON.fromJson(reader, JsonObject.class);
                    if (onDisk != null) {
                        // 从"新建的默认配置"导出 JSON，作为默认值的来源
                        JsonObject defaults = GSON
                            .toJsonTree(new VerdantSkyConfig())
                            .getAsJsonObject();
                        // 用磁盘上的值覆盖默认值
                        for (var entry : onDisk.entrySet()) {
                            defaults.add(entry.getKey(), entry.getValue());
                        }
                        // 从合并后的 JSON 反序列化——新字段保留默认值
                        INSTANCE = GSON.fromJson(defaults, VerdantSkyConfig.class);
                    }
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


    // ══════════════════════════════════════════════════════════════
    //  访问器
    // ══════════════════════════════════════════════════════════════
    //  -- 腐朽百子莲 Decay Agapanthus --
    public static int decayAgapanthusRadius() {
        return Math.max(1, INSTANCE.decayAgapanthusRadius);
    }

    public static int decayAgapanthusMaxConversionsPerTick() {
        return Math.max(0, INSTANCE.decayAgapanthusMaxConversionsPerTick);
    }

    public static int decayAgapanthusInterval() {
        return Math.max(1, INSTANCE.decayAgapanthusInterval);
    }

    public static int decayAgapanthusManaCost() {
        return INSTANCE.decayAgapanthusManaCost;
    }

    public static int decayAgapanthusMaxMana() {
        return Math.max(1, INSTANCE.decayAgapanthusMaxMana);
    }

    public static int decayAgapanthusWitherDuration() {
        return Math.max(0, INSTANCE.decayAgapanthusWitherDuration);
    }

    public static int decayAgapanthusWitherAmplifier() {
        return Math.max(0, INSTANCE.decayAgapanthusWitherAmplifier);
    }
    
    //  -- 饮雪莲 Drinking Snow Lotus --
    public static int drinkingSnowLotusRadius() {
        return Math.max(1, INSTANCE.drinkingSnowLotusRadius);
    }
    
    public static int drinkingSnowLotusSnowballWorkTicks() {
        return Math.max(1, INSTANCE.drinkingSnowLotusSnowballWorkTicks);
    }
    
    public static int drinkingSnowLotusSnowLayerWorkTicks() {
        return Math.max(1, INSTANCE.drinkingSnowLotusSnowLayerWorkTicks);
    }
    
    public static int drinkingSnowLotusSnowBlockWorkTicks() {
        return Math.max(1, INSTANCE.drinkingSnowLotusSnowBlockWorkTicks);
    }
    
    public static int drinkingSnowLotusNormalManaAmount() {
        return Math.max(1, INSTANCE.drinkingSnowLotusNormalManaAmount);
    }
    
    public static int drinkingSnowLotusNormalManaInterval() {
        return Math.max(1, INSTANCE.drinkingSnowLotusNormalManaInterval);
    }
    
    public static int drinkingSnowLotusColdManaAmount() {
        return Math.max(1, INSTANCE.drinkingSnowLotusColdManaAmount);
    }
    
    public static int drinkingSnowLotusColdManaInterval() {
        return Math.max(1, INSTANCE.drinkingSnowLotusColdManaInterval);
    }

    public static double drinkingSnowLotusManaVariance() {
        return Math.max(0.0, INSTANCE.drinkingSnowLotusManaVariance);
    }
    
    public static int drinkingSnowLotusMaxWorkTicks() {
        return Math.max(1, INSTANCE.drinkingSnowLotusMaxWorkTicks);
    }
    
    //  -- 修改项 Modification --
    public static boolean enablePlantLootModification() {
        return INSTANCE.enablePlantLootModification;
    }
    
    public static boolean enableWaterBowlModification() {
        return INSTANCE.enableWaterBowlModification;
    }
    
}