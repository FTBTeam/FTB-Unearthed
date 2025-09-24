package dev.ftb.mods.ftbunearthed.registry;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.crafting.DevEnvironmentCondition;
import dev.ftb.mods.ftbunearthed.crafting.FTBUnearthedRecipeType;
import dev.ftb.mods.ftbunearthed.crafting.recipe.TokenResetRecipe;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS
            = DeferredRegister.create(Registries.RECIPE_SERIALIZER, FTBUnearthed.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES
            = DeferredRegister.create(Registries.RECIPE_TYPE, FTBUnearthed.MODID);
    public static final DeferredRegister<MapCodec<? extends ICondition>> RECIPE_CONDITIONS
            = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, FTBUnearthed.MODID);

    // ---------------------------

    public static final Supplier<RecipeType<UneartherRecipe>> UNEARTHER_TYPE
            = registerType("unearther", FTBUnearthedRecipeType::new);
    public static final Supplier<RecipeSerializer<UneartherRecipe>> UNEARTHER_SERIALIZER
            = RECIPE_SERIALIZERS.register("unearther", () -> new UneartherRecipe.Serializer<>(UneartherRecipe::new));
    public static final Supplier<SimpleCraftingRecipeSerializer<TokenResetRecipe>> TOKEN_RESET
            = RECIPE_SERIALIZERS.register("token_reset", () -> new SimpleCraftingRecipeSerializer<>(TokenResetRecipe::new));

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<DevEnvironmentCondition>> DEV_ENVIRONMENT
            = RECIPE_CONDITIONS.register("dev_environment", () -> DevEnvironmentCondition.CODEC);

    // ----------------------------

    private static <T extends RecipeType<?>> Supplier<T> registerType(String name, Function<String, T> factory) {
        return RECIPE_TYPES.register(name, () -> factory.apply(name));
    }
}
