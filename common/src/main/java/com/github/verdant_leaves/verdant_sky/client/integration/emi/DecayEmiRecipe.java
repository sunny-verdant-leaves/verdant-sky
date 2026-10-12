package com.github.verdant_leaves.verdant_sky.client.integration.emi;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.github.verdant_leaves.verdant_sky.VerdantSky;
import com.github.verdant_leaves.verdant_sky.client.integration.jei.helper.LocationPredicateFormatter;
import com.github.verdant_leaves.verdant_sky.recipe.DecayRecipe;
import com.github.verdant_leaves.verdant_sky.registry.ModBlocks;

public class DecayEmiRecipe extends VerdantEmiRecipe {

    private static final ResourceLocation TEXTURE = new ResourceLocation(VerdantSky.MOD_ID, "textures/gui/decay_overlay.png");
    private static final EmiStack DECAY_FLOWER = EmiStack.of(ModBlocks.DECAY_AGAPANTHUS.get());

    private final DecayRecipe recipe;

    public DecayEmiRecipe(DecayRecipe recipe) {
        super(VerdantSkyEmiPlugin.DECAY, recipe);
        this.recipe = recipe;

        // 输入
        this.input = List.of(EmiIngredient.of(
            recipe.input().getDisplayed().stream()
                .map(s -> s.getFluidState().isEmpty()
                    ? EmiStack.of(s.getBlock())
                    : EmiStack.of(s.getFluidState().getType()))
                .toList()));
    
        // 输出
        var out = recipe.outputState();
        this.output = List.of(
            !out.getFluidState().isEmpty()
                ? EmiStack.of(out.getFluidState().getType())
                : EmiStack.of(out.getBlock().asItem()));

        // 催化剂
        this.catalysts = List.of(DECAY_FLOWER);
    }

    @Override
    public int getDisplayWidth() {
        return 96;
    }

    @Override
    public int getDisplayHeight() {
        return 44;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // overlay
        widgets.addDrawable(17, 0, 64, 44, (gui, mouseX, mouseY, delta) -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            gui.blit(TEXTURE, 0, 0, 0, 0, 64, 44, 256, 256);
            RenderSystem.disableBlend();
        });

        // 槽位
        widgets.addSlot(input.get(0), 10, 13).drawBack(false);
        widgets.addSlot(DECAY_FLOWER, 39, 13).catalyst(true).drawBack(false);
        widgets.addSlot(output.get(0), 68, 13).drawBack(false).recipeContext(this);

        // 输入原料
        var inputTooltip = recipe.input().descriptionTooltip();
        if (!inputTooltip.isEmpty()) {
            widgets.addTooltipText(inputTooltip, 10, 13, 16, 16);
        }

        // 条件提示
        addConditionWidgets(widgets);
    }

    // tooltip
    private void addConditionWidgets(WidgetHolder widgets) {
        if (recipe.locationPredicate() == null) return;

        var font = Minecraft.getInstance().font;
        var shortHint = Component.translatable("jei.verdant_sky.condition.required.short");
        int x = getDisplayWidth() - font.width(shortHint) - 2;

        widgets.addText(shortHint, x, 2, 0x888888, false);

        var lines = new ArrayList<Component>();
        lines.add(Component.translatable("jei.verdant_sky.condition.required"));
        lines.addAll(LocationPredicateFormatter.format(recipe.locationPredicate()));
        widgets.addTooltipText(lines, x, 2, font.width(shortHint), font.lineHeight);
    }
}