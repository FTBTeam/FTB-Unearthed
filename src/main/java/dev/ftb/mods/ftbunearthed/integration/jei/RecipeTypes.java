package dev.ftb.mods.ftbunearthed.integration.jei;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeTypes {
    public static final RecipeType<UneartherRecipe> UNEARTHER = register("unearther", UneartherRecipe.class);

    private static <T extends Recipe<?>> RecipeType<T> register(String name, Class<T> recipeClass) {
        return RecipeType.create(FTBUnearthed.MODID, name, recipeClass);
    }
}
