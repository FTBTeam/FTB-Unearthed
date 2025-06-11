package dev.ftb.mods.ftbunearthed.crafting;

import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlockEntity;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;

public interface RecipeCaches {
    RecipeCache<UneartherRecipe> UNEARTHER = new RecipeCache<>();

    static void clearAll() {
        UNEARTHER.clear();
        UneartherCoreBlockEntity.clearKnownItemCaches();
    }
}
