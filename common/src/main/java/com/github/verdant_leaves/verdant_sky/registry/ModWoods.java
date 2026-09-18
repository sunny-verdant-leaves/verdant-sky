package com.github.verdant_leaves.verdant_sky.registry;

import java.util.ArrayList;
import java.util.List;

import com.github.verdant_leaves.verdant_sky.api.WoodVariant;

/**
 * 模组支持的所有木材类型。
 *
 * <p><b>添加新木材只需在下方加一行。</b>
 * 所有依赖木材变种的方块类型都会自动生成对应变种。</p>
 */
public class ModWoods {
    public static final List<WoodVariant> ALL = new ArrayList<>();

    // ── 主世界木材 ──
    public static final WoodVariant OAK = add(WoodVariant.normal("oak"));
    public static final WoodVariant SPRUCE = add(WoodVariant.normal("spruce"));
    public static final WoodVariant BIRCH = add(WoodVariant.normal("birch"));
    public static final WoodVariant JUNGLE = add(WoodVariant.normal("jungle"));
    public static final WoodVariant ACACIA = add(WoodVariant.normal("acacia"));
    public static final WoodVariant DARK_OAK = add(WoodVariant.normal("dark_oak"));
    public static final WoodVariant MANGROVE = add(WoodVariant.normal("mangrove"));  // 1.19
    public static final WoodVariant CHERRY = add(WoodVariant.normal("cherry"));      // 1.20
    public static final WoodVariant BAMBOO = add(WoodVariant.normal("bamboo"));      // 1.20

    // ── 下界木材（防火）──
    public static final WoodVariant CRIMSON = add(WoodVariant.nether("crimson"));
    public static final WoodVariant WARPED = add(WoodVariant.nether("warped"));

    private static WoodVariant add(WoodVariant variant) {
        ALL.add(variant);
        return variant;
    }
}