package com.github.verdant_leaves.verdant_sky.registry;

import net.minecraft.world.level.block.SoundType;

public class ModSoundTypes {
    public static SoundType snowBrick() {
        return new SoundType(
            1.0F, // 音量 (Volume)
            1.0F, // 音调 (Pitch)
            ModSoundEvents.SNOW_BRICKS_BREAK.get(),
            ModSoundEvents.SNOW_BRICKS_STEP.get(),
            ModSoundEvents.SNOW_BRICKS_PLACE.get(),
            ModSoundEvents.SNOW_BRICKS_HIT.get(),
            ModSoundEvents.SNOW_BRICKS_FALL.get()
        );
    }
}