package dev.ftb.mods.ftbunearthed.block;

import dev.ftb.mods.ftbunearthed.crafting.AcceptabilityCache;
import dev.ftb.mods.ftbunearthed.crafting.RecipeCaches;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.menu.UneartherMenu;
import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
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
    private static final int OUTPUT_SLOTS = 6;
    private static final int IDLING = -1;
    private static final int COOLDOWN = 40;  // cool-off if output is clogged
    private static final int PROGRESS_MULT = 100;  // internal multiplier, allows for speed boosting
    public static final int MAX_FOOD_BUFFER = 24000; // in ticks, 20 minutes

    private final FoodHandler foodHandler = new FoodHandler();
    private final InputHandler inputHandler = new InputHandler();
    private final WorkerHandler workerHandler = new WorkerHandler();
    private final ToolHandler toolHandler = new ToolHandler();
    private final ItemStackHandler outputHandler = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    // public capability access
    private final IItemHandler foodWrapper = new InputWrapper(foodHandler);
    private final IItemHandler inputWrapper = new InputWrapper(inputHandler);
    private final IItemHandler outputWrapper = new OutputWrapper(outputHandler);

    private static final AcceptabilityCache<Item> knownInputItems = new AcceptabilityCache<>();
    private static final AcceptabilityCache<Item> knownWorkerItems = new AcceptabilityCache<>();
    private static final AcceptabilityCache<Item> knownToolItems = new AcceptabilityCache<>();

    private UneartherRecipe currentRecipe = null;
    private boolean recheckRecipe = true;
    private int cooldownTimer = 0;
    private int progress = IDLING;

    private final ContainerData dataAccess;
    private int processingTime;
    private int syncedProgress;
    private int foodBuffer;
    private int currentSpeedBoost = 0;

    public UneartherCoreBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntityTypes.UNEARTHER_CORE.get(), blockPos, blockState);

        dataAccess = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> processingTime;
                    case 1 -> syncedProgress;
                    case 2 -> foodBuffer;
                    default -> currentSpeedBoost;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> processingTime = value;
                    case 1 -> syncedProgress = value;
                    case 2 -> foodBuffer = value;
                    default -> currentSpeedBoost = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    public static void clearKnownItemCaches() {
        knownWorkerItems.clear();
        knownToolItems.clear();
        knownInputItems.clear();
    }

    public void tickClient() {
    }

    private @NotNull ItemStack getToolStack() {
        return toolHandler.getStackInSlot(0);
    }

    private @NotNull ItemStack getWorkerStack() {
        return workerHandler.getStackInSlot(0);
    }

    private @NotNull ItemStack getInputStack() {
        return inputHandler.getStackInSlot(0);
    }

    public void tickServer() {
        if (recheckRecipe) {
            currentRecipe = RecipeCaches.UNEARTHER.getCachedRecipe(this::searchForRecipe, this::genIngredientHash)
                    .map(RecipeHolder::value)
                    .orElse(null);

            processingTime = currentRecipe == null ? 0 : currentRecipe.getProcessingTime();
            recheckRecipe = false;
        }

        if (level.getGameTime() % 20 == 0) {
            processFoodSlot();
        }

        int prevProgress = progress;

        if (cooldownTimer > 0) {
            cooldownTimer--;
        } else if (currentRecipe != null) {
            runOneWorkCycle();
        } else {
            progress = IDLING;
        }

        if (progress != prevProgress) {
            syncedProgress = progress / PROGRESS_MULT;
            setChanged();
        }
    }

    private void processFoodSlot() {
        ItemStack food = foodHandler.getStackInSlot(0);
        if (!food.isEmpty()) {
            FoodProperties props = food.getFoodProperties(null);
            if (props != null) {
                int value = (int) (props.saturation() * 1200);  // one saturation = 1200 ticks or 1 minute
                if (MAX_FOOD_BUFFER - foodBuffer > value) {
                    foodHandler.extractItem(0, 1, false);
                    foodBuffer += value;
                    currentSpeedBoost = props.nutrition() * 5;
                    level.playSound(null, getBlockPos(), SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 1f, 1f);
                    setChanged();
                }
            }
        }
    }

    private void runOneWorkCycle() {
        int internalProcessingTime = currentRecipe.getProcessingTime() * PROGRESS_MULT;

        if (progress < 0 || progress > internalProcessingTime) {
            progress = internalProcessingTime;
        }

        int step = PROGRESS_MULT;
        if (foodBuffer > 0) {
            step += currentSpeedBoost;
            foodBuffer--;
        }
        if (progress > 0) {
            progress = Math.max(0, progress - step);
            if (progress == 0) {
                // work cycle complete
                ItemStack taken = inputHandler.extractItem(0, 1, true);
                if (taken.isEmpty() || !tryGenerateOutputs()) {
                    cooldownTimer = COOLDOWN;
                }
                inputHandler.extractItem(0, 1, false);
                foodBuffer = Math.max(0, foodBuffer - currentRecipe.getProcessingTime());
                progress = internalProcessingTime;
                if (level.random.nextFloat() < currentRecipe.getDamageChance()) {
                    getToolStack().hurtAndBreak(1, (ServerLevel) level, null, item -> {});
                }
            }
        }
    }

    private Optional<RecipeHolder<UneartherRecipe>> searchForRecipe() {
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.UNEARTHER_TYPE.get()).stream()
                .filter(holder -> holder.value().test(getInputStack(), getWorkerStack(), getToolStack()))
                .findFirst();
    }

    private int genIngredientHash() {
        return Objects.hash(
                ItemStack.hashItemAndComponents(getInputStack()),
                ItemStack.hashItemAndComponents(getWorkerStack()),
                ItemStack.hashItemAndComponents(getToolStack())
        );
    }

    private boolean tryGenerateOutputs() {
        assert currentRecipe != null;

        boolean ok = false;
        for (ItemStack output: currentRecipe.generateOutputs(level.random)) {
            ItemStack result = ItemHandlerHelper.insertItemStacked(outputHandler, output, false);
            // if nothing at all can be inserted, go into cooldown
            if (!ItemStack.matches(output, result)) {
                ok = true;  // at least some of the stack could be inserted
            }
        }
        return ok;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("Input", inputHandler.serializeNBT(registries));
        tag.put("Food", foodHandler.serializeNBT(registries));
        tag.put("Worker", workerHandler.serializeNBT(registries));
        tag.put("Tool", toolHandler.serializeNBT(registries));
        tag.put("Output", outputHandler.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("FoodBuffer", foodBuffer);
        tag.putInt("SpeedBoost", currentSpeedBoost);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        inputHandler.deserializeNBT(registries, tag.getCompound("Input"));
        foodHandler.deserializeNBT(registries, tag.getCompound("Food"));
        workerHandler.deserializeNBT(registries, tag.getCompound("Worker"));
        toolHandler.deserializeNBT(registries, tag.getCompound("Tool"));
        outputHandler.deserializeNBT(registries, tag.getCompound("Output"));
        progress = tag.getInt("Progress");
        foodBuffer = tag.getInt("FoodBuffer");
        currentSpeedBoost = tag.getInt("SpeedBoost");
    }

    public IItemHandler getWorkerHandler() {
        return workerHandler;
    }

    public IItemHandler getToolHandler() {
        return toolHandler;
    }

    public IItemHandler getInputHandler() {
        return inputHandler;
    }

    public IItemHandler getFoodHandler() {
        return foodHandler;
    }

    // only used by menu
    public ItemStackHandler getOutputHandler() {
        return outputHandler;
    }

    // used for capability access
    public IItemHandler getSidedHandler(@Nullable Direction side) {
        if (side == null) return inputWrapper;
        return switch (side) {
            case UP -> foodWrapper;
            case DOWN -> outputWrapper;
            default -> inputWrapper;
        };
    }

    public static boolean isKnownWorkerItem(Level level, ItemStack stack) {
        return knownWorkerItems.isAcceptable(stack.getItem(), () ->
                level.getRecipeManager().getAllRecipesFor(ModRecipes.UNEARTHER_TYPE.get()).stream()
                        .anyMatch(h -> h.value().getWorkerItem().test(stack)));
    }

    public static boolean isKnownToolItem(Level level, ItemStack stack) {
        return knownToolItems.isAcceptable(stack.getItem(), () ->
                level.getRecipeManager().getAllRecipesFor(ModRecipes.UNEARTHER_TYPE.get()).stream()
                        .anyMatch(h -> h.value().getToolItem().test(stack)));
    }

    private static boolean isKnownInputItem(Level level, ItemStack stack) {
        return knownInputItems.isAcceptable(stack.getItem(), () ->
                level.getRecipeManager().getAllRecipesFor(ModRecipes.UNEARTHER_TYPE.get()).stream()
                        .anyMatch(h -> h.value().isValidInput(stack)));
    }

    public void dropItemContents() {
        dropIfPresent(foodHandler.getStackInSlot(0));
        dropIfPresent(getInputStack());
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ftbunearthed.core");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new UneartherMenu(containerId, playerInventory, getBlockPos(), dataAccess);
    }

    public static class InputWrapper extends ForwardingItemHandler {
        public InputWrapper(IItemHandler delegate) {
            super(delegate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
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

    private class InputHandler extends FilteredHandler {
        public InputHandler() {
            super(stack -> UneartherCoreBlockEntity.isKnownInputItem(level, stack));
        }
    }

    private class FoodHandler extends FilteredHandler {
        public FoodHandler() {
            super(stack -> stack.getFoodProperties(null) != null);
        }
    }

    private class WorkerHandler extends FilteredHandler {
        public WorkerHandler() {
            super(stack -> UneartherCoreBlockEntity.isKnownWorkerItem(level, stack));
        }
    }

    private class ToolHandler extends FilteredHandler {
        public ToolHandler() {
            super(stack -> UneartherCoreBlockEntity.isKnownToolItem(level, stack));
        }
    }
}
