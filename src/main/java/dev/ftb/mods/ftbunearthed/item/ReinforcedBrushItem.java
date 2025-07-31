package dev.ftb.mods.ftbunearthed.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.component.Unbreakable;

public class ReinforcedBrushItem extends BrushItem {
    public ReinforcedBrushItem(Properties properties) {
        super(properties.component(DataComponents.UNBREAKABLE, new Unbreakable(false)));
    }
}
