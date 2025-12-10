package dev.ftb.mods.ftbunearthed.item;

import dev.ftb.mods.ftblibrary.util.text.RainbowTextColor;
import dev.ftb.mods.ftbunearthed.config.ServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SuperBrushItem extends BrushItem {
    public SuperBrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(Style.EMPTY.withColor(RainbowTextColor.INSTANCE));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("ftbunearthed.tooltip.super_brush", ServerConfig.SUPER_BRUSH_SPEED_BOOST.get()).withStyle(ChatFormatting.GRAY));
    }
}
