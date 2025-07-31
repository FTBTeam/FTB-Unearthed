package dev.ftb.mods.ftbunearthed.datagen;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemModelsGenerator extends ItemModelProvider {
    public ItemModelsGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, FTBUnearthed.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        handheldItem(ModItems.REINFORCED_BRUSH.get());
    }
}
