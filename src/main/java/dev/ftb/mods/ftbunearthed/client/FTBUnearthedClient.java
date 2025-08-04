package dev.ftb.mods.ftbunearthed.client;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = FTBUnearthed.MODID, dist = Dist.CLIENT)
public class FTBUnearthedClient {
    public FTBUnearthedClient(IEventBus modEventBus) {
        modEventBus.addListener(FTBUnearthedClient::registerScreens);
        modEventBus.addListener(FTBUnearthedClient::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.UNEARTHER_CORE.get(), UneartherRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.WORKER.get(), WorkerRenderer::new);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.UNEARTHER_MENU.get(), UneartherScreen::new);
    }
}
