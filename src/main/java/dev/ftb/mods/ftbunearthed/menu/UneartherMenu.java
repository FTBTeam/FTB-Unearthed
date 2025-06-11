package dev.ftb.mods.ftbunearthed.menu;

import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlockEntity;
import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.SlotItemHandler;

public class UneartherMenu extends AbstractContainerMenu {
    public static final int PLAYER_INV_Y = 84;

    private final UneartherCoreBlockEntity unearther;

    public UneartherMenu(int containerId, Inventory invPlayer, BlockPos pos) {
        super(ModMenuTypes.UNEARTHER_MENU.get(), containerId);

        unearther = invPlayer.player.level().getBlockEntity(pos, ModBlockEntityTypes.UNEARTHER_CORE.get())
                .orElseThrow(() -> new IllegalStateException("unearther block entity missing at " + pos));

        // player's main inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                int slot = 9 + y * 9 + x;
                addSlot(new Slot(invPlayer, slot, 8 + x * 18, PLAYER_INV_Y + y * 18));
            }
        }
        // player's hotbar
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(invPlayer, x, 8 + x * 18, PLAYER_INV_Y + 58));
        }

        // worker item slot
        addSlot(new SlotItemHandler(unearther.getWorkerItemHandler(), 0, 14, 35));
        // tool slot
        addSlot(new SlotItemHandler(unearther.getToolItemHandler(), 0, 37, 35));

        // item output slots
        for (int i = 0; i < 6; i++) {
            addSlot(new OutputOnlySlot(unearther.getInternalOutputHandler(), i, 98 + 18 * (i % 3), 26 + 18 * (i / 3)));
        }

        addDataSlot(unearther.getProgressSlot());
        addDataSlot(unearther.getProcessingTimeSlot());
    }

    public static UneartherMenu fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        UneartherMenu menu = new UneartherMenu(windowId, inventory, buf.readBlockPos());
        // TODO anything else to sync?
        return menu;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            resultStack = stackInSlot.copy();
            if (index < 36) {
                // moving from player inv to machine slots
                if (!moveItemStackTo(stackInSlot, 36, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 27, 36, false)
                    && !moveItemStackTo(stackInSlot, 0, 27, false)) {
                // try move to hotbar, then to player inv
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return resultStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return !unearther.isRemoved() && player.distanceToSqr(Vec3.atCenterOf(unearther.getBlockPos())) < 64;
    }

    public UneartherCoreBlockEntity getUnearther() {
        return unearther;
    }

    public float getProgress() {
        // called client-side
        int max = unearther.getProcessingTimeSlot().get();
        int current = unearther.getProgressSlot().get();
        return max == 0 || current <= 0 ? 0f : 1f - (float) current / max;
    }
}
