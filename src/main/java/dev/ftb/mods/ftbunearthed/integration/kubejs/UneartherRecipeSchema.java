package dev.ftb.mods.ftbunearthed.integration.kubejs;

import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface UneartherRecipeSchema {
    RecipeKey<Ingredient> WORKER_ITEM = IngredientComponent.INGREDIENT.key("worker_item", ComponentRole.INPUT);
    RecipeKey<Ingredient> TOOL_ITEM = IngredientComponent.INGREDIENT.key("tool_item", ComponentRole.INPUT);
    RecipeKey<Integer> TIME = NumberComponent.INT.key("processing_time", ComponentRole.OTHER).optional(200);
    RecipeKey<List<ItemWithChance>> RESULTS = ItemWithChanceComponent.INSTANCE.asList().key("outputs", ComponentRole.OUTPUT);

    RecipeSchema SCHEMA = new RecipeSchema(RESULTS, WORKER_ITEM, TOOL_ITEM, TIME);
}
