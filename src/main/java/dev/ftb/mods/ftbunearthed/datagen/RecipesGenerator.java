package dev.ftb.mods.ftbunearthed.datagen;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.ftb.mods.ftbunearthed.datagen.recipe.UneartherRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RecipesGenerator extends RecipeProvider {
    public RecipesGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        new UneartherRecipeBuilder(Ingredient.of(Items.NAME_TAG), Ingredient.of(Items.BRUSH), List.of(
                new ItemWithChance(new ItemStack(Items.DIAMOND, 2), 0.1),
                new ItemWithChance(Items.AMETHYST_SHARD.getDefaultInstance(), 0.3),
                new ItemWithChance(Items.EMERALD.getDefaultInstance(), 0.5),
                new ItemWithChance(Items.LAPIS_LAZULI.getDefaultInstance(), 1.5)
        )).withProcessingTime(40).saveTest(output, FTBUnearthed.id("name_tag_and_brush"));
    }
}
