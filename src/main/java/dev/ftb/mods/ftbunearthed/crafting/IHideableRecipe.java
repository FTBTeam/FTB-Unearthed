package dev.ftb.mods.ftbunearthed.crafting;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

@FunctionalInterface
public interface IHideableRecipe {
    static <I extends RecipeInput, T extends Recipe<I>> boolean shouldShow(T t) {
        return !(t instanceof IHideableRecipe h) || h.shouldShowRecipe();
    }

    boolean shouldShowRecipe();
}
