package com.github.verdant_leaves.verdant_sky.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.github.verdant_leaves.verdant_sky.registry.ModWoods;

/**
 * 通用工具：为一组 {@link WoodVariant} 批量注册方块和对应物品。
 *
 * <p>任何“需要按木材生成变种”的方块类型，都可以调用 {@link #registerAll}，
 * 传入自己的构造器即可。添加新木材只需改 {@link ModWoods}。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * public static final List<RegistrySupplier<Item>> CAULDRON_ITEMS = new ArrayList<>();
 * static {
 *     WoodenVariantRegistry.registerAll(
 *         BLOCKS, ITEMS, CAULDRON_ITEMS,
 *         "cauldron",
 *         (wood, props) -> new WoodenCauldronBlock(props),
 *         WoodenVariantRegistry.WOOD_PROPERTIES
 *     );
 * }
 * }</pre>
 */
public class WoodenVariantRegistry {

    /** 默认的方块属性工厂，使用木材对应的 MapColor / SoundType。 */
    public static final BiFunction<WoodVariant, Void, BlockBehaviour.Properties> WOOD_PROPERTIES =
        (wood, unused) -> {
            BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .mapColor(wood.mapColor())
                .strength(2.0F)
                .sound(wood.soundType())
                .noOcclusion();
            return props;
        };

    /**
     * 为所有木材批量注册方块及其 BlockItem。
     *
     * @param blocks     方块注册表
     * @param items      物品注册表
     * @param itemOut    用于收集生成的物品，方便加入创造模式物品栏
     * @param suffix     注册名后缀，例如 "cauldron" → "oak_cauldron"
     * @param factory    方块工厂，参数为木材与属性
     * @param propsFactory 属性工厂，通常用 {@link #WOOD_PROPERTIES}
     */
    public static void registerAll(
            DeferredRegister<Block> blocks,
            DeferredRegister<Item> items,
            List<RegistrySupplier<Item>> itemOut,
            String suffix,
            BiFunction<WoodVariant, BlockBehaviour.Properties, ? extends Block> factory,
            BiFunction<WoodVariant, Void, BlockBehaviour.Properties> propsFactory
    ) {
        for (WoodVariant wood : ModWoods.ALL) {
            String id = wood.name() + "_" + suffix;
            BlockBehaviour.Properties props = propsFactory.apply(wood, null);

            RegistrySupplier<Block> block = blocks.register(id, () ->
                factory.apply(wood, props)
            );

            RegistrySupplier<Item> item = items.register(id, () ->
                new BlockItem(block.get(), new Item.Properties())
            );

            itemOut.add(item);
        }
    }
}