package dev.ftb.mods.ftbunearthed.registry;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES
            = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FTBUnearthed.MODID);

    public static final Supplier<AttachmentType<Integer>> UNEARTHER_LEVEL = ATTACHMENT_TYPES.register(
            "unearther_level", () -> AttachmentType.builder(() -> 1)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build()
    );
}
