package com.github.verdant_leaves.verdant_sky.recipe;

import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class DecayRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final DecayIngredient input;
    private final BlockState output;
    @Nullable
    private final LocationPredicate locationPredicate;

    public DecayRecipe(ResourceLocation id, DecayIngredient input, BlockState output, @Nullable LocationPredicate locationPredicate) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.locationPredicate = locationPredicate;
    }

    public DecayIngredient input() {
        return input;
    }

    public BlockState outputState() {
        return output;
    }

    @Nullable
    public LocationPredicate locationPredicate() {
        return locationPredicate;
    }

    /**
     * 检查给定位置是否满足配方的条件。
     * 如果配方未定义条件，则始终返回 true。
     */
    public boolean matchesLocation(ServerLevel level, BlockPos pos) {
        if (locationPredicate == null) {
            return true;
        }
        return locationPredicate.matches(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public boolean matchesState(BlockState state) {
        return input.test(state);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.DECAY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.DECAY.get();
    }

    public static class Serializer implements RecipeSerializer<DecayRecipe> {

        @Override
        public DecayRecipe fromJson(ResourceLocation id, JsonObject json) {
            DecayIngredient input = DecayIngredient.fromJson(
                GsonHelper.getAsJsonObject(json, "input"));
            BlockState output = DecayOutput.fromJson(
                GsonHelper.getAsJsonObject(json, "output"));

            LocationPredicate locationPredicate = null;
            if (json.has("location")) {
                locationPredicate = LocationPredicate.fromJson(json.get("location"));
            }

            return new DecayRecipe(id, input, output, locationPredicate);
        }

        @Override
        public DecayRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            DecayIngredient input = DecayIngredient.fromNetwork(buf);
            BlockState output = Block.stateById(buf.readVarInt());

            LocationPredicate locationPredicate = null;
            if (buf.readBoolean()) {
                String jsonStr = buf.readUtf();
                JsonElement element = JsonParser.parseString(jsonStr);
                locationPredicate = LocationPredicate.fromJson(element);
            }

            return new DecayRecipe(id, input, output, locationPredicate);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DecayRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeVarInt(Block.getId(recipe.output));

            boolean hasLocation = recipe.locationPredicate != null;
            buf.writeBoolean(hasLocation);
            if (hasLocation) {
                buf.writeUtf(recipe.locationPredicate.serializeToJson().toString());
            }
        }
    }
}