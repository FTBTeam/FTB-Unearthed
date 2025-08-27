package dev.ftb.mods.ftbunearthed.config;

import dev.ftb.mods.ftblibrary.snbt.config.DoubleValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftbunearthed.FTBUnearthed;

public interface ServerConfig {
    SNBTConfig SERVER_CONFIG = SNBTConfig.create(FTBUnearthed.MODID + "-server");

    SNBTConfig GENERAL = SERVER_CONFIG.addGroup("general");

    DoubleValue MANUAL_BRUSHING_SPEEDUP = GENERAL.addDouble("manual_brushing_speedup", 4.0, 0.05, Double.MAX_VALUE)
            .comment("Recipe duration multiplier for manual brushing, higher is faster",
                    "Recipe duration is divided by this value when using a brush manually."
            );
}
