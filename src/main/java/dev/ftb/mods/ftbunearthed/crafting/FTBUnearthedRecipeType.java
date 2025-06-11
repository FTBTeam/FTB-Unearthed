package dev.ftb.mods.ftbunearthed.crafting;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class FTBUnearthedRecipeType<T extends Recipe<?>> implements RecipeType<T> {
    private final String name;

    public FTBUnearthedRecipeType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
