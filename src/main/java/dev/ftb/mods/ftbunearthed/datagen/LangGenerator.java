package dev.ftb.mods.ftbunearthed.datagen;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlock;
import dev.ftb.mods.ftbunearthed.block.UneartherFrameBlock;
import dev.ftb.mods.ftbunearthed.registry.ModBlocks;
import dev.ftb.mods.ftbunearthed.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class LangGenerator extends LanguageProvider {
    public LangGenerator(PackOutput packOutput) {
        super(packOutput, FTBUnearthed.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        ModBlocks.BLOCKS.getEntries().forEach(b -> {
            if (b.get() instanceof UneartherFrameBlock || b.get() instanceof UneartherCoreBlock) {
                add(b.get(), "Unearther");
            }
        });

        add(ModItems.REINFORCED_BRUSH.get(), "Reinforced Brush");

        add(FTBUnearthed.MODID + ".gui.speed_boost", "Speed Boost: %s%%");
    }
}
