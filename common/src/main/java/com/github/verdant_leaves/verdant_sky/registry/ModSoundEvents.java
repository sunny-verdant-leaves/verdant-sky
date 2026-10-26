package com.github.verdant_leaves.verdant_sky.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(VerdantSky.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> SNOW_BRICKS_BREAK = register("block.snow_bricks.break");
    public static final RegistrySupplier<SoundEvent> SNOW_BRICKS_STEP = register("block.snow_bricks.step");
    public static final RegistrySupplier<SoundEvent> SNOW_BRICKS_PLACE = register("block.snow_bricks.place");
    public static final RegistrySupplier<SoundEvent> SNOW_BRICKS_HIT = register("block.snow_bricks.hit");
    public static final RegistrySupplier<SoundEvent> SNOW_BRICKS_FALL = register("block.snow_bricks.fall");
    public static final RegistrySupplier<SoundEvent> DRINKING_SNOW_LOTUS_CONSUME = register("block.drinking_snow_lotus.consume");

    private static RegistrySupplier<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(VerdantSky.MOD_ID, name)
        ));
    }

    public static void register() {
        SOUNDS.register();
    }
}