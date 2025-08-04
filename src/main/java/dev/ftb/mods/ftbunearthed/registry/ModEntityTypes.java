package dev.ftb.mods.ftbunearthed.registry;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.entity.Worker;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES
            = DeferredRegister.create(Registries.ENTITY_TYPE, FTBUnearthed.MODID);

    public static final Supplier<EntityType<Worker>> WORKER
            = register("worker", ModEntityTypes::worker);

    private static <E extends Entity> Supplier<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITY_TYPES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<Worker> worker() {
        return EntityType.Builder.of(Worker::new, MobCategory.MISC)
                .sized(0.6F, 1.95F)
                .eyeHeight(1.62F)
                .clientTrackingRange(10);
    }
}
