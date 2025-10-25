package dev.ftb.mods.ftbunearthed.client;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.config.ServerConfig;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import dev.ftb.mods.ftbunearthed.menu.UneartherMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class UneartherScreen extends AbstractContainerScreen<UneartherMenu> {
    private static final ResourceLocation TEXTURE = FTBUnearthed.id("textures/gui/unearther.png");
    private static final ResourceLocation BURN_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    private static final int FOOD_BAR_HEIGHT = 50;

    public UneartherScreen(UneartherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        imageHeight = 176;
        inventoryLabelY = 74;
    }

    @Override
    protected void init() {
        super.init();

        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x71767a, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        if (x >= leftPos + 8 && x <= leftPos + 14 && y >= topPos + 18 && y <= topPos + 68) {
            if (menu.getFoodBuffer() > 0) {
                guiGraphics.renderTooltip(font, List.of(
                        Component.translatable("ftbunearthed.gui.speed_boost", +menu.getSpeedBoost()),
                        Component.translatable("ftbunearthed.gui.food_remaining", +(int) (menu.getFoodBuffer() * ServerConfig.MAX_FOOD_BUFFER.get() / 20)).withStyle(ChatFormatting.GRAY)
                ), Optional.empty(), x, y);
            } else {
                guiGraphics.renderTooltip(font, List.of(
                        Component.translatable("ftbunearthed.gui.no_food.1"),
                        Component.translatable("ftbunearthed.gui.no_food.2").withStyle(ChatFormatting.GRAY)
                ), Optional.empty(), x, y);
            }
        } else if (x >= leftPos + 45 && x <= leftPos + 48 && y >= topPos + 52 && y <= topPos + 70) {
            int xpProgress = WorkerToken.getXPProgress(getMenu().getSlot(38).getItem());
            MutableComponent line = Component.translatable("ftbunearthed.tooltip.worker_xp_progress", Component.literal(String.valueOf(xpProgress)));
            guiGraphics.renderTooltip(font, line, x, y);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int uwidth = Mth.ceil(menu.getProgress() * 24.0F);
        guiGraphics.blitSprite(BURN_PROGRESS_SPRITE, 24, 16, 0, 0, leftPos + 86, topPos + 18, uwidth, 16);

        if (menu.getFoodBuffer() > 0f) {
            int fHeight = Math.max(1, (int) (FOOD_BAR_HEIGHT * menu.getFoodBuffer()));
            guiGraphics.fillGradient(
                    leftPos + 8, topPos + 18 + (FOOD_BAR_HEIGHT - fHeight),
                    leftPos + 14, topPos + 18 + FOOD_BAR_HEIGHT + 1,
                    0xFF20A020, 0xFF008000
            );
        }

        int xpProgress = WorkerToken.getXPProgress(getMenu().getSlot(38).getItem());
        if (xpProgress > 0) {
            int y1 = (int) (69 - 16f * xpProgress / 100f);
            guiGraphics.fill(leftPos + 45, topPos + y1, leftPos + 48, topPos + 69, 0xFFA0FF40);
        }
    }
}
