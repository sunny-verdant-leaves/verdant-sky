package com.github.verdant_leaves.verdant_sky.recipe;

import com.github.verdant_leaves.verdant_sky.registry.ModRecipeTypes;
import com.google.gson.JsonObject;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 腐朽花配方：把 input（block / tag）转换为其 output（BlockState）。
 * 匹配逻辑在 DecayFlowerBlockEntity 中主动进行，不依赖合成容器。
 */
public class DecayRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final DecayIngredient input;
    private final BlockState output;

    public DecayRecipe(ResourceLocation id, DecayIngredient input, BlockState output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    public DecayIngredient input() {
        return input;
    }

    public BlockState outputState() {
        return output;
    }

    /** 供方块实体调用：判断某个方块状态是否匹配本配方。 */
    public boolean matchesState(BlockState state) {
        return input.test(state);
    }

    // ══════════════════════════════════════════════════════════════
    //  Recipe 接口实现（本配方不以容器形式使用）
    // ══════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════
    //  Serializer
    // ══════════════════════════════════════════════════════════════

    public static class Serializer implements RecipeSerializer<DecayRecipe> {

        @Override
        public DecayRecipe fromJson(ResourceLocation id, JsonObject json) {
            DecayIngredient input = DecayIngredient.fromJson(
                GsonHelper.getAsJsonObject(json, "input"));
            BlockState output = DecayOutput.fromJson(
                GsonHelper.getAsJsonObject(json, "output"));
            return new DecayRecipe(id, input, output);
        }

        @Override
        public DecayRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            DecayIngredient input = DecayIngredient.fromNetwork(buf);
            BlockState output = Block.stateById(buf.readVarInt());
            return new DecayRecipe(id, input, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DecayRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeVarInt(Block.getId(recipe.output));
        }
    }
}