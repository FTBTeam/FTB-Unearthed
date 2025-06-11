package dev.ftb.mods.ftbunearthed.datagen.recipe;

import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class UneartherRecipeBuilder extends BaseRecipeBuilder<UneartherRecipe> {
    private final Ingredient workerItem;
    private final Ingredient toolItem;
    private int processingTime;
    private final List<ItemWithChance> outputs;

    public UneartherRecipeBuilder(Ingredient workerItem, Ingredient toolItem, int processingTime, List<ItemWithChance> outputs) {
        this.workerItem = workerItem;
        this.toolItem = toolItem;
        this.processingTime = processingTime;
        this.outputs = outputs;
    }

    public UneartherRecipeBuilder(Ingredient workerItem, Ingredient toolItem, List<ItemWithChance> outputs) {
        this(workerItem, toolItem, 200, outputs);
    }

    public UneartherRecipeBuilder withProcessingTime(int time) {
        processingTime = time;
        return this;
    }

    @Override
    protected UneartherRecipe buildRecipe() {
        return new UneartherRecipe(workerItem, toolItem, processingTime, outputs);
    }
}
