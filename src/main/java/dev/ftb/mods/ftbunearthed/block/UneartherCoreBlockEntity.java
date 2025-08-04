package dev.ftb.mods.ftbunearthed.block;

import dev.ftb.mods.ftbunearthed.crafting.AcceptabilityCache;
import dev.ftb.mods.ftbunearthed.crafting.RecipeCaches;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.entity.Worker;
import dev.ftb.mods.ftbunearthed.menu.UneartherMenu;
import dev.ftb.mods.ftbunearthed.network.UneartherStatusMessage;
import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.ForwardingItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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
    private UUID workerID = Util.NIL_UUID;

    private final ContainerData dataAccess;
    private int processingTime;
    private int syncedProgress;
    private int foodBuffer;
    private int currentSpeedBoost = 0;

    private UneartherStatusMessage.ClientStatus clientStatus = UneartherStatusMessage.ClientStatus.IDLE;
    private int clientProgress;

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

    private @NotNull ItemStack getToolStack() {
        return toolHandler.getStackInSlot(0);
    }

    private @NotNull ItemStack getWorkerStack() {
        return workerHandler.getStackInSlot(0);
    }

    @NotNull
    public ItemStack getInputStack() {
        return inputHandler.getStackInSlot(0);
    }

    public void tickClient(Level level) {
        if (clientStatus.active()) {
            clientProgress = (clientProgress + 1) % clientStatus.processingTime();
            if (level.random.nextInt(8) == 0) {
                Direction d = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                Vec3 vec = Vec3.atBottomCenterOf(getBlockPos()).add(d.getStepX() * 0.5, 1.5, d.getStepZ() * 0.5);
                level.addParticle(ParticleTypes.DUST_PLUME, vec.x + level.random.nextDouble() - 0.5, vec.y, vec.z+ level.random.nextDouble() - 0.5, 0.0, -0.1, 0.0);
            }
        } else {
            clientProgress = 0;
        }
    }

    public void tickServer(ServerLevel level) {
        if (recheckRecipe) {
            currentRecipe = RecipeCaches.UNEARTHER.getCachedRecipe(this::searchForRecipe, this::genIngredientHash)
                    .map(RecipeHolder::value)
                    .orElse(null);

            processingTime = currentRecipe == null ? 0 : currentRecipe.getProcessingTime();
            recheckRecipe = false;
        }

        checkForEntity(level);

        if (level.getGameTime() % 20 == 0) {
            processFoodSlot(level);
        }

        int prevProgress = progress;

        if (cooldownTimer > 0) {
            cooldownTimer--;
        } else if (currentRecipe != null) {
            runOneWorkCycle(level);
        } else {
            if (progress != IDLING) {
                syncStatusToClients();
            }
            progress = IDLING;
        }

        if (progress != prevProgress) {
            syncedProgress = progress / PROGRESS_MULT;
            setChanged();
        }
    }

    private void checkForEntity(ServerLevel level) {
        Entity currentWorker = level.getEntity(workerID);

        Direction d = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (!getWorkerStack().isEmpty() && currentWorker == null) {
            // (re)spawn worker entity

            // TODO get entity from component data on worker item
            Worker newWorker = new Worker(ModEntityTypes.WORKER.get(), level);
            newWorker.setVillagerData(newWorker.getVillagerData().setProfession(VillagerProfession.MASON));
            newWorker.setNoAi(true);
            newWorker.setPos(Vec3.atCenterOf(getBlockPos()).add(d.getStepX() * -0.5, 0.0, d.getStepZ() * -0.5));
            newWorker.setYHeadRot(d.toYRot());
            level.addFreshEntity(newWorker);
            newWorker.setSilent(true);
            workerID = newWorker.getUUID();
            currentWorker = newWorker;
            level.playSound(null, getBlockPos().above(2), SoundEvents.VILLAGER_CELEBRATE, SoundSource.BLOCKS);
            setChanged();
        } else if (getWorkerStack().isEmpty() && currentWorker != null) {
            level.playSound(null, getBlockPos().above(2), SoundEvents.VILLAGER_NO, SoundSource.BLOCKS);
            currentWorker.discard();
            currentWorker = null;
            workerID = Util.NIL_UUID;
            setChanged();
        }

        if (currentWorker instanceof Worker w) {
            w.setItemInHand(InteractionHand.MAIN_HAND, getToolStack().copy());
            w.setYHeadRot(d.toYRot());
            Direction dir = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (!getInputStack().isEmpty() && !getToolStack().isEmpty()) {
                w.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(getBlockPos()).add(dir.getStepX(), 1, dir.getStepZ()));
                w.setBusy(true);
            } else {
                Player p = level.getNearestPlayer(w, 6.0);
                if (p != null) {
                    w.lookAt(EntityAnchorArgument.Anchor.EYES, p.getEyePosition());
                } else {
                    w.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(getBlockPos()).add(dir.getStepX(), 1.5, dir.getStepZ()));
                }
                w.setBusy(false);
            }
        }
    }

    private void processFoodSlot(ServerLevel level) {
        ItemStack food = foodHandler.getStackInSlot(0);
        if (!food.isEmpty()) {
            FoodProperties props = food.getFoodProperties(null);
            if (props != null) {
                int value = (int) (props.saturation() * 1200);  // one saturation = 1200 ticks or 1 minute
                if (MAX_FOOD_BUFFER - foodBuffer > value) {
                    foodHandler.extractItem(0, 1, false);
                    foodBuffer += value;
                    currentSpeedBoost = props.nutrition() * 5;
                    level.playSound(null, getBlockPos().above(2), SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 1f, 1f);
                    setChanged();
                }
            }
        }
    }

    private void runOneWorkCycle(ServerLevel level) {
        int internalProcessingTime = currentRecipe.getProcessingTime() * PROGRESS_MULT;

        if (progress < 0 || progress > internalProcessingTime) {
            progress = internalProcessingTime;
        }

        if (progress == internalProcessingTime) {
            syncStatusToClients();
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
                    getToolStack().hurtAndBreak(1, level, null, item -> {});
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
        if (workerID != Util.NIL_UUID) {
            tag.putUUID("WorkerID", workerID);
        }
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
        if (tag.contains("WorkerID")) {
            workerID = tag.getUUID("WorkerID");
        } else {
            workerID = Util.NIL_UUID;
        }
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

    @Override
    public void setRemoved() {
        if (!workerID.equals(Util.NIL_UUID) && level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(workerID);
            if (entity != null) {
                level.playSound(null, getBlockPos().above(2), SoundEvents.VILLAGER_DEATH, SoundSource.BLOCKS);
                entity.discard();
            }
        }
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

    public void syncStatusFromServer(UneartherStatusMessage.ClientStatus status) {
        this.clientStatus = status;
        clientProgress = 0;
    }

    public UneartherStatusMessage.ClientStatus getClientStatus() {
        return clientStatus;
    }

    public int getClientBreakProgress() {
        if (clientStatus.active()) {
            float pct = Mth.clamp((float) clientProgress / clientStatus.processingTime(), 0f, 1f);
            return (int) (9 * pct);
        }
        return 0;
    }

    public Optional<UneartherRecipe> getCurrentRecipe() {
        return Optional.ofNullable(currentRecipe);
    }

    public void syncStatusToClients() {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(getBlockPos()), UneartherStatusMessage.create(this));
        }
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
        private Item lastItem = Items.AIR;

        public InputHandler() {
            super(stack -> UneartherCoreBlockEntity.isKnownInputItem(level, stack));
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (getStackInSlot(slot).getItem() != lastItem) {
                lastItem = getStackInSlot(slot).getItem();
                syncStatusToClients();
            }
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
