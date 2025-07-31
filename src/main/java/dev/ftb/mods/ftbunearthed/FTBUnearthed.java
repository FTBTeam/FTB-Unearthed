package dev.ftb.mods.ftbunearthed;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlockEntity;
import dev.ftb.mods.ftbunearthed.block.UneartherFrameBlockEntity;
import dev.ftb.mods.ftbunearthed.crafting.RecipeCaches;
import dev.ftb.mods.ftbunearthed.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod(FTBUnearthed.MODID)
public class FTBUnearthed {
    public static final String MODID = "ftbunearthed";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBUnearthed(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        registerAll(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerCapabilities);

//        NeoForge.EVENT_BUS.addListener(this::onItemUse);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);

        ConfigManager.getInstance().registerServerConfig(Config.CONFIG, MODID + ".settings",
                true, Config::onConfigChanged);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void registerAll(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_CONDITIONS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == FTBLibrary.getCreativeModeTab().get()) {
            event.accept(ModBlocks.CORE.asItem());
            event.accept(ModItems.REINFORCED_BRUSH.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntityTypes.UNEARTHER_CORE.get(), UneartherCoreBlockEntity::getSidedHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntityTypes.UNEARTHER_FRAME.get(), UneartherFrameBlockEntity::getSidedHandler);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CacheReloadListener());
    }

//    private void onItemUse(PlayerInteractEvent.RightClickItem event) {
//        if (event.getHand() == InteractionHand.MAIN_HAND && !event.getLevel().isClientSide) {
//            ItemStack main = event.getEntity().getMainHandItem();
//            ItemStack off = event.getEntity().getOffhandItem();
//            if (UneartherCoreBlockEntity.isKnownToolItem(event.getLevel(), main) && UneartherCoreBlockEntity.isKnownWorkerItem(event.getLevel(), off)) {
//                FTBUnearthed.LOGGER.info("rightclicking! {}", event.getLevel().getGameTime());
//                event.getEntity().swing(InteractionHand.MAIN_HAND, true);
//                event.getLevel().playSound(null, event.getEntity().blockPosition(), SoundEvents.BRUSH_GRAVEL, SoundSource.PLAYERS, 1f, 1f);
//            }
//        }
//    }

    public static class CacheReloadListener implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.runAsync(RecipeCaches::clearAll, gameExecutor).thenCompose(stage::wait);
        }
    }
}
