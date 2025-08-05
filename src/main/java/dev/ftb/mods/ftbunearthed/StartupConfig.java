package dev.ftb.mods.ftbunearthed;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;

public interface StartupConfig {
    SNBTConfig STARTUP_CONFIG = SNBTConfig.create(FTBUnearthed.MODID + "-startup");

    SNBTConfig GENERAL = STARTUP_CONFIG.addGroup("general");

    BooleanValue INCLUDE_DEV_RECIPES = GENERAL.addBoolean("include_dev_recipes", false)
            .comment("If true, dev/testing recipes will be available outside a development environment",
                    "Leave this false unless actually testing the mod."
            );
}
