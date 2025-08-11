package dev.ftb.mods.ftbunearthed.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbunearthed.crafting.AcceptabilityCache;
import dev.ftb.mods.ftbunearthed.crafting.RecipeCaches;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.entity.Worker;
import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import dev.ftb.mods.ftbunearthed.menu.UneartherMenu;
import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModDataComponents;
import dev.ftb.mods.ftbunearthed.registry.ModEntityTypes;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class UneartherCoreBlockEntity extends BlockEntity implements MenuProvider {
    public static final int OUTPUT_SLOTS = 9;
    private static final int IDLING = -1;
    private static final int COOLDOWN = 40;  // cool-off if output is clogged
    private static final int PROGRESS_MULT = 100;  // internal multiplier, allows for speed boosting
    public static final int MAX_FOOD_BUFFER = 24000; // in ticks, 20 minutes

    private final FoodHandler foodHandler = new FoodHandler();
    private final InputHandler inputHandler = new InputHandler();
    private final WorkerHandler workerHandler = new WorkerHandler();
    private final ToolHandler toolHandler = new ToolHandler();
    private final ItemStackHandler outputHandler = new OutputHandler();

    // public capability access
    private final IItemHandler publicItemHandler = new PublicItemWrapper();

    private static final AcceptabilityCache<Item> knownInputItems = new AcceptabilityCache<>();
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

    private int clientProgress;
    private SyncedStatus syncedStatus = SyncedStatus.EMPTY;
    private SyncedStatus lastSynced = null;
    private boolean syncNeeded;

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
        knownToolItems.clear();
        knownInputItems.clear();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        // server side, chunk sending
        CompoundTag compound = super.getUpdateTag(provider);
        lastSynced = SyncedStatus.create(this);
        compound.put("Status", SyncedStatus.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), lastSynced).getOrThrow());
        return compound;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        // server side, block update (calls getUpdateTag())
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        // client side, chunk sending
        processClientSync(tag, provider);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        // client side, block update
        processClientSync(pkt.getTag(), lookupProvider);
    }

    private void processClientSync(CompoundTag tag, HolderLookup.Provider provider) {
        syncedStatus = SyncedStatus.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag.getCompound("Status")).getOrThrow();
        clientProgress = 0;
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
        if (syncedStatus.active()) {
            if (clientProgress == 0) {
                level.playLocalSound(getBlockPos().above(), SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 0.6f, 1f, false);
            }
            clientProgress = (clientProgress + 1) % syncedStatus.processingTime();
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
            progress = IDLING;
        }

        if (progress != prevProgress) {
            syncedProgress = progress / PROGRESS_MULT;
            setChanged();
            syncNeeded = true;
        }

        if (syncNeeded) {
            syncStatusToClients();
            syncNeeded = false;
        }
    }

    private void checkForEntity(ServerLevel level) {
        Entity currentWorker = level.getEntity(workerID);

        Direction d = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (!getWorkerStack().isEmpty() && currentWorker == null) {
            // (re)spawn worker entity
            WorkerToken.WorkerData workerData = getWorkerStack().get(ModDataComponents.WORKER_DATA);
            if (workerData == null) {
                return;  // shouldn't happen!
            }

            // TODO get entity from component data on worker item
            Worker newWorker = new Worker(ModEntityTypes.WORKER.get(), level);
            newWorker.setVillagerData(workerData.toVillagerData());
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
            if (currentRecipe != null) {
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
        if (!food.isEmpty() && !getWorkerStack().isEmpty() && foodBuffer == 0) {
            FoodProperties props = food.getFoodProperties(null);
            if (props != null) {
                int value = Math.min(MAX_FOOD_BUFFER, (int) (props.saturation() * 1200));  // one saturation = 1200 ticks or 1 minute
                foodHandler.extractItem(0, 1, false);
                foodBuffer += value;
                currentSpeedBoost = props.nutrition() * 5;
                level.playSound(null, getBlockPos().above(2), SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 1f, 1f);
                setChanged();
            }
        }
    }

    private void runOneWorkCycle(ServerLevel level) {
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
                if (taken.isEmpty() || !tryGenerateOutputs(level)) {
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
        return RecipeCaches.sortedUneartherRecipes(level).stream()
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

    private boolean tryGenerateOutputs(ServerLevel level) {
        assert currentRecipe != null;

        List<ItemStack> outputs = currentRecipe.generateOutputs(level.random);
        if (outputs.isEmpty()) return true;

        boolean ok = false;
        for (ItemStack output: outputs) {
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

    public int getCurrentSpeedBoost() {
        return currentSpeedBoost;
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
    public IItemHandler getItemHandler() {
        return publicItemHandler;
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

    public void syncStatusToClients() {
        if (level instanceof ServerLevel serverLevel && !lastSynced.equals(SyncedStatus.create(this))) {
            serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public SyncedStatus getSyncedStatus() {
        return syncedStatus;
    }

    public int getClientBreakProgress() {
        if (syncedStatus.active()) {
            float pct = Mth.clamp((float) clientProgress / syncedStatus.processingTime(), 0f, 1f);
            return (int) (9 * pct);
        }
        return 0;
    }

    public Optional<UneartherRecipe> getCurrentRecipe() {
        return Optional.ofNullable(currentRecipe);
    }

    private class PublicItemWrapper implements IItemHandler {
        @Override
        public int getSlots() {
            return 2 + OUTPUT_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return switch (slot) {
                case 0 -> inputHandler.getStackInSlot(0);
                case 1 -> foodHandler.getStackInSlot(0);
                default -> outputHandler.getStackInSlot(slot - 2);
            };
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return switch (slot) {
                case 0 -> inputHandler.insertItem(0, stack, simulate);
                case 1 -> foodHandler.insertItem(0, stack, simulate);
                default -> stack;
            };
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= 2 ? outputHandler.extractItem(slot - 2, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return switch (slot) {
                case 0 -> inputHandler.getSlotLimit(0);
                case 1 -> foodHandler.getSlotLimit(0);
                default -> outputHandler.getSlotLimit(slot - 2);
            };
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> inputHandler.isItemValid(0, stack);
                case 1 -> foodHandler.isItemValid(0, stack);
                default -> outputHandler.isItemValid(slot - 2, stack);
            };
        }
    }

    private abstract class FilteredInsertOnlyHandler extends ItemStackHandler {
        private final Predicate<ItemStack> filter;

        public FilteredInsertOnlyHandler(Predicate<ItemStack> filter) {
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

    private class InputHandler extends FilteredInsertOnlyHandler {
        private Item lastItem = Items.AIR;

        public InputHandler() {
            super(stack -> UneartherCoreBlockEntity.isKnownInputItem(level, stack));
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (getStackInSlot(slot).getItem() != lastItem) {
                lastItem = getStackInSlot(slot).getItem();
                syncNeeded = true;
            }
        }
    }

    private class FoodHandler extends FilteredInsertOnlyHandler {
        public FoodHandler() {
            super(stack -> stack.getFoodProperties(null) != null);
        }
    }

    private class WorkerHandler extends FilteredInsertOnlyHandler {
        public WorkerHandler() {
            super(stack -> stack.get(ModDataComponents.WORKER_DATA) != null);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }

    private class ToolHandler extends FilteredInsertOnlyHandler {
        public ToolHandler() {
            super(stack -> UneartherCoreBlockEntity.isKnownToolItem(level, stack));
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }

    private class OutputHandler extends ItemStackHandler {
        public OutputHandler() {
            super(UneartherCoreBlockEntity.OUTPUT_SLOTS);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return super.insertItem(slot, stack, simulate);
        }
    }

    public record SyncedStatus(BlockState blockState, int processingTime) {
        static final Codec<SyncedStatus> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BlockState.CODEC.fieldOf("block").forGetter(SyncedStatus::blockState),
                Codec.INT.fieldOf("time").forGetter(SyncedStatus::processingTime)
        ).apply(builder, SyncedStatus::new));
        static final SyncedStatus EMPTY = new SyncedStatus(Blocks.AIR.defaultBlockState(), 0);

        static SyncedStatus create(UneartherCoreBlockEntity core) {
            int processingTime = core.getCurrentRecipe().map(UneartherRecipe::getProcessingTime).orElse(0);
            int boost = core.getCurrentSpeedBoost();
            BlockState state = core.getInputStack().getItem() instanceof BlockItem bi ? bi.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();
            return new SyncedStatus(state, processingTime * 100 / (100 + boost));
        }

        public boolean active() {
            return processingTime > 0;
        }
    }
}
