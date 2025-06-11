package dev.ftb.mods.ftbunearthed.block;

import dev.ftb.mods.ftbunearthed.crafting.AcceptabilityCache;
import dev.ftb.mods.ftbunearthed.crafting.RecipeCaches;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.menu.UneartherMenu;
import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.ForwardingItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class UneartherCoreBlockEntity extends BlockEntity implements MenuProvider {
    public static final int OUTPUT_SLOTS = 6;
    public static final int IDLING = -1;
    private static final int COOLDOWN = 40;  // cool-off if output is clogged

    private final WorkerHandler workerHandler = new WorkerHandler();
    private final ToolHandler toolHandler = new ToolHandler();
    private final ItemStackHandler outputHandler = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final IItemHandler outputHandlerWrapper = new OutputWrapper(outputHandler);
    private final DataSlot processingTimeSlot = DataSlot.standalone();
    private final DataSlot progressSlot = DataSlot.standalone();

    private static final AcceptabilityCache<Item> knownWorkerItems = new AcceptabilityCache<>();
    private static final AcceptabilityCache<Item> knownToolItems = new AcceptabilityCache<>();

    private UneartherRecipe currentRecipe = null;
    private boolean recheckRecipe = true;
    private int progress = IDLING;
    private int cooldownTimer = 0;

    public UneartherCoreBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntityTypes.UNEARTHER_CORE.get(), blockPos, blockState);
    }

    public static void clearKnownItemCaches() {
        knownWorkerItems.clear();
        knownToolItems.clear();
    }

    public void tickClient() {
    }

    private @NotNull ItemStack getToolStack() {
        return toolHandler.getStackInSlot(0);
    }

    private @NotNull ItemStack getWorkerStack() {
        return workerHandler.getStackInSlot(0);
    }

    public void tickServer() {
        if (recheckRecipe) {
            currentRecipe = RecipeCaches.UNEARTHER.getCachedRecipe(this::searchForRecipe, this::genIngredientHash)
                    .map(RecipeHolder::value)
                    .orElse(null);

            processingTimeSlot.set(currentRecipe == null ? 0 : currentRecipe.getProcessingTime());
            recheckRecipe = false;
        }

        int prevProgress = progress;

        if (cooldownTimer > 0) {
            cooldownTimer--;
        } else if (currentRecipe != null) {
            if (progress < 0 || progress > currentRecipe.getProcessingTime()) {
                progress = currentRecipe.getProcessingTime();
            }

            if (progress > 0 && --progress == 0) {
                if (!tryGenerateOutputs()) {
                    cooldownTimer = COOLDOWN;
                }
                progress = currentRecipe.getProcessingTime();
            }
        } else {
            progress = IDLING;
        }

        if (progress != prevProgress) {
            progressSlot.set(progress);
            setChanged();
        }
    }

    private Optional<RecipeHolder<UneartherRecipe>> searchForRecipe() {
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.UNEARTHER_TYPE.get()).stream()
                .filter(holder -> holder.value().test(getWorkerStack(), getToolStack()))
                .findFirst();
    }

    private int genIngredientHash() {
        return Objects.hash(
                ItemStack.hashItemAndComponents(getWorkerStack()),
                ItemStack.hashItemAndComponents(getToolStack())
        );
    }

    private boolean tryGenerateOutputs() {
        assert currentRecipe != null;

        ItemStack output = currentRecipe.generateRandomItem();
        ItemStack result = ItemHandlerHelper.insertItem(outputHandler, output, false);
        return !ItemStack.matches(output, result);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("Worker", workerHandler.serializeNBT(registries));
        tag.put("Tool", toolHandler.serializeNBT(registries));
        tag.put("Output", outputHandler.serializeNBT(registries));
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        workerHandler.deserializeNBT(registries, tag.getCompound("Worker"));
        toolHandler.deserializeNBT(registries, tag.getCompound("Tool"));
        outputHandler.deserializeNBT(registries, tag.getCompound("Output"));
        progress = tag.getInt("Progress");
    }

    public IItemHandler getWorkerItemHandler() {
        return workerHandler;
    }

    public IItemHandler getToolItemHandler() {
        return toolHandler;
    }

    public ItemStackHandler getInternalOutputHandler() {
        // only used by client-side menu
        return outputHandler;
    }

    public IItemHandler getPublicOutputHandler() {
        // used for capability access
        return outputHandlerWrapper;
    }

    private boolean isKnownWorkerItem(ItemStack stack) {
        return knownWorkerItems.isAcceptable(stack.getItem(), () ->
                level.getRecipeManager().getAllRecipesFor(ModRecipes.UNEARTHER_TYPE.get()).stream()
                        .anyMatch(h -> h.value().getWorkerItem().test(stack)));
    }

    private boolean isKnownToolItem(ItemStack stack) {
        return knownToolItems.isAcceptable(stack.getItem(), () ->
                level.getRecipeManager().getAllRecipesFor(ModRecipes.UNEARTHER_TYPE.get()).stream()
                        .anyMatch(h -> h.value().getToolItem().test(stack)));
    }

    public void dropItemContents() {
        dropIfPresent(getWorkerStack());
        dropIfPresent(getToolStack());
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            dropIfPresent(outputHandler.getStackInSlot(i));
        }
    }

    private void dropIfPresent(ItemStack stack) {
        if (!stack.isEmpty()) {
            Block.popResource(getLevel(), getBlockPos(), stack);
        }
    }

    public DataSlot getProgressSlot() {
        return progressSlot;
    }

    public DataSlot getProcessingTimeSlot() {
        return processingTimeSlot;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ftbunearthed.unearther");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new UneartherMenu(containerId, playerInventory, getBlockPos());
    }

    public static class OutputWrapper extends ForwardingItemHandler {
        public OutputWrapper(IItemHandler delegate) {
            super(delegate);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }
    }

    private abstract class FilteredHandler extends ItemStackHandler {
        private final Predicate<ItemStack> filter;

        public FilteredHandler(Predicate<ItemStack> filter) {
            super(1);
            this.filter = filter;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return super.isItemValid(slot, stack) && filter.test(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            recheckRecipe = true;
        }
    }

    private class WorkerHandler extends FilteredHandler {
        public WorkerHandler() {
            super(UneartherCoreBlockEntity.this::isKnownWorkerItem);
        }
    }


    private class ToolHandler extends FilteredHandler {
        public ToolHandler() {
            super(UneartherCoreBlockEntity.this::isKnownToolItem);
        }
    }
}
