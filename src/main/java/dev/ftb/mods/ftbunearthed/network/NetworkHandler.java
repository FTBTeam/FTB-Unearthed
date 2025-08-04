package dev.ftb.mods.ftbunearthed.network;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(FTBUnearthed.MODID).versioned("1.0");

        // clientbound
        registrar.playToClient(UneartherStatusMessage.TYPE, UneartherStatusMessage.STREAM_CODEC, UneartherStatusMessage::handle);
    }
}
