package dev.ftb.mods.ftbunearthed.client;

import dev.ftb.mods.ftbunearthed.registry.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientSetup {
    public static void onModConstruction(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.UNEARTHER_MENU.get(), UneartherScreen::new);
    }
}
