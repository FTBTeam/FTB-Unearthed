package dev.ftb.mods.ftbunearthed.client;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = FTBUnearthed.MODID, dist = Dist.CLIENT)
public class FTBEchoesClient {
    public FTBEchoesClient(IEventBus modEventBus) {
        modEventBus.addListener(FTBEchoesClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.UNEARTHER_MENU.get(), UneartherScreen::new);
    }
}
