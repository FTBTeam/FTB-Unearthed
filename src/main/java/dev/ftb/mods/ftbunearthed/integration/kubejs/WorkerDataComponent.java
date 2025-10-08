package dev.ftb.mods.ftbunearthed.integration.kubejs;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public enum WorkerDataComponent implements RecipeComponent<WorkerToken.WorkerData> {
    INSTANCE;

    private static final String ID_STR = "worker_data";
    private static final RecipeComponentType.Unit<WorkerToken.WorkerData> TYPE
            = RecipeComponentType.unit(FTBUnearthed.id(ID_STR), INSTANCE);

    @Override
    public RecipeComponentType<?> type() {
        return TYPE;
    }

    @Override
    public Codec<WorkerToken.WorkerData> codec() {
        return WorkerToken.WorkerData.COMPONENT_CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(WorkerToken.WorkerData.class);
    }

    @Override
    public String toString() {
        return ID_STR;
    }
}
