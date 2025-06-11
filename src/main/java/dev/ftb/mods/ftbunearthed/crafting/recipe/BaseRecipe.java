package dev.ftb.mods.ftbunearthed.crafting.recipe;

import dev.ftb.mods.ftbunearthed.crafting.NoInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * Common base class for "machine" recipes with vanilla boilerplate handled
 */
public abstract class BaseRecipe<T extends Recipe<?>> implements Recipe<NoInventory> {
    private final RecipeSerializer<T> serializer;
    private final RecipeType<T> recipeType;

    protected BaseRecipe(Supplier<RecipeSerializer<T>> serializer, Supplier<RecipeType<T>> recipeType) {
        this.serializer = serializer.get();
        this.recipeType = recipeType.get();
    }

    @Override
    public boolean matches(NoInventory inv, Level world) {
        return true;
    }

    @Override
    public ItemStack assemble(NoInventory input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public RecipeType<T> getType() {
        return recipeType;
    }
}
