package com.github.verdant_leaves.verdant_sky.client.integration.jei;

import java.util.List;

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

/**
 * 腐朽转化配方的 JEI 类别。
 */
public class DecayRecipeCategory implements IRecipeCategory<DecayRecipe> {

    public static final RecipeType<DecayRecipe> TYPE = RecipeType.create(VerdantSky.MOD_ID, "decay_conversion", DecayRecipe.class);

    private static final ResourceLocation ARROW = new ResourceLocation("jei", "textures/gui/arrow.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public DecayRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(96, 44);
        this.icon = guiHelper.createDrawableIngredient(
            VanillaTypes.ITEM_STACK,
            new ItemStack(ModBlocks.DECAY_AGAPANTHUS.get())
        );
        this.localizedName = Component.translatable("jei.verdant_sky.decay_conversion");
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
        // 输入槽
        List<ItemStack> inputs = recipe.input().getDisplayedStacks();

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 9, 12)
            .addItemStacks(inputs);

        // 附加标签提示
        List<Component> tooltip = recipe.input().descriptionTooltip();
        if (!tooltip.isEmpty()) {
            inputSlot.addRichTooltipCallback((view, tooltipBuilder) ->
                tooltipBuilder.addAll(tooltip));
        }

        // 催化剂槽
        builder.addSlot(RecipeIngredientRole.CATALYST, 39, 12)
            .addItemStack(new ItemStack(ModBlocks.DECAY_AGAPANTHUS.get()));

        // 输出槽
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
        gui.blit(ARROW, 58, 12, 0, 0, 22, 16);
    }
}