package dev.ftb.mods.ftbunearthed.integration.jei;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.crafting.IHideableRecipe;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import dev.ftb.mods.ftbunearthed.registry.ModBlocks;
import dev.ftb.mods.ftbunearthed.registry.ModItems;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@JeiPlugin
public class UnearthedJeiPlugin implements IModPlugin {
    static IJeiHelpers jeiHelpers;

    private static final ResourceLocation ID = FTBUnearthed.id("default");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();

        registration.addRecipeCategories(new UneartherCategory());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        addRecipeType(registration, ModRecipes.UNEARTHER_TYPE.get(), RecipeTypes.UNEARTHER);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.CORE.toStack(), RecipeTypes.UNEARTHER);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.WORKER_TOKEN.get(), new ISubtypeInterpreter<>() {
            @Override
            public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
                return WorkerToken.getWorkerData(ingredient).profession();
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
                return "";
            }
        });
    }

    private <I extends RecipeInput, T extends Recipe<I>> void addRecipeType(IRecipeRegistration registration, net.minecraft.world.item.crafting.RecipeType<T> mcRecipeType, RecipeType<T> jeiRecipeType) {
        addRecipeType(registration, mcRecipeType, jeiRecipeType, Function.identity());
    }

    private <I extends RecipeInput, T extends Recipe<I>> void addRecipeType(IRecipeRegistration registration, net.minecraft.world.item.crafting.RecipeType<T> mcRecipeType, RecipeType<T> jeiRecipeType, Function<List<T>, List<T>> postProcessor) {
        List<T> recipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(mcRecipeType).stream()
                .map(RecipeHolder::value)
                .sorted()
                .filter(IHideableRecipe::shouldShow)
                .toList();
        registration.addRecipes(jeiRecipeType, postProcessor.apply(recipes.reversed()));
    }
}
