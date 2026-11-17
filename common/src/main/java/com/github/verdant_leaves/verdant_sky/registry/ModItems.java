package com.github.verdant_leaves.verdant_sky.registry;

import com.github.verdant_leaves.verdant_sky.VerdantSky;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BowlFoodItem;

public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(VerdantSky.MOD_ID, Registries.ITEM);
    
    // 草籽雪团 Seed Snowball
    public static final RegistrySupplier<Item> SEED_SNOWBALL = Food("seed_snowball", 1, 0.1F);
    
    // 雪拌草芽 Snow-Tossed Sprouts
    public static final RegistrySupplier<Item> SNOW_TOSSED_GREENS = BowlFood("snow_tossed_greens", 2, 0.1F);
    
    // 雪裹嫩枝 Snow-Packed Shoots
    public static final RegistrySupplier<Item> SNOW_PACKED_SHOOTS = BowlFood("snow_packed_shoots", 4, 0.1F);
    
    private static RegistrySupplier<Item> BowlFood(String id, int nutrition, float saturationModifier) {
        FoodProperties food = new FoodProperties.Builder()
            .nutrition(nutrition)
            .saturationMod(saturationModifier)
            .build();
        Item.Properties item = new Item.Properties().stacksTo(1).food(food);

        return ITEMS.register(id, () -> new BowlFoodItem(item));
    }
    private static RegistrySupplier<Item> Food(String id, int nutrition, float saturationModifier) {
        FoodProperties food = new FoodProperties.Builder()
            .nutrition(nutrition)
            .saturationMod(saturationModifier)
            .build();
        Item.Properties item = new Item.Properties().stacksTo(1).food(food);

        return ITEMS.register(id, () -> new Item(item));
    }
    
    public static void register() {
        ITEMS.register();
    }
}