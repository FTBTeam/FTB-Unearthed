package dev.ftb.mods.ftbunearthed;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public interface Config {
    SNBTConfig CONFIG = SNBTConfig.create(FTBUnearthed.MODID);

    SNBTConfig GENERAL = CONFIG.addGroup("general");

    BooleanValue INCLUDE_DEV_RECIPES = GENERAL.addBoolean("include_dev_recipes", false)
            .comment("If true, dev/testing recipes will be available outside a development environment",
                    "Leave this false unless actually testing the mod."
            );

    static void onConfigChanged(boolean isServerSide) {

    }
}
