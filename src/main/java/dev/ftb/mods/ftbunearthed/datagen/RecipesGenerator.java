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
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RecipesGenerator extends RecipeProvider {
    public RecipesGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        new UneartherRecipeBuilder("minecraft:sand", Ingredient.of(Items.NAME_TAG), Ingredient.of(Tags.Items.TOOLS_BRUSH), List.of(
                new ItemWithChance(new ItemStack(Items.DIAMOND, 2), 0.1),
                new ItemWithChance(Items.AMETHYST_SHARD.getDefaultInstance(), 0.3),
                new ItemWithChance(Items.EMERALD.getDefaultInstance(), 0.5),
                new ItemWithChance(Items.LAPIS_LAZULI.getDefaultInstance(), 1.5)
        )).withProcessingTime(40).saveTest(output, FTBUnearthed.id("unearthing_sand"));

        new UneartherRecipeBuilder("minecraft:gravel", Ingredient.of(Items.NAME_TAG), Ingredient.of(Tags.Items.TOOLS_BRUSH), List.of(
                new ItemWithChance(new ItemStack(Items.RAW_IRON, 2), 0.6),
                new ItemWithChance(Items.RAW_COPPER.getDefaultInstance(), 0.3),
                new ItemWithChance(Items.RAW_GOLD.getDefaultInstance(), 0.1),
                new ItemWithChance(Items.REDSTONE.getDefaultInstance(), 0.1)
        )).withProcessingTime(60).withToolDamageChance(0.25f).saveTest(output, FTBUnearthed.id("unearthing_gravel"));
    }
}
