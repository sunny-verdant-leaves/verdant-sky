package com.github.verdant_leaves.verdant_sky.forge;

import java.nio.file.Path;

import dev.architectury.platform.forge.EventBuses;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;


import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.api.WoodVariant;
import com.github.verdant_leaves.verdant_sky.platform.PlatformFlammable;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;
import com.github.verdant_leaves.verdant_sky.registry.ModWoods;
import com.github.verdant_leaves.verdant_sky.forge.platform.PlatformFlammableForge;

@Mod(VerdantSky.MOD_ID)
public final class VerdantSkyForge {
    public VerdantSkyForge() {
        EventBuses.registerModEventBus(VerdantSky.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // 注入 Forge 端的燃烧实现
        PlatformFlammable.Holder.INSTANCE = new PlatformFlammableForge();

        // 运行公共初始化
        VerdantSky.init();

        // 注册燃料燃烧时间事件
        MinecraftForge.EVENT_BUS.addListener(this::onFuelBurnTime);

        // 注册内置资源包事件
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onAddPackFinders);
    }

    private void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.is(ModBlocks.LIVINGWOOD_POOL_ITEM.get())) {
            event.setBurnTime(ModBlocks.WOOD_BURN_TIME);
            return;
        }

        for (WoodVariant wood : ModWoods.ALL) {
            if (wood.fireResistant()) continue;

            // 花药台
            var apothecary = ModBlocks.PETAL_APOTHECARY_ITEMS.get(wood.name());
            if (apothecary != null && stack.is(apothecary.get())) {
                event.setBurnTime(ModBlocks.WOOD_BURN_TIME);
                return;
            }

            // 炼药锅
            var empty = ModBlocks.EMPTY_CAULDRONS.get(wood.name());
            if (empty == null) continue;
            if (stack.is(empty.get().asItem())) {
                event.setBurnTime(ModBlocks.WOOD_BURN_TIME);
                return;
            }
        }
    }

    //  内置资源包
    private void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
    
        String folderName = "vanilla_blocks";
        IModFile modFile = ModList.get().getModFileById(VerdantSky.MOD_ID).getFile();
    
        // 注意：这里不再指向 .zip，而是指向文件夹
        Path packPath = modFile.findResource("resourcepacks/" + folderName);
    
        PathPackResources packResources = new PathPackResources(folderName, true, packPath);
    
        Pack myPack = Pack.readMetaAndCreate(
            VerdantSky.MOD_ID + ":" + folderName,
            Component.translatable("resourcepack.descriptionTranslation.verdant_sky.vanilla_blocks.1"),
            false,
            (packLocationInfo) -> packResources,
            PackType.CLIENT_RESOURCES,
            Pack.Position.TOP,
            PackSource.BUILT_IN
        );
    
        if (myPack != null) {
            event.addRepositorySource((packConsumer) -> packConsumer.accept(myPack));
        }
    }
}