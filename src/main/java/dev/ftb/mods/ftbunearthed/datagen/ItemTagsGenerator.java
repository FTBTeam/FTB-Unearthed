package dev.ftb.mods.ftbunearthed.datagen;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.FTBUnearthedTags;
import dev.ftb.mods.ftbunearthed.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ItemTagsGenerator extends ItemTagsProvider {
    public ItemTagsGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, FTBUnearthed.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Tags.Items.TOOLS_BRUSH).add(
                ModItems.CRUDE_BRUSH.get(),
                ModItems.REINFORCED_BRUSH.get(),
                ModItems.UNBREAKABLE_BRUSH.get()
        );
        tag(ItemTags.DURABILITY_ENCHANTABLE).add(
                ModItems.CRUDE_BRUSH.get(),
                ModItems.REINFORCED_BRUSH.get()
        );
        tag(FTBUnearthedTags.Items.WORKER_TOKENS).add(ModItems.WORKER_TOKEN.get());
    }
}
