package dev.ftb.mods.ftbunearthed.block;

import dev.ftb.mods.ftbunearthed.util.VoxelShapeUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class UneartherFrameBlock extends Block implements EntityBlock {
    private static final Map<Part, VoxelShape> SHAPES = Util.make(new EnumMap<>(Part.class), map -> {
        map.put(Part.LOWER_CORNER, VoxelShapeUtils.or(
                box(0, 0, 0, 16, 8, 16),
                box(0, 8, 0, 4, 16, 4),
                box(4, 8, 1, 16, 16, 3),
                box(1, 8, 4, 3, 16, 16)
        ));
        map.put(Part.LOWER_EDGE, VoxelShapeUtils.or(
                box(0, 0, 0, 16, 8, 16),
                box(0, 8, 1, 16, 16, 3)
        ));
        map.put(Part.MID_EDGE, VoxelShapeUtils.or(
                box(0, 0, 0, 4, 16, 4),
                box(4, 0, 1, 16, 16, 3),
                box(1, 0, 4, 3, 16, 16)
        ));
        map.put(Part.MID_FACE, VoxelShapeUtils.or(
                box(0, 0, 1, 16, 16, 3)
        ));
        map.put(Part.UPPER_CORNER, VoxelShapeUtils.or(
                box(0, 0, 0, 4, 16, 4),
                box(4, 12, 0, 16, 16, 4),
                box(0, 12, 4, 4, 16, 16),
                box(4, 0, 1, 16, 12, 3),
                box(1, 0, 4, 3, 12, 16),
                box(4, 13, 4, 16, 15, 16)
        ));
        map.put(Part.UPPER_EDGE, VoxelShapeUtils.or(
                box(0, 12, 0, 16, 16, 4),
                box(0, 13, 4, 16, 15, 16),
                box(0, 0, 1, 16, 12, 3)
        ));
        map.put(Part.UPPER_FACE, VoxelShapeUtils.or(
                box(0, 13, 0, 16, 15, 16)
        ));
    });
    private static final Map<Direction,Map<Part, VoxelShape>> D_SHAPES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.NORTH, SHAPES);
        map.put(Direction.EAST, rotate(SHAPES, 90));
        map.put(Direction.SOUTH, rotate(SHAPES, 180));
        map.put(Direction.WEST, rotate(SHAPES, 270));
    });

    private static Map<Part,VoxelShape> rotate(Map<Part,VoxelShape> map, int rot) {
        return Util.make(new EnumMap<>(Part.class), res -> map.forEach((part, shape) -> res.put(part, VoxelShapeUtils.rotateY(shape, rot))));
    }

    private final Part part;

    public UneartherFrameBlock(Properties properties, Part part) {
        super(properties);

        this.part = part;

        BlockState state = getStateDefinition().any();
        state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UneartherFrameBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof UneartherFrameBlockEntity frame) {
            frame.getCore().ifPresent(core -> level.destroyBlock(core.getBlockPos(), true));
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return D_SHAPES.get(dir).get(part);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof UneartherFrameBlockEntity be) {
                be.getCore().ifPresent(core -> player.openMenu(core, core.getBlockPos()));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    public enum Part {
        LOWER_CORNER,
        LOWER_EDGE,
        MID_EDGE,
        MID_FACE,
        UPPER_CORNER,
        UPPER_EDGE,
        UPPER_FACE
    }
}
