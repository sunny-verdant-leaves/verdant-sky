package com.github.verdant_leaves.verdant_sky.fabric.platform;

import com.github.verdant_leaves.verdant_sky.platform.PlatformFlammable;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.Block;

public class PlatformFlammableFabric implements PlatformFlammable {
    @Override
    public void register(Block block, int burnChance, int spreadChance) {
        FlammableBlockRegistry.getDefaultInstance().add(block, burnChance, spreadChance);
    }
}