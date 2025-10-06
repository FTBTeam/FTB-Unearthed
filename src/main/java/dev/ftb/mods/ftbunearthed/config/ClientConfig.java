package dev.ftb.mods.ftbunearthed.config;

import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;
import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerType;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

public interface ClientConfig {
    SNBTConfig CLIENT_CONFIG = SNBTConfig.create(FTBUnearthed.MODID + "-client");

    SNBTConfig GENERAL = CLIENT_CONFIG.addGroup("general");

    StringValue RAW_VILLAGER_TYPE = GENERAL.addString("default_villager_type", "minecraft:plains")
            .comment("Default village type for villager tokens in creative/JEI",
                    "This should be a valid resource location for a village type in the VILLAGER_TYPE registry",
                    "e.g. \"minecraft:plains\", \"minecraft:savanna\" etc., or a custom type such as \"ftb:stone\"");
    Lazy<VillagerType> DEFAULT_VILLAGER_TYPE = Lazy.of(ClientConfig::parseVillagerType);

    private static @NotNull VillagerType parseVillagerType() {
        String typeStr = RAW_VILLAGER_TYPE.get();
        try {
            ResourceLocation type = ResourceLocation.parse(typeStr);
            return BuiltInRegistries.VILLAGER_TYPE.getOptional(type).orElseGet(() -> {
                FTBUnearthed.LOGGER.error("unknown villager type '{}'", typeStr);
                return VillagerType.PLAINS;
            });
        } catch (ResourceLocationException e) {
            FTBUnearthed.LOGGER.error("invalid villager type '{}'", typeStr);
            return VillagerType.PLAINS;
        }
    }

    static void onChanged(boolean onServer) {
        DEFAULT_VILLAGER_TYPE.invalidate();
    }
}
