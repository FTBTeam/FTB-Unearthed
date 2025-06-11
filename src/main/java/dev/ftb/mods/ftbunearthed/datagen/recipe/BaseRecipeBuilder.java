package dev.ftb.mods.ftbunearthed.datagen.recipe;

import dev.ftb.mods.ftbunearthed.crafting.DevEnvironmentCondition;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRecipeBuilder<T extends Recipe<?>> implements RecipeBuilder {
    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    @Override
    public Item getResult() {
        return Items.AIR;
    }

    abstract protected T buildRecipe();

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        T recipe = buildRecipe();
        ResourceLocation id1 = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), recipe.getType() + "/" + id.getPath());
        recipeOutput.accept(id1, recipe, null);
    }

    public void saveTest(RecipeOutput recipeOutput, ResourceLocation id) {
        T recipe = buildRecipe();
        ResourceLocation id1 = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), recipe.getType() + "/dev_test_" + id.getPath());
        recipeOutput.withConditions(DevEnvironmentCondition.INSTANCE).accept(id1, recipe, null);
    }
}
