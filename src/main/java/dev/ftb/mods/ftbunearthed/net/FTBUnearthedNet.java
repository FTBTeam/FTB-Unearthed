package dev.ftb.mods.ftbunearthed.net;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = FTBUnearthed.MODID)
public class FTBUnearthedNet {
    private static final String NETWORK_VERSION = "1.0";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(FTBUnearthed.MODID)
                .versioned(NETWORK_VERSION);

        registrar.playToClient(SendMultibreakProgressMessage.TYPE, SendMultibreakProgressMessage.STREAM_CODEC, SendMultibreakProgressMessage::handle);
    }
}
