package dev.ftb.mods.ftbunearthed.menu;

import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlockEntity;
import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.SlotItemHandler;

public class UneartherMenu extends AbstractContainerMenu {
    public static final int PLAYER_INV_Y = 84;

    private final UneartherCoreBlockEntity unearther;
    private final ContainerData data;

    public UneartherMenu(int containerId, Inventory invPlayer, BlockPos pos, ContainerData data) {
        super(ModMenuTypes.UNEARTHER_MENU.get(), containerId);
        this.data = data;

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

        // item slots
        addSlot(new SlotItemHandler(unearther.getInputHandler(), 0, 62, 18));
        addSlot(new SlotItemHandler(unearther.getFoodHandler(), 0, 26, 18));
        addSlot(new SlotItemHandler(unearther.getWorkerHandler(), 0, 26, 53));
        addSlot(new SlotItemHandler(unearther.getToolHandler(), 0, 62, 53));

        // item output slots
        for (int i = 0; i < 6; i++) {
            addSlot(new OutputOnlySlot(unearther.getOutputHandler(), i, 116 + 18 * (i % 2), 17 + 18 * (i / 2)));
        }

        // sync'd data
        addDataSlots(data);
    }

    public static UneartherMenu fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        UneartherMenu menu = new UneartherMenu(windowId, inventory, buf.readBlockPos(), new SimpleContainerData(4));
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
                if (!moveItemStackTo(stackInSlot, 36, 40, false)) {
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
        int processingTime = data.get(0);
        int progress = data.get(1);
        return processingTime == 0 || progress <= 0 ? 0f : 1f - (float) progress / processingTime;
    }

    public float getFoodBuffer() {
        return (float) data.get(2) / UneartherCoreBlockEntity.MAX_FOOD_BUFFER;
    }

    public int getSpeedBoost() {
        return data.get(3);
    }
}
