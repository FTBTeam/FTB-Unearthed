package dev.ftb.mods.ftbunearthed.registry;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.item.ReinforcedBrushItem;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FTBUnearthed.MODID);

    public static final DeferredItem<BlockItem> UNEARTHER
            = blockItem("core", ModBlocks.CORE);
    public static final DeferredItem<Item> REINFORCED_BRUSH
            = ITEMS.register("reinforced_brush", () -> new ReinforcedBrushItem(new Item.Properties().component(DataComponents.UNBREAKABLE, new Unbreakable(false)).stacksTo(1)));
    public static final DeferredItem<Item> WORKER_TOKEN
            = ITEMS.register("worker_token", () -> new WorkerToken(new Item.Properties()));

    public static DeferredItem<BlockItem> blockItem(String id, Supplier<? extends Block> sup) {
        return ITEMS.registerSimpleBlockItem(id, sup);
    }
}
