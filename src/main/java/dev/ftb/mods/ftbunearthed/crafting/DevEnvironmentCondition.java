package dev.ftb.mods.ftbunearthed.crafting;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbunearthed.config.StartupConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.conditions.ICondition;

public enum DevEnvironmentCondition implements ICondition {
    INSTANCE;

    public static final MapCodec<DevEnvironmentCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean test(IContext context) {
        return StartupConfig.INCLUDE_DEV_RECIPES.get() || !FMLLoader.isProduction();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
