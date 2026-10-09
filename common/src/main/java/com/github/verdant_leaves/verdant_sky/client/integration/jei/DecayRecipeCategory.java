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

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

@SuppressWarnings("removal")
public class DecayRecipeCategory implements IRecipeCategory<DecayRecipe> {

    public static final RecipeType<DecayRecipe> TYPE = RecipeType.create(VerdantSky.MOD_ID, "decay", DecayRecipe.class);

    /** 条件提示文字在背景上的位置（相对背景左上角） */
    private static final int CONDITION_TEXT_X = 4;
    private static final int CONDITION_TEXT_Y = 34;

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
            .addItemStack(new ItemStack(ModBlocks.DECAY_AGAPANTHUS.get()))
            .addItemStack(new ItemStack(ModBlocks.FLOATING_DECAY_AGAPANTHUS.get()));

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

        // 有位置条件时，在背景底部显示一行提示文字
        if (recipe.locationPredicate() != null) {
            Component hint = Component.translatable("jei.verdant_sky.decay.condition_required")
                .withStyle(ChatFormatting.DARK_GRAY);
            gui.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                hint,
                CONDITION_TEXT_X, CONDITION_TEXT_Y,
                0x404040,
                false
            );
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

        // 判断鼠标是否悬停在提示文字区域
        int textWidth = net.minecraft.client.Minecraft.getInstance().font
            .width(Component.translatable("jei.verdant_sky.decay.condition_required"));
        if (mouseX >= CONDITION_TEXT_X && mouseX <= CONDITION_TEXT_X + textWidth
                && mouseY >= CONDITION_TEXT_Y && mouseY <= CONDITION_TEXT_Y + 10) {
            return formatPredicateTooltip(recipe.locationPredicate());
        }

        return List.of();
    }

    /**
     * 把 LocationPredicate 转成一行行可读的 Component。
     * 通过序列化 JSON 反推条件内容，避免反射。
     *
     * <p> TODO: 后续把条件显示逻辑抽象到独立的 helper 类，
     * 让其他配方类别也能复用同一套格式化规则。
     */
    private static List<Component> formatPredicateTooltip(@Nullable LocationPredicate predicate) {
        if (predicate == null) {
            return List.of();
        }
    
        List<Component> lines = new ArrayList<>();
        com.google.gson.JsonObject json = predicate.serializeToJson().getAsJsonObject();
    
        // 维度
        if (json.has("dimension")) {
            lines.add(Component.translatable(
                "jei.verdant_sky.decay.condition.dimension",
                json.get("dimension").getAsString()
            ).withStyle(ChatFormatting.GRAY));
        }
    
        // 生物群系
        if (json.has("biome")) {
            lines.add(Component.translatable(
                "jei.verdant_sky.decay.condition.biome",
                json.get("biome").getAsString()
            ).withStyle(ChatFormatting.GRAY));
        }
    
        // 结构
        if (json.has("structure")) {
            lines.add(Component.translatable(
                "jei.verdant_sky.decay.condition.structure",
                json.get("structure").getAsString()
            ).withStyle(ChatFormatting.GRAY));
        }
    
        // 坐标范围
        if (json.has("position")) {
            lines.add(Component.translatable(
                "jei.verdant_sky.decay.condition.position",
                json.getAsJsonObject("position").toString()
            ).withStyle(ChatFormatting.GRAY));
        }
    
        // 光照等级
        if (json.has("light")) {
            lines.add(Component.translatable(
                "jei.verdant_sky.decay.condition.light",
                json.get("light").toString()
            ).withStyle(ChatFormatting.GRAY));
        }
    
        // 天空可见
        if (json.has("can_see_sky")) {
            lines.add(Component.translatable(
                "jei.verdant_sky.decay.condition.can_see_sky",
                json.get("can_see_sky").getAsBoolean()
            ).withStyle(ChatFormatting.GRAY));
        }
    
        // 方块状态
        if (json.has("block")) {
            lines.add(Component.translatable(
                "jei.verdant_sky.decay.condition.block",
                json.get("block").toString()
            ).withStyle(ChatFormatting.GRAY));
        }
    
        // 流体状态
        if (json.has("fluid")) {
            lines.add(Component.translatable(
                "jei.verdant_sky.decay.condition.fluid",
                json.get("fluid").toString()
            ).withStyle(ChatFormatting.GRAY));
        }
    
        // 营火上方
        if (json.has("smokey")) {
            lines.add(Component.translatable(
                "jei.verdant_sky.decay.condition.smokey",
                json.get("smokey").getAsBoolean()
            ).withStyle(ChatFormatting.GRAY));
        }
    
        if (lines.isEmpty()) {
            lines.add(Component.translatable("jei.verdant_sky.decay.condition.empty")
                .withStyle(ChatFormatting.GRAY));
        }
    
        return lines;
    }
}