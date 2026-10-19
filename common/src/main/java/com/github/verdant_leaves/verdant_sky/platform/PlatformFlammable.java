package com.github.verdant_leaves.verdant_sky.platform;

import net.minecraft.world.level.block.Block;

public interface PlatformFlammable {
    void register(Block block, int burnChance, int spreadChance);

    class Holder {
        public static PlatformFlammable INSTANCE;
    }
}