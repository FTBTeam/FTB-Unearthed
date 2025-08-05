package dev.ftb.mods.ftbunearthed;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class FTBUnearthedTags {
    public static class Items {
        public static final TagKey<Item> WORKER_TOKENS
                = TagKey.create(Registries.ITEM, FTBUnearthed.id("worker_tokens"));
    }
}
