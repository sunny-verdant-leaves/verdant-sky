package com.github.verdant_leaves.verdant_sky.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 腐朽花配方输入：仿 Botania StateIngredient 的两种简单类型。
 * <pre>
 *   { "type": "block", "block": "minecraft:stone" }
 *   { "type": "tag",   "tag":   "minecraft:logs"  }
 * </pre>
 */
public abstract class DecayIngredient {

    public abstract boolean test(BlockState state);

    public abstract void toNetwork(FriendlyByteBuf buf);

    // ══════════════════════════════════════════════════════════════
    //  反序列化
    // ══════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════
    //  Block 匹配
    // ══════════════════════════════════════════════════════════════

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

        @Override
        public boolean test(BlockState state) {
            return state.is(block);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeByte(0);
            buf.writeVarInt(BuiltInRegistries.BLOCK.getId(block));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Tag 匹配
    // ══════════════════════════════════════════════════════════════

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

        @Override
        public boolean test(BlockState state) {
            return state.is(tag);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeByte(1);
            buf.writeResourceLocation(tag.location());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  工具
    // ══════════════════════════════════════════════════════════════

    private static Block resolveBlock(String name) {
        ResourceLocation loc = new ResourceLocation(name);
        Block block = BuiltInRegistries.BLOCK.get(loc);
        // 注册表里找不到时 get 会返回 air，需要和"真的注册了 air"区分
        if (block == Blocks.AIR
                && !loc.equals(BuiltInRegistries.BLOCK.getKey(Blocks.AIR))) {
            throw new JsonSyntaxException("Unknown block in decay recipe: " + loc);
        }
        return block;
    }
}