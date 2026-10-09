package com.github.verdant_leaves.verdant_sky.client.integration.jei;

import com.mojang.blaze3d.systems.RenderSystem;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

public class DecayRecipeCategory implements IRecipeCategory<DecayRecipe> {

    public static final RecipeType<DecayRecipe> TYPE = RecipeType.create(VerdantSky.MOD_ID, "decay_conversion", DecayRecipe.class);

    private final IDrawable background;
    private final Component localizedName;
    private final IDrawable overlay;
    private final IDrawable icon;

    public DecayRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(96, 44);
        this.localizedName = Component.translatable("jei.verdant_sky.decay");

        this.overlay = guiHelper.createDrawable(
            new ResourceLocation(VerdantSky.MOD_ID, "textures/gui/decay_overlay.png"),
            0, 0, 64, 44
        );

        this.icon = guiHelper.createDrawableIngredient(
            VanillaTypes.ITEM_STACK,
            new ItemStack(ModBlocks.DECAY_AGAPANTHUS.get())
        );
    }

    @NotNull
    @Override
    public RecipeType<DecayRecipe> getRecipeType() {
        return TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return localizedName;
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
        @NotNull IRecipeLayoutBuilder builder, 
        @NotNull DecayRecipe recipe, 
        @NotNull IFocusGroup focusGroup
    ) {
        // 输入
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 12)
            .addItemStacks(recipe.input().getDisplayedStacks());

        // 催化剂
        builder.addSlot(RecipeIngredientRole.CATALYST, 39, 12)
            .addItemStack(new ItemStack(ModBlocks.DECAY_AGAPANTHUS.get()));

        // 输出
        ItemStack output = new ItemStack(recipe.outputState().getBlock().asItem());
        if (!output.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 12)
                .addItemStack(output);
        }
    }

    @Override
    public void draw(
        @NotNull DecayRecipe recipe, 
        @NotNull IRecipeSlotsView slotsView, 
        @NotNull GuiGraphics gui, 
        double mouseX, double mouseY
    ) {
        RenderSystem.enableBlend();
        overlay.draw(gui, 17, 0);
        RenderSystem.disableBlend();
    }
}