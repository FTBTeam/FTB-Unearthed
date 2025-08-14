package dev.ftb.mods.ftbunearthed.datagen;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ItemModelsGenerator extends ItemModelProvider {
    public ItemModelsGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, FTBUnearthed.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        handheldItem(ModItems.CRUDE_BRUSH.get());
        handheldItem(ModItems.REINFORCED_BRUSH.get());
        handheldItem(ModItems.UNBREAKABLE_BRUSH.get());

        handheldItem(ModItems.ECHO_ENCODER.get());

        simpleLayeredItem(ModItems.WORKER_TOKEN, "item/worker_token", "item/worker_layer1");
    }

    private ItemModelBuilder simpleLayeredItem(DeferredItem<? extends Item> item, String... textures) {
        return simpleLayeredItem(item.getId(), textures);
    }

    private ItemModelBuilder simpleLayeredItem(ResourceLocation itemKey, String... textures) {
        ItemModelBuilder builder = withExistingParent(itemKey.getPath(), "item/generated");
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }
}
