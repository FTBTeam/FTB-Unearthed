package dev.ftb.mods.ftbunearthed.crafting;

import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlockEntity;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.List;

public interface RecipeCaches {
    RecipeCache<UneartherRecipe> UNEARTHER = new RecipeCache<>();
    RecipeCache<UneartherRecipe> MANUAL_BRUSHING = new RecipeCache<>();

    static void clearAll() {
        UNEARTHER.clear();
        MANUAL_BRUSHING.clear();
        UneartherCoreBlockEntity.clearKnownItemCaches();
        SortedRecipeCache.sortedRecipes = null;
    }

    static List<RecipeHolder<UneartherRecipe>> sortedUneartherRecipes(Level level) {
        if (SortedRecipeCache.sortedRecipes == null) {
            SortedRecipeCache.sortedRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.UNEARTHER_TYPE.get()).stream()
                    .sorted((o1, o2) -> o2.value().compareTo(o1.value()))  // sort recipes with higher unearther level first
                    .toList();
        }
        return SortedRecipeCache.sortedRecipes;
    }

    class SortedRecipeCache {
        private static List<RecipeHolder<UneartherRecipe>> sortedRecipes = null;
    }
}
