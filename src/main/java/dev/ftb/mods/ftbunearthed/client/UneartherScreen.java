package dev.ftb.mods.ftbunearthed.client;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.menu.UneartherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

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
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        if (x >= leftPos + 8 && x <= leftPos + 14 && y >= topPos + 18 && y <= topPos + 68 && menu.getFoodBuffer() > 0) {
            guiGraphics.renderTooltip(font, Component.translatable("ftbunearthed.gui.speed_boost", + menu.getSpeedBoost()), x, y);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int uwidth = Mth.ceil(menu.getProgress() * 24.0F);
        guiGraphics.blitSprite(BURN_PROGRESS_SPRITE, 24, 16, 0, 0, leftPos + 86, topPos + 18, uwidth, 16);

        int fHeight = (int) (FOOD_BAR_HEIGHT * menu.getFoodBuffer());
        if (fHeight > 0) {
            guiGraphics.fillGradient(
                    leftPos + 8, topPos + 18 + (FOOD_BAR_HEIGHT - fHeight),
                    leftPos + 14, topPos + 18 + FOOD_BAR_HEIGHT + 1,
                    0xFF20A020, 0xFF008000
            );
        }
    }
}
