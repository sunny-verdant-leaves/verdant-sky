package com.github.verdant_leaves.verdant_sky.recipe;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DecayIngredient {

    public abstract boolean test(BlockState state);

    public abstract void toNetwork(FriendlyByteBuf buf);

    public abstract List<ItemStack> getDisplayedStacks();

    public List<Component> descriptionTooltip() {
        return List.of();
    }

    public static DecayIngredient fromJson(JsonObject json) {
        String type = GsonHelper.getAsString(json, "type");
        return switch (type) {
            case "block" -> new BlockIngredient(json);
            case "tag"   -> new TagIngredient(json);
            default -> throw new JsonSyntaxException(
                "Unknown decay ingredient type: " + type);
        };
    }

    public static DecayIngredient fromNetwork(FriendlyByteBuf buf) {
        int type = buf.readByte();
        return switch (type) {
            case 0 -> new BlockIngredient(buf);
            case 1 -> new TagIngredient(buf);
            default -> throw new IllegalArgumentException(
                "Unknown decay ingredient network type: " + type);
        };
    }

    //  方块原料
    public static class BlockIngredient extends DecayIngredient {

        private final Block block;

        public BlockIngredient(Block block) {
            this.block = block;
        }

        public BlockIngredient(JsonObject json) {
            this(resolveBlock(GsonHelper.getAsString(json, "block")));
        }

        public BlockIngredient(FriendlyByteBuf buf) {
            this(BuiltInRegistries.BLOCK.byId(buf.readVarInt()));
        }

        public Block getBlock() {
            return block;
        }

        @Override
        public boolean test(BlockState state) {
            return state.is(block);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeByte(0);
            buf.writeVarInt(BuiltInRegistries.BLOCK.getId(block));
        }

        @Override
        public List<ItemStack> getDisplayedStacks() {
            ItemStack stack = new ItemStack(block.asItem());
            return stack.isEmpty() ? List.of() : List.of(stack);
        }
    }

    //  标签原料
    public static class TagIngredient extends DecayIngredient {

        private final TagKey<Block> tag;

        public TagIngredient(TagKey<Block> tag) {
            this.tag = tag;
        }

        public TagIngredient(JsonObject json) {
            this(TagKey.create(
                Registries.BLOCK,
                new ResourceLocation(GsonHelper.getAsString(json, "tag"))
            ));
        }

        public TagIngredient(FriendlyByteBuf buf) {
            this(TagKey.create(Registries.BLOCK, buf.readResourceLocation()));
        }

        public TagKey<Block> getTag() {
            return tag;
        }

        @Override
        public boolean test(BlockState state) {
            return state.is(tag);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeByte(1);
            buf.writeResourceLocation(tag.location());
        }

        @Override
        public List<ItemStack> getDisplayedStacks() {
            Optional<HolderSet.Named<Block>> optionalTag =
                BuiltInRegistries.BLOCK.getTag(tag);
            if (optionalTag.isEmpty()) return List.of();

            return optionalTag.get().stream()
                .map(holder -> new ItemStack(holder.value().asItem()))
                .filter(stack -> !stack.isEmpty())
                .findFirst()
                .map(List::of)
                .orElse(List.of());
        }

        @Override
        public List<Component> descriptionTooltip() {
            return List.of(
                Component.literal("Tag: #" + tag.location())
                    .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    //  工具方法
    private static Block resolveBlock(String name) {
        ResourceLocation loc = new ResourceLocation(name);
        Block block = BuiltInRegistries.BLOCK.get(loc);
        if (block == Blocks.AIR
                && !loc.equals(BuiltInRegistries.BLOCK.getKey(Blocks.AIR))) {
            throw new JsonSyntaxException("Unknown block in decay recipe: " + loc);
        }
        return block;
    }
}