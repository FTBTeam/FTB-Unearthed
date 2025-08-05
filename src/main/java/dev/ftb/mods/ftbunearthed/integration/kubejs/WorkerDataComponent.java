package dev.ftb.mods.ftbunearthed.integration.kubejs;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

public enum WorkerDataComponent implements RecipeComponent<WorkerToken.WorkerData> {
    INSTANCE;

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
        return "worker_data";
    }
}
