package dev.ftb.mods.ftbunearthed.integration.kubejs;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public enum ItemWithChanceComponent implements RecipeComponent<ItemWithChance> {
    INSTANCE;

    private static final String ID_STR = "item_with_chance";
    private static final RecipeComponentType.Unit<ItemWithChance> TYPE
            = RecipeComponentType.unit(FTBUnearthed.id(ID_STR), INSTANCE);

    @Override
    public RecipeComponentType<?> type() {
        return TYPE;
    }

    @Override
    public Codec<ItemWithChance> codec() {
        return ItemWithChance.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ItemWithChance.class);
    }

    @Override
    public String toString() {
        return ID_STR;
    }
}
