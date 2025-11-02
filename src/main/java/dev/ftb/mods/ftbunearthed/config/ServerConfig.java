package dev.ftb.mods.ftbunearthed.config;

import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerType;

import java.util.Optional;

public interface ServerConfig {
    SNBTConfig SERVER_CONFIG = SNBTConfig.create(FTBUnearthed.MODID + "-server");

    SNBTConfig GENERAL = SERVER_CONFIG.addGroup("general");

    DoubleValue MANUAL_BRUSHING_SPEEDUP = GENERAL.addDouble("manual_brushing_speedup", 4.0, 0.05, Double.MAX_VALUE)
            .comment("Recipe duration multiplier for manual brushing, higher is faster",
                    "Recipe duration is divided by this value when using a brush manually."
            );
    IntValue ENCODER_MAX_USES = GENERAL.addInt("encoder_max_uses", 10, 1, Integer.MAX_VALUE)
            .comment("Maximum number of times an encode may be used");

    BooleanValue ENCODER_KEEPS_VILLAGER_LEVEL = GENERAL.addBoolean("encoder_keeps_village_level", false)
            .comment("When encoding a villager, should the villager's level be preserved?",
                    "Setting this to true makes the Encoder a convenient way to move villagers around...");

    StringValue ENCODED_VILLAGER_TYPE = GENERAL.addString("encoded_villager_type", "ftb:stone")
            .comment("Villager type to force encoding to. If this is empty or an invalid villager type,",
                    "the villager current type is kept.");

    SNBTConfig UNEARTHER = SERVER_CONFIG.addGroup("unearther");

    IntValue MAX_FOOD_BUFFER = UNEARTHER.addInt("max_food_buffer", 24000, 1, Integer.MAX_VALUE)
            .comment("Maximum amount of food points (drumsticks) that can be stored in an Unearther");

    IntValue FOOD_SATURATION_MULTIPLIER = UNEARTHER.addInt("food_saturation_multiplier", 1, 1, Integer.MAX_VALUE)
            .comment("Used to multiply the amount of saturation a food item can provide",
                    "Food saturation determines how long the speed boost will last");

    IntValue FOOD_SPEED_BOOST_MULTIPLIER = UNEARTHER.addInt("food_speed_boost_multiplier", 1, 1, Integer.MAX_VALUE)
            .comment("Used to multiply the amount of speed boost a food item can provide");


    static Optional<VillagerType> encodedVillagerType() {
        if (!ENCODED_VILLAGER_TYPE.get().isEmpty()) {
            try {
                ResourceLocation typeId = ResourceLocation.parse(ENCODED_VILLAGER_TYPE.get());
                return BuiltInRegistries.VILLAGER_TYPE
                        .getOptional(ResourceKey.create(Registries.VILLAGER_TYPE, typeId))
                        .or(() -> {
                            FTBUnearthed.LOGGER.error("unknown 'encoded_villager_type' {}, ignoring", ENCODED_VILLAGER_TYPE.get());
                            return Optional.empty();
                        });
            } catch (ResourceLocationException e) {
                FTBUnearthed.LOGGER.error("invalid 'encoded_villager_type' {}, ignoring", ENCODED_VILLAGER_TYPE.get());
            }
        }

        return Optional.empty();
    }
}
