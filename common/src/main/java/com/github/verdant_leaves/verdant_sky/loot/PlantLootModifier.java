package com.github.verdant_leaves.verdant_sky.loot;

import dev.architectury.event.events.common.LootEvent;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import com.github.verdant_leaves.verdant_sky.VerdantSkyConfig;

public class PlantLootModifier {

    private static final TagKey<Item> SHEARS = TagKey.create(
        Registries.ITEM,
        new ResourceLocation("c", "tools/shears")
    );

    public static void register() {
        LootEvent.MODIFY_LOOT_TABLE.register(
            (LootDataManager lootDataManager, ResourceLocation id, LootEvent.LootTableModificationContext context, boolean builtin) -> {
                if (!builtin) return;
                if (!VerdantSkyConfig.enablePlantLootModification()) return;

                if (Blocks.FERN.getLootTable().equals(id)) {
                    context.addPool(singleBlockPool(Items.FERN, 0.1f, 0.15f, 0.2f, 0.25f));
                }

                if (Blocks.GRASS.getLootTable().equals(id)) {
                    context.addPool(singleBlockPool(Items.GRASS, 0.1f, 0.15f, 0.2f, 0.25f));
                }

                if (Blocks.SEAGRASS.getLootTable().equals(id)) {
                    context.addPool(singleBlockPool(Items.SEAGRASS, 0.1f, 0.15f, 0.2f, 0.25f));
                }

                if (Blocks.LARGE_FERN.getLootTable().equals(id)) {
                    addDoubleBlockPools(context, Items.FERN, Blocks.LARGE_FERN, 0.1f, 0.15f, 0.2f, 0.25f);
                }

                if (Blocks.TALL_GRASS.getLootTable().equals(id)) {
                    addDoubleBlockPools(context, Items.GRASS, Blocks.TALL_GRASS, 0.1f, 0.15f, 0.2f, 0.25f);
                }

                if (Blocks.TALL_SEAGRASS.getLootTable().equals(id)) {
                    addDoubleBlockPools(context, Items.SEAGRASS, Blocks.TALL_SEAGRASS, 0.1f, 0.15f, 0.2f, 0.25f);
                }
            }
        );
    }

    private static LootPool.Builder singleBlockPool(Item drop, float... fortuneChances) {
        return LootPool.lootPool()
                .add(LootItem.lootTableItem(drop)
                        .when(ExplosionCondition.survivesExplosion())
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                Enchantments.BLOCK_FORTUNE, fortuneChances))
                        .when(notShearsNorSilkTouch()));
    }

    private static void addDoubleBlockPools(
            LootEvent.LootTableModificationContext context,
            Item drop, Block block, float... fortuneChances) {

        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            DoubleBlockHalf other = (half == DoubleBlockHalf.LOWER)
                    ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
            int offsetY = (half == DoubleBlockHalf.LOWER) ? 1 : -1;

            context.addPool(LootPool.lootPool()
                    .add(LootItem.lootTableItem(drop)
                            .when(ExplosionCondition.survivesExplosion())
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                    Enchantments.BLOCK_FORTUNE, fortuneChances))
                            .when(notShearsNorSilkTouch()))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, half)))
                    .when(LocationCheck.checkLocation(
                            LocationPredicate.Builder.location()
                                    .setBlock(BlockPredicate.Builder.block()
                                            .of(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, other)
                                                    .build())
                                            .build()),
                            new BlockPos(0, offsetY, 0)))
            );
        }
    }

    private static LootItemCondition.Builder notShearsNorSilkTouch() {
        return InvertedLootItemCondition.invert(
                AnyOfCondition.anyOf(
                        MatchTool.toolMatches(
                                ItemPredicate.Builder.item().of(SHEARS)
                        ),
                        MatchTool.toolMatches(
                                ItemPredicate.Builder.item().hasEnchantment(
                                        new EnchantmentPredicate(
                                                Enchantments.SILK_TOUCH,
                                                MinMaxBounds.Ints.ANY)
                                )
                        )
                )
        );
    }
}