package com.github.verdant_leaves.verdant_sky.forge.platform;

import com.github.verdant_leaves.verdant_sky.platform.PlatformFlammable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class PlatformFlammableForge implements PlatformFlammable {
    @Override
    public void register(Block block, int burnChance, int spreadChance) {
        try {
            // "m_53444_" 是 1.20.1 中 FireBlock.setFlammable(Block, int, int) 的 SRG 名称 (Block, 蔓延速度, 烧毁几率)
            ObfuscationReflectionHelper.findMethod(
                FireBlock.class, 
                "m_53444_", 
                Block.class, int.class, int.class
            ).invoke(Blocks.FIRE, block, spreadChance, burnChance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register flammable block: " + block, e);
        }
    }
}