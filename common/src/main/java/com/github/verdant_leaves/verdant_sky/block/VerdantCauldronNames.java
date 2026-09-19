package com.github.verdant_leaves.verdant_sky.block;

public final class VerdantCauldronNames {
    private VerdantCauldronNames() {}

    public static String empty(String wood)      { return wood + "_cauldron"; }
    public static String water(String wood)      { return wood + "_water_cauldron"; }
    public static String lava(String wood)       { return wood + "_lava_cauldron"; }
    public static String powderSnow(String wood) { return wood + "_powder_snow_cauldron"; }
}