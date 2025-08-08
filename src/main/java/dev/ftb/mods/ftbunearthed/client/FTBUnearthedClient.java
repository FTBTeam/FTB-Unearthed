package dev.ftb.mods.ftbunearthed.client;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModItems;
import dev.ftb.mods.ftbunearthed.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = FTBUnearthed.MODID, dist = Dist.CLIENT)
public class FTBUnearthedClient {
    private static final int[] COLORS = new int[] { 0xFFB3B1AF, 0xFFD8AF93, 0xFFFDFF76, 0xFF41F384, 0xFF33EBCB };

    public FTBUnearthedClient(IEventBus modEventBus) {
        modEventBus.addListener(FTBUnearthedClient::registerScreens);
        modEventBus.addListener(FTBUnearthedClient::registerRenderers);
        modEventBus.addListener(FTBUnearthedClient::registerItemColorHandlers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.UNEARTHER_CORE.get(), UneartherRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.WORKER.get(), WorkerRenderer::new);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.UNEARTHER_MENU.get(), UneartherScreen::new);
    }

    private static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) ->
                        WorkerToken.getWorkerData(stack)
                                .filter(data -> tintIndex == 1)
                                .map(data -> COLORS[Math.clamp(data.getVillagerLevel(), 1, 5) - 1])
                                .orElse(0xFFFFFFFF),
                ModItems.WORKER_TOKEN.asItem()
        );
    }
}
