package com.github.verdant_leaves.verdant_sky.client.integration.jei.helper;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

/**
 * 把 LocationPredicate 转换成人类可读的 Component 列表，用于 JEI tooltip。
 *
 * <p>每种条件各占一行，字段值为 JsonNull 时跳过。
 * 位置与光照会解析 min/max 范围，输出形如 "X: 0 ~ 100" / "Y ≥ 60"。
 * 维度、生物群系、结构通过原版翻译键显示友好名称。
 */
public final class LocationPredicateFormatter {

    private LocationPredicateFormatter() {}

    public static List<Component> format(@Nullable LocationPredicate predicate) {
        if (predicate == null) {
            return List.of();
        }

        List<Component> lines = new ArrayList<>();
        JsonObject json = predicate.serializeToJson().getAsJsonObject();

        // 维度
        if (hasValue(json, "dimension")) {
            lines.add(line("dimension",
                formatResourceKey("dimension", json.get("dimension").getAsString())));
        }

        // 生物群系
        if (hasValue(json, "biome")) {
            lines.add(line("biome",
                formatResourceKey("biome", json.get("biome").getAsString())));
        }

        // 结构
        if (hasValue(json, "structure")) {
            lines.add(line("structure",
                formatResourceKey("structure", json.get("structure").getAsString())));
        }

        // 位置：每个轴独立成行
        if (hasValue(json, "position")) {
            lines.addAll(formatPosition(json.getAsJsonObject("position")));
        }

        // 光照
        if (hasValue(json, "light")) {
            Component lightLine = formatLight(json.getAsJsonObject("light"));
            if (lightLine != null) {
                lines.add(lightLine);
            }
        }

        // 可见天空
        if (hasValue(json, "can_see_sky")) {
            boolean value = json.get("can_see_sky").getAsBoolean();
            lines.add(Component.translatable(value
                    ? "jei.verdant_sky.condition.can_see_sky.yes"
                    : "jei.verdant_sky.condition.can_see_sky.no")
                .withStyle(ChatFormatting.GRAY));
        }

        // 方块
        if (hasValue(json, "block")) {
            lines.add(line("block", Component.literal(json.get("block").toString())));
        }

        // 流体
        if (hasValue(json, "fluid")) {
            lines.add(line("fluid", Component.literal(json.get("fluid").toString())));
        }

        // 营火上方
        if (hasValue(json, "smokey")) {
            boolean value = json.get("smokey").getAsBoolean();
            lines.add(Component.translatable(value
                    ? "jei.verdant_sky.condition.smokey.yes"
                    : "jei.verdant_sky.condition.smokey.no")
                .withStyle(ChatFormatting.GRAY));
        }

        if (lines.isEmpty()) {
            lines.add(Component.translatable("jei.verdant_sky.condition.empty")
                .withStyle(ChatFormatting.GRAY));
        }

        return lines;
    }

    // ══════════════════════════════════════════════════════════════
    //  位置 / 光照
    // ══════════════════════════════════════════════════════════════

    private static List<Component> formatPosition(JsonObject positionJson) {
        List<Component> lines = new ArrayList<>();
        for (String axis : List.of("x", "y", "z")) {
            if (!positionJson.has(axis)) continue;
            Component range = formatRangeValue(positionJson.get(axis));
            if (range == null) continue;

            lines.add(Component.translatable(
                "jei.verdant_sky.condition.position.axis",
                axis.toUpperCase(),
                range
            ).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    private static @Nullable Component formatLight(JsonObject lightJson) {
        if (!lightJson.has("light")) return null;
        Component range = formatRangeValue(lightJson.get("light"));
        if (range == null) return null;
        return line("light", range);
    }

    private static @Nullable Component formatRangeValue(@Nullable JsonElement value) {
        if (value == null || value.isJsonNull()) return null;

        // 具体值：{ "x": 60 }
        if (value.isJsonPrimitive()) {
            return Component.literal(formatNumber(value.getAsDouble()));
        }

        // 范围：{ "x": { "min": 0, "max": 100 } }
        if (value.isJsonObject()) {
            JsonObject obj = value.getAsJsonObject();
            boolean hasMin = hasValue(obj, "min");
            boolean hasMax = hasValue(obj, "max");

            if (hasMin && hasMax) {
                return Component.translatable(
                    "jei.verdant_sky.condition.value.range",
                    formatNumber(obj.get("min").getAsDouble()),
                    formatNumber(obj.get("max").getAsDouble())
                );
            } else if (hasMin) {
                return Component.translatable(
                    "jei.verdant_sky.condition.value.min",
                    formatNumber(obj.get("min").getAsDouble())
                );
            } else if (hasMax) {
                return Component.translatable(
                    "jei.verdant_sky.condition.value.max",
                    formatNumber(obj.get("max").getAsDouble())
                );
            }
        }

        return null;
    }

    // ══════════════════════════════════════════════════════════════
    //  工具
    // ══════════════════════════════════════════════════════════════

    /** 拼一行 "标签: 值" */
    private static Component line(String labelKey, Component value) {
        return Component.translatable(
            "jei.verdant_sky.condition." + labelKey,
            value
        ).withStyle(ChatFormatting.GRAY);
    }

    /**
     * 获取原版翻译。
     * 例如 dimension.minecraft.overworld -> "主世界" / "Overworld"。
     * 如果翻译不存在，会回退到原始 ID 字符串。
     */
    private static Component formatResourceKey(String type, String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) {
            return Component.literal(id);
        }
        return Component.translatableWithFallback(
            loc.toLanguageKey(type),
            id
        );
    }

    /** 整数就不显示小数位 */
    private static String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    /** 字段存在且不是 JsonNull */
    private static boolean hasValue(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull();
    }
}