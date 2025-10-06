package dev.ftb.mods.ftbunearthed.config;

import dev.ftb.mods.ftblibrary.snbt.config.DoubleValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;
import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerType;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

public interface ServerConfig {
    SNBTConfig SERVER_CONFIG = SNBTConfig.create(FTBUnearthed.MODID + "-server");

    SNBTConfig GENERAL = SERVER_CONFIG.addGroup("general");

    DoubleValue MANUAL_BRUSHING_SPEEDUP = GENERAL.addDouble("manual_brushing_speedup", 4.0, 0.05, Double.MAX_VALUE)
            .comment("Recipe duration multiplier for manual brushing, higher is faster",
                    "Recipe duration is divided by this value when using a brush manually."
            );


    SNBTConfig UNEARTHER = SERVER_CONFIG.addGroup("unearther");

    IntValue MAX_FOOD_BUFFER = UNEARTHER.addInt("max_food_buffer", 24000, 1, Integer.MAX_VALUE)
            .comment("Maximum amount of food points (drumsticks) that can be stored in an Unearther");

    IntValue FOOD_SATURATION_MULTIPLIER = UNEARTHER.addInt("food_saturation_multiplier", 1, 1, Integer.MAX_VALUE)
            .comment("Used to multiply the amount of saturation a food item can provide");

    IntValue FOOD_SPEED_BOOST_MULTIPLIER = UNEARTHER.addInt("food_speed_boost_multiplier", 1, 1, Integer.MAX_VALUE)
            .comment("Used to multiply the amount of speed boost a food item can provide");

}
