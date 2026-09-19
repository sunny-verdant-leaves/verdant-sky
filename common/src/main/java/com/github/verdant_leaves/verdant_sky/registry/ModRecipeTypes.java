package com.github.verdant_leaves.verdant_sky.registry;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.RECIPE_TYPE);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(VerdantSky.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<RecipeType<DecayRecipe>> DECAY =
        RECIPE_TYPES.register("decay", () -> new RecipeType<DecayRecipe>() {
            @Override
            public String toString() {
                return VerdantSky.MOD_ID + ":decay";
            }
        });

    public static final RegistrySupplier<RecipeSerializer<DecayRecipe>> DECAY_SERIALIZER =
        RECIPE_SERIALIZERS.register("decay", DecayRecipe.Serializer::new);

    public static void register() {
        RECIPE_TYPES.register();
        RECIPE_SERIALIZERS.register();
    }
}