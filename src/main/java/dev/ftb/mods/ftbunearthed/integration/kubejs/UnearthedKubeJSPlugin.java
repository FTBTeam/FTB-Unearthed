package dev.ftb.mods.ftbunearthed.integration.kubejs;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;

public class UnearthedKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(FTBUnearthed.id("unearther"), UneartherRecipeSchema.SCHEMA);

        FTBUnearthed.LOGGER.info("Registered KubeJS recipe schema");
    }
}
