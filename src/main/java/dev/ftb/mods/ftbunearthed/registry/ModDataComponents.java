package dev.ftb.mods.ftbunearthed.registry;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS
            = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, FTBUnearthed.MODID);

    // villager data saved onto a worker token (or potentially any other item)
    public static final Supplier<DataComponentType<WorkerToken.WorkerData>> WORKER_DATA
            = COMPONENTS.registerComponentType("worker_data", builder -> builder
            .persistent(WorkerToken.WorkerData.COMPONENT_CODEC)
            .networkSynchronized(WorkerToken.WorkerData.STREAM_CODEC)
    );

    // worker XP (not to be confused with villager XP) - increases as worker brushes things
    public static final Supplier<DataComponentType<Integer>> WORKER_XP
            = COMPONENTS.registerComponentType("worker_xp", builder -> builder
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT)
    );
}
