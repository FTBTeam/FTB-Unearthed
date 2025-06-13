package dev.ftb.mods.ftbunearthed.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class UneartherCategory extends BaseUnearthedCategory<UneartherRecipe> {
    protected UneartherCategory() {
        super(RecipeTypes.UNEARTHER,
                Component.translatable("block.ftbunearthed.unearther"),
                guiHelper().drawableBuilder(bgTexture("jei_unearther.png"), 0, 0, 148, 46)
                        .setTextureSize(148, 46)
                        .build(),
                guiHelper().createDrawableItemStack(ModItems.UNEARTHER.toStack()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, UneartherRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST,6, 15).addIngredients(recipe.getWorkerItem());
        builder.addSlot(RecipeIngredientRole.CATALYST, 29, 16).addIngredients(recipe.getToolItem());

        int slot = 0;
        for (ItemWithChance output : recipe.getOutputs()) {
            builder.addOutputSlot(90 + slot % 3 * 18, 6 + slot / 3 * 18)
                    .addItemStack(output.item());
            slot++;
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, UneartherRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.getProcessingTime()).setPosition(50, 14);
    }

    @Override
    public void draw(UneartherRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);

        int slot = 0;
        for (ItemWithChance output : recipe.getOutputs()) {
            PoseStack stack = guiGraphics.pose();
            stack.pushPose();
            stack.translate(90 + slot % 3 * 18, 6 + slot / 3 * 18, 300f);
            stack.scale(0.5f, 0.5f, 1f);
            String weightStr = String.format("%d%%", (int) (output.chance() * 100));
            guiGraphics.drawString(Minecraft.getInstance().font, Component.literal(weightStr).withStyle(ChatFormatting.YELLOW), 0, 0, 0xFFFFFF);
            stack.popPose();
            slot++;
        }
    }
}
