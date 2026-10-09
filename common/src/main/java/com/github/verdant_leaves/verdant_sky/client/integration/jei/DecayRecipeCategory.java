package com.github.verdant_leaves.verdant_sky.client.integration.jei;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import org.jetbrains.annotations.NotNull;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.client.integration.jei.helper.LocationPredicateFormatter;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

@SuppressWarnings("removal")
public class DecayRecipeCategory implements IRecipeCategory<DecayRecipe> {

    public static final RecipeType<DecayRecipe> TYPE = RecipeType.create(VerdantSky.MOD_ID, "decay", DecayRecipe.class);

    private final IDrawable background;
    private final Component localizedName;
    private final IDrawable overlay;
    private final IDrawable icon;
    @SuppressWarnings("rawtypes")
    private final IPlatformFluidHelper fluidHelper;

    public DecayRecipeCategory(IGuiHelper guiHelper, IPlatformFluidHelper<?> fluidHelper) {
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

        this.fluidHelper = fluidHelper;
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

    @SuppressWarnings("unchecked")
    @Override
    public void setRecipe(
        @NotNull IRecipeLayoutBuilder builder,
        @NotNull DecayRecipe recipe,
        @NotNull IFocusGroup focusGroup
    ) {
        // 输入
        IRecipeSlotBuilder inputSlot = builder
            .addSlot(RecipeIngredientRole.INPUT, 9, 12)
            .setFluidRenderer(1000L, false, 16, 16);

        for (BlockState state : recipe.input().getDisplayed()) {
            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty()) {
                inputSlot.addIngredient(
                    fluidHelper.getFluidIngredientType(),
                    fluidHelper.create(fluid.getType(), 1000L)
                );
            }
        }

        inputSlot.addItemStacks(recipe.input().getDisplayedStacks())
            .addTooltipCallback((view, tooltip) -> tooltip.addAll(recipe.input().descriptionTooltip()));

        // 催化剂
        builder.addSlot(RecipeIngredientRole.CATALYST, 39, 12)
            .addItemStack(new ItemStack(ModBlocks.DECAY_AGAPANTHUS.get()));

        // 输出
        Block outBlock = recipe.outputState().getBlock();
        FluidState outFluid = outBlock.defaultBlockState().getFluidState();
        if (!outFluid.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 12)
                .setFluidRenderer(1000L, false, 16, 16)
                .addIngredient(
                    fluidHelper.getFluidIngredientType(),
                    fluidHelper.create(outFluid.getType(), 1000L)
                );
        } else if (outBlock.asItem() != Items.AIR) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 12)
                .addItemStack(new ItemStack(outBlock));
        }
    }

    // 绘制：overlay + 条件提示文字
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

        if (recipe.locationPredicate() != null) {
            Font font = Minecraft.getInstance().font;
            Component shortHint = Component.translatable("jei.verdant_sky.condition.required.short");
            int xOffset = background.getWidth() - font.width(shortHint) - 2;
            int yOffset = 2;
            gui.drawString(font, shortHint, xOffset, yOffset, 0x808080, false);
        }
    }

    // Tooltip：悬停在条件文字上时展开详细信息
    @NotNull
    @Override
    public List<Component> getTooltipStrings(
        @NotNull DecayRecipe recipe,
        @NotNull IRecipeSlotsView slotsView,
        double mouseX, double mouseY
    ) {
        if (recipe.locationPredicate() == null) {
            return List.of();
        }

        Font font = Minecraft.getInstance().font;
        Component shortHint = Component.translatable("jei.verdant_sky.condition.required.short");
        int xOffset = background.getWidth() - font.width(shortHint) - 2;
        int yOffset = 2;
        int width = font.width(shortHint);
        int height = font.lineHeight;

        if (mouseX >= xOffset && mouseX <= xOffset + width
            && mouseY >= yOffset && mouseY <= yOffset + height) {

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("jei.verdant_sky.condition.required"));
            tooltip.addAll(LocationPredicateFormatter.format(recipe.locationPredicate()));
            return tooltip;
        }

        return List.of();
    }
}