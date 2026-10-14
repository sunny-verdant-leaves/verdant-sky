package com.github.verdant_leaves.verdant_sky.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import dev.architectury.platform.Platform;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import com.github.verdant_leaves.verdant_sky.api.WoodVariant;

/**
 * 模组支持的所有木材类型清单。
 * <p><b>添加新木材只需在下方加一行。</b></p>
 */
public final class ModWoods {
    private ModWoods() {}

    public static final List<WoodVariant> ALL = new ArrayList<>();

    // 主世界木材
    public static final WoodVariant OAK      = add(WoodVariant.normal("oak"));
    public static final WoodVariant SPRUCE   = add(WoodVariant.normal("spruce"));
    public static final WoodVariant BIRCH    = add(WoodVariant.normal("birch"));
    public static final WoodVariant JUNGLE   = add(WoodVariant.normal("jungle"));
    public static final WoodVariant ACACIA   = add(WoodVariant.normal("acacia"));
    public static final WoodVariant DARK_OAK = add(WoodVariant.normal("dark_oak"));
    public static final WoodVariant MANGROVE = add(WoodVariant.normal("mangrove"));
    public static final WoodVariant CHERRY   = add(WoodVariant.normal("cherry"));
    public static final WoodVariant BAMBOO   = add(WoodVariant.normal("bamboo"));

    // 下界木材（防火）
    public static final WoodVariant CRIMSON  = add(WoodVariant.nether("crimson"));
    public static final WoodVariant WARPED   = add(WoodVariant.nether("warped"));

    // Botania 木材
    public static final WoodVariant LIVINGWOOD = addIf("botania", () -> new WoodVariant("livingwood", false, MapColor.WOOD, SoundType.WOOD));
    public static final WoodVariant DREAMWOOD = addIf("botania", () -> new WoodVariant("dreamwood", false, MapColor.WOOD, SoundType.WOOD));

    private static WoodVariant add(WoodVariant variant) {
        ALL.add(variant);
        return variant;
    }

    private static WoodVariant addIf(String modId, Supplier<WoodVariant> factory) {
        if (!Platform.isModLoaded(modId)) return null;
        return add(factory.get());
    }

    /** 按木材 id 查找。找不到返回 null。 */
    public static WoodVariant byName(String name) {
        for (WoodVariant wood : ALL) {
            if (wood.name().equals(name)) {
                return wood;
            }
        }
        return null;
    }

    /** 按木材 id 查找。找不到则抛出异常。 */
    public static WoodVariant requireByName(String name) {
        WoodVariant variant = byName(name);
        if (variant == null) {
            throw new IllegalArgumentException("Unknown wood: " + name);
        }
        return variant;
    }
}