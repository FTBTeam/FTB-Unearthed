package dev.ftb.mods.ftbunearthed.integration.kubejs;

import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface UneartherRecipeSchema {
    RecipeKey<List<ItemWithChance>> RESULTS = ItemWithChanceComponent.INSTANCE.asList().outputKey("outputs");
    RecipeKey<String> INPUT_BLOCK = StringComponent.ANY.inputKey("input_block");
    RecipeKey<Ingredient> WORKER_ITEM = IngredientComponent.INGREDIENT.inputKey("worker_item");
    RecipeKey<Ingredient> TOOL_ITEM = IngredientComponent.INGREDIENT.inputKey("tool_item");
    RecipeKey<Integer> TIME = NumberComponent.INT.otherKey("processing_time").optional(200);

    RecipeSchema SCHEMA = new RecipeSchema(RESULTS, INPUT_BLOCK, WORKER_ITEM, TOOL_ITEM, TIME);
}
