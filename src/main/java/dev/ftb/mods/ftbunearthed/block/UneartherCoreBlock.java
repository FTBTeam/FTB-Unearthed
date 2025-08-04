package dev.ftb.mods.ftbunearthed.block;

import dev.ftb.mods.ftbunearthed.registry.ModBlocks;
import dev.ftb.mods.ftbunearthed.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class UneartherCoreBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 8, 16);

    public UneartherCoreBlock(Properties properties) {
        super(properties);

        BlockState state = getStateDefinition().any();
        state = state.setValue(HORIZONTAL_FACING, Direction.NORTH);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!validatePlaceableBlock(context)) {
            context.getPlayer().displayClientMessage(Component.literal("Obstructed by block!").withStyle(ChatFormatting.RED), true);
            return null;
        }
        if (!validatePlaceableEntity(context)) {
            context.getPlayer().displayClientMessage(Component.literal("Obstructed by entity!").withStyle(ChatFormatting.RED), true);
            return null;
        }
        return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    private boolean validatePlaceableBlock(BlockPlaceContext ctx) {
        BoundingBox box = new BoundingBox(ctx.getClickedPos()).inflatedBy(1).moved(0, 1, 0);
        return BlockPos.betweenClosedStream(box)
                .allMatch(pos -> ctx.getLevel().getBlockState(pos).canBeReplaced(ctx));
    }

    private boolean validatePlaceableEntity(BlockPlaceContext ctx) {
        AABB box = new AABB(ctx.getClickedPos()).inflate(1).move(0, 1, 0);

        return ctx.getLevel().getEntities((Entity) null, box, e -> true).isEmpty();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        // we're assuming placement is OK, we called validatePlaceable*() already
        if (level.getBlockEntity(pos) instanceof UneartherCoreBlockEntity core) {
            BlockPos mid = pos.above();
            BlockPos top = mid.above();

            for (Direction d : MiscUtil.HORIZONTALS) {
                Direction d1 = d.getCounterClockWise();
                createFrameBlock(level, pos.relative(d), ModBlocks.L_EDGE, d, core);
                createFrameBlock(level, pos.relative(d).relative(d1), ModBlocks.L_CORNER, d, core);
                createFrameBlock(level, mid.relative(d), ModBlocks.M_FACE, d, core);
                createFrameBlock(level, mid.relative(d).relative(d1), ModBlocks.M_EDGE, d, core);
                createFrameBlock(level, top.relative(d), ModBlocks.U_EDGE, d, core);
                createFrameBlock(level, top.relative(d).relative(d1), ModBlocks.U_CORNER, d, core);
            }

            createFrameBlock(level, top, ModBlocks.U_FACE, state.getValue(HORIZONTAL_FACING), core);
        }
    }

    private void createFrameBlock(Level level, BlockPos pos, DeferredBlock<UneartherFrameBlock> block, Direction rot, UneartherCoreBlockEntity core) {
        level.setBlock(pos, block.get().defaultBlockState().setValue(HORIZONTAL_FACING, rot), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof UneartherFrameBlockEntity f) {
            f.setCore(core);
        }
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new UneartherCoreBlockEntity(blockPos, blockState);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean bl) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof UneartherCoreBlockEntity machine) {
                machine.dropItemContents();
            }
            BoundingBox box = new BoundingBox(pos).inflatedBy(1).moved(0, 1, 0);
            BlockPos.betweenClosedStream(box).forEach(p -> {
                if (level.getBlockState(p).getBlock() instanceof UneartherFrameBlock) {
                    level.destroyBlock(p, false);
                }
            });
        }

        super.onRemove(state, level, pos, newState, bl);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof UneartherCoreBlockEntity be) {
                player.openMenu(be, pos);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (level1, blockPos, blockState, t) -> {
            if (t instanceof UneartherCoreBlockEntity tickable) {
                if (level1 instanceof ServerLevel serverLevel) {
                    tickable.tickServer(serverLevel);
                } else {
                    tickable.tickClient(level1);
                }
            }
        };
    }
}
