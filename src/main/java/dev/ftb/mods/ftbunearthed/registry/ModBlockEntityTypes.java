package dev.ftb.mods.ftbunearthed.registry;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlockEntity;
import dev.ftb.mods.ftbunearthed.block.UneartherFrameBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES
            = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FTBUnearthed.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UneartherCoreBlockEntity>> UNEARTHER_CORE
            = BLOCK_ENTITY_TYPES.register("core", () -> BlockEntityType.Builder.of(UneartherCoreBlockEntity::new, ModBlocks.CORE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UneartherFrameBlockEntity>> UNEARTHER_FRAME
            = BLOCK_ENTITY_TYPES.register("frame", () -> BlockEntityType.Builder.of(UneartherFrameBlockEntity::new, ModBlocks.L_CORNER.get(), ModBlocks.L_EDGE.get(), ModBlocks.M_EDGE.get(), ModBlocks.M_FACE.get(), ModBlocks.U_CORNER.get(), ModBlocks.U_EDGE.get(), ModBlocks.U_FACE.get()).build(null));
}
