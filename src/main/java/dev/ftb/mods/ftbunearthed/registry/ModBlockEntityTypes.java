package dev.ftb.mods.ftbunearthed.registry;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES
            = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FTBUnearthed.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UneartherCoreBlockEntity>> UNEARTHER_CORE
            = BLOCK_ENTITY_TYPES.register("unearther", () -> BlockEntityType.Builder.of(UneartherCoreBlockEntity::new, ModBlocks.UNEARTHER.get()).build(null));
}
