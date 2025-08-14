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

        add(ModItems.CRUDE_BRUSH.get(), "Crude Brush");
        add(ModItems.REINFORCED_BRUSH.get(), "Reinforced Brush");
        add(ModItems.UNBREAKABLE_BRUSH.get(), "Unbreakable Brush");
        add(ModItems.WORKER_TOKEN.get(), "Worker Token");
        add(ModItems.ECHO_ENCODER.get(), "Echo Encoder");

        add(FTBUnearthed.MODID + ".gui.speed_boost", "Speed Boost: %s%%");
        add(FTBUnearthed.MODID + ".gui.food_remaining", "Remaining: %s");
        add(FTBUnearthed.MODID + ".gui.no_food.1", "No Speed Boost!");
        add(FTBUnearthed.MODID + ".gui.no_food.2", "Insert some food in the top-left slot");

        add(FTBUnearthed.MODID + ".message.unearther_level", "Unearther Level: %s (%s)");
        add(FTBUnearthed.MODID + ".message.unearther_level.player", "Unearther Level for '%s': %s (%s)");
        add(FTBUnearthed.MODID + ".message.item_too_damaged", "%s is too damaged!");

        add(FTBUnearthed.MODID + ".tooltip.echo_encoder_usage", "Sneak + Right-click a villager to encode it into a Worker Token");
        add(FTBUnearthed.MODID + ".tooltip.echo_encoder_charges", "Charges remaining: %s");

        add(FTBUnearthed.MODID + ".tooltip.worker_profession", "Profession: %s");
        add(FTBUnearthed.MODID + ".tooltip.worker_type", "Village Type: %s");
        add(FTBUnearthed.MODID + ".tooltip.worker_level", "Level: %s");

        add(FTBUnearthed.MODID + ".tooltip.worker_require_profession", "Required Profession: %s");
        add(FTBUnearthed.MODID + ".tooltip.worker_require_type", "Required Village Type: %s");
        add(FTBUnearthed.MODID + ".tooltip.worker_require_level", "Required Level: >= %s");

        add(FTBUnearthed.MODID + ".villager_type.plains", "Plains");
        add(FTBUnearthed.MODID + ".villager_type.desert", "Desert");
        add(FTBUnearthed.MODID + ".villager_type.jungle", "Plains");
        add(FTBUnearthed.MODID + ".villager_type.savanna", "Plains");
        add(FTBUnearthed.MODID + ".villager_type.snow", "Plains");
        add(FTBUnearthed.MODID + ".villager_type.taiga", "Plains");
        add(FTBUnearthed.MODID + ".villager_type.swamp", "Plains");
    }
}
