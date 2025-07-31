package dev.ftb.mods.ftbunearthed.block;

import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;

public class UneartherFrameBlockEntity extends BlockEntity {
    @NotNull
    private WeakReference<UneartherCoreBlockEntity> core = new WeakReference<>(null);
    private BlockPos corePosPending;  // non-null after NBT load & before querying/resolving

    public UneartherFrameBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.UNEARTHER_FRAME.get(), pos, blockState);
    }

    public Optional<UneartherCoreBlockEntity> getCore() {
        if (corePosPending != null && level != null) {
            // first core screen query since loaded from NBT
            level.getBlockEntity(corePosPending, ModBlockEntityTypes.UNEARTHER_CORE.get()).ifPresentOrElse(
                    core -> {
                        this.core = new WeakReference<>(core);
                        corePosPending = null;
                    },
                    () -> {
                        // something's gone wrong & the core no longer exists?
                        level.destroyBlock(getBlockPos(), false, null);
                    }
            );
        }
        return Optional.ofNullable(core.get());
    }

    public void setCore(@NotNull UneartherCoreBlockEntity core) {
        // this must ONLY be called from UneartherCoreBlock#onPlacedBy() !
        if (this.core.get() != null) {
            throw new IllegalStateException("core is already set and can't be changed!");
        }

        this.core = new WeakReference<>(core);
        setChanged();
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        corePosPending = NbtUtils.readBlockPos(compoundTag, "CorePos").orElse(null);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);

        if (corePosPending != null) {
            compoundTag.put("CorePos", NbtUtils.writeBlockPos(corePosPending));
        } else {
            UneartherCoreBlockEntity cs = core.get();
            if (cs != null) {
                compoundTag.put("CorePos", NbtUtils.writeBlockPos(cs.getBlockPos()));
            }
        }
    }

    public @Nullable IItemHandler getSidedHandler(Direction side) {
        return getCore().map(be -> be.getSidedHandler(side)).orElse(null);
    }
}
