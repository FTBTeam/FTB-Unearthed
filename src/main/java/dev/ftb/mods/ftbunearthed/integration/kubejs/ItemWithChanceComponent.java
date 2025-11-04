package dev.ftb.mods.ftbunearthed.integration.kubejs;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

public class ItemWithChanceComponent extends SimpleRecipeComponent<ItemWithChance> {
    public static final TypeInfo TYPE_INFO = TypeInfo.of(ItemWithChance.class);
    public static final RecipeComponentType.Unit<ItemWithChance> TYPE
            = RecipeComponentType.unit(FTBUnearthed.id("item_with_chance"), ItemWithChanceComponent::new);

    public ItemWithChanceComponent(RecipeComponentType<?> type) {
        super(type, ItemWithChance.CODEC, TYPE_INFO);
    }
}
