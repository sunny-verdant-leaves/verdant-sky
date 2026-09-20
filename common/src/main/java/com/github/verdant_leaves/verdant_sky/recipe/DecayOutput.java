package com.github.verdant_leaves.verdant_sky.recipe;

import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class DecayOutput {

    private DecayOutput() {}

    public static BlockState fromJson(JsonObject json) {
        String name;
        if (json.has("name")) {
            name = GsonHelper.getAsString(json, "name");
        } else if (json.has("block")) {
            name = GsonHelper.getAsString(json, "block");
        } else {
            throw new JsonSyntaxException(
                "Decay output must contain 'name' (or 'block')");
        }

        ResourceLocation loc = new ResourceLocation(name);
        Block block = BuiltInRegistries.BLOCK.get(loc);
        if (block == Blocks.AIR
                && !loc.equals(BuiltInRegistries.BLOCK.getKey(Blocks.AIR))) {
            throw new JsonSyntaxException("Unknown output block: " + loc);
        }

        BlockState state = block.defaultBlockState();

        if (json.has("properties")) {
            JsonObject props = GsonHelper.getAsJsonObject(json, "properties");
            for (Map.Entry<String, JsonElement> entry : props.entrySet()) {
                state = applyProperty(state, block, loc,
                    entry.getKey(), entry.getValue().getAsString());
            }
        }
        return state;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState applyProperty(
            BlockState state, Block block, ResourceLocation loc,
            String key, String value) {

        Property<?> property = block.getStateDefinition().getProperty(key);
        if (property == null) {
            throw new JsonSyntaxException(
                "Unknown property '" + key + "' for block " + loc);
        }
        Optional<?> parsed = property.getValue(value);
        if (parsed.isEmpty()) {
            throw new JsonSyntaxException(
                "Invalid value '" + value + "' for property '" + key
                    + "' on block " + loc);
        }
        return state.setValue((Property) property, (Comparable) parsed.get());
    }
}