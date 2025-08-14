package dev.ftb.mods.ftbunearthed.datagen.recipe;

import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.item.WorkerToken.WorkerData;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class UneartherRecipeBuilder extends BaseRecipeBuilder<UneartherRecipe> {
    private final String inputStateStr;
    private final WorkerData workerData;
    private final Ingredient toolItem;
    private final List<ItemWithChance> outputs;
    private int processingTime;
    private float damageChance;

    public UneartherRecipeBuilder(String inputStateStr, WorkerData workerData, Ingredient toolItem, List<ItemWithChance> outputs) {
        this.inputStateStr = inputStateStr;
        this.workerData = workerData;
        this.toolItem = toolItem;
        this.outputs = outputs;
        this.processingTime = 200;
        this.damageChance = 1.0f;
    }

    public UneartherRecipeBuilder withProcessingTime(int time) {
        processingTime = time;
        return this;
    }

    public UneartherRecipeBuilder withToolDamageChance(float damageChance) {
        this.damageChance = damageChance;
        return this;
    }

    @Override
    protected UneartherRecipe buildRecipe() {
        return new UneartherRecipe(inputStateStr, workerData, toolItem, processingTime, outputs, damageChance);
    }
}
