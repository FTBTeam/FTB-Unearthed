package dev.ftb.mods.ftbunearthed.datagen;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.ftb.mods.ftbunearthed.datagen.recipe.UneartherRecipeBuilder;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import dev.ftb.mods.ftbunearthed.item.WorkerToken.WorkerData;
import dev.ftb.mods.ftbunearthed.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WORKER_TOKEN.asItem())
                .requires(ModItems.WORKER_TOKEN.asItem())
                .unlockedBy("has_worker_token", has(ModItems.WORKER_TOKEN.asItem()))
                .save(output, FTBUnearthed.id("worker_token_reset"));

        new UneartherRecipeBuilder("minecraft:sand", new WorkerData(VillagerProfession.MASON), Ingredient.of(Tags.Items.TOOLS_BRUSH), List.of(
                new ItemWithChance(new ItemStack(Items.DIAMOND, 2), 0.1),
                new ItemWithChance(Items.AMETHYST_SHARD.getDefaultInstance(), 0.3),
                new ItemWithChance(Items.EMERALD.getDefaultInstance(), 0.5),
                new ItemWithChance(Items.LAPIS_LAZULI.getDefaultInstance(), 1.5)
        )).withProcessingTime(40).saveTest(output, FTBUnearthed.id("unearthing_sand"));

        new UneartherRecipeBuilder("minecraft:snow_block", new WorkerData(VillagerProfession.FISHERMAN, VillagerType.SNOW), Ingredient.of(Tags.Items.TOOLS_BRUSH), List.of(
                new ItemWithChance(new ItemStack(Items.SNOWBALL, 2), 0.2),
                new ItemWithChance(Items.BLUE_ICE.getDefaultInstance(), 0.1),
                new ItemWithChance(Items.SNOW_GOLEM_SPAWN_EGG.getDefaultInstance(), 0.005)
        )).withProcessingTime(25).saveTest(output, FTBUnearthed.id("unearthing_snow"));

        new UneartherRecipeBuilder("minecraft:gravel", new WorkerData(VillagerProfession.MASON), Ingredient.of(Tags.Items.TOOLS_BRUSH), List.of(
                new ItemWithChance(new ItemStack(Items.RAW_IRON, 2), 0.6),
                new ItemWithChance(Items.RAW_COPPER.getDefaultInstance(), 0.3),
                new ItemWithChance(Items.RAW_GOLD.getDefaultInstance(), 0.1),
                new ItemWithChance(Items.REDSTONE.getDefaultInstance(), 0.1)
        )).withProcessingTime(60).withToolDamageChance(0.25f).saveTest(output, FTBUnearthed.id("unearthing_gravel_1"));

        new UneartherRecipeBuilder("minecraft:gravel", new WorkerData(VillagerProfession.MASON, 3), Ingredient.of(Tags.Items.TOOLS_BRUSH), List.of(
                new ItemWithChance(new ItemStack(Items.DIAMOND_ORE, 1), 0.75),
                new ItemWithChance(new ItemStack(Items.EMERALD_ORE, 1), 0.1)
        )).withProcessingTime(60).withToolDamageChance(0.25f).saveTest(output, FTBUnearthed.id("unearthing_gravel_3"));

        new UneartherRecipeBuilder("#c:cobblestones", new WorkerData(VillagerProfession.MASON, 1), Ingredient.of(Tags.Items.TOOLS_BRUSH), List.of(
                new ItemWithChance(new ItemStack(Items.STONE_BUTTON, 1), 1)
        )).withProcessingTime(200).withToolDamageChance(0.5f).saveTest(output, FTBUnearthed.id("unearthing_cobblestones"));
    }
}
