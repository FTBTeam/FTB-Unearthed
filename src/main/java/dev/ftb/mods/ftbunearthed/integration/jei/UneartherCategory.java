package dev.ftb.mods.ftbunearthed.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import dev.ftb.mods.ftbunearthed.registry.ModDataComponents;
import dev.ftb.mods.ftbunearthed.registry.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class UneartherCategory extends BaseUnearthedCategory<UneartherRecipe> {
    protected UneartherCategory() {
        super(RecipeTypes.UNEARTHER,
                Component.translatable("block.ftbunearthed.core"),
                guiHelper().drawableBuilder(bgTexture("jei_unearther.png"), 0, 0, 152, 46)
                        .setTextureSize(152, 46)
                        .build(),
                guiHelper().createDrawableItemStack(ModItems.UNEARTHER.toStack()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, UneartherRecipe recipe, IFocusGroup focuses) {
        WorkerToken.WorkerData workerData = recipe.getWorkerData();
        ItemStack worker = ModItems.WORKER_TOKEN.get().getDefaultInstance();
        worker.set(ModDataComponents.WORKER_DATA, workerData.hideTooltip(true));
        builder.addSlot(RecipeIngredientRole.CATALYST,6, 15)
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(WorkerToken.tooltipLine("worker_require_profession", workerData.getProfessionName()));
                    workerData.type().ifPresent(type -> tooltip.add(WorkerToken.tooltipLine("worker_require_type", type.toString())));
                    String lvlStr = workerData.level().map(String::valueOf).orElse("1");
                    tooltip.add(WorkerToken.tooltipLine("worker_require_level", lvlStr));
                })
                .addIngredient(VanillaTypes.ITEM_STACK, worker);

        builder.addSlot(RecipeIngredientRole.CATALYST, 29, 15)
                .addIngredients(recipe.getToolItem());

        var inputBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 52, 15);
        recipe.getInputsForDisplay().forEach(input ->
                input.ifLeft(stack -> inputBuilder.addIngredient(VanillaTypes.ITEM_STACK, stack))
                        .ifRight(fluid -> inputBuilder.addFluidStack(fluid, 1000L))
        );

        int slot = 0;
        for (ItemWithChance output : recipe.getOutputs()) {
            builder.addOutputSlot(94 + slot % 3 * 18, 6 + slot / 3 * 18)
                    .addItemStack(output.item());
            slot++;
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, UneartherRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.getProcessingTime()).setPosition(70, 15);
    }

    @Override
    public void draw(UneartherRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);

        int slot = 0;
        for (ItemWithChance output : recipe.getOutputs()) {
            PoseStack stack = guiGraphics.pose();
            stack.pushPose();
            stack.translate(94 + slot % 3 * 18, 6 + slot / 3 * 18, 300f);
            stack.scale(0.5f, 0.5f, 1f);
            String weightStr = output.chance() > 0.01 ?
                    String.format("%d%%", (int)(output.chance() * 100.0)) :
                    String.format("%s%%", String.format("%.1f", output.chance() * 100.0));

            guiGraphics.drawString(Minecraft.getInstance().font, Component.literal(weightStr).withStyle(ChatFormatting.YELLOW), 0, 0, 0xFFFFFF);
            stack.popPose();
            slot++;
        }
    }
}
