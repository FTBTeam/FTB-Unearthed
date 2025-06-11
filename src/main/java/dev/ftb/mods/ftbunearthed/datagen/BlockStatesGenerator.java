package dev.ftb.mods.ftbunearthed.datagen;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class BlockStatesGenerator extends BlockStateProvider {
    private static final Direction[] HORIZONTALS = new Direction[] {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public BlockStatesGenerator(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, FTBUnearthed.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerOrientableBlock(ModBlocks.UNEARTHER);
    }

    private void registerOrientableBlock(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();

        ModelFile model = models().withExistingParent(name, "block/orientable")
                .texture("front", modid("block/%s_front", name))
                .texture("side", modid("block/%s_side", name))
                .texture("top", modid("block/%s_top", name));

        VariantBlockStateBuilder.PartialBlockstate builder = getVariantBuilder(block.get()).partialState();
        for (Direction d : HORIZONTALS) {
            builder.with(HorizontalDirectionalBlock.FACING, d)
                    .setModels(new ConfiguredModel(model, 0, getYRotation(d), false));
        }

        simpleBlockItem(block.get(), model);
    }

    private int getYRotation(Direction dir) {
        return switch (dir) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> throw new IllegalArgumentException("invalid dir");
        };
    }

    static String modid(String s, Object... args) {
        return FTBUnearthed.MODID + ":" + String.format(s, args);
    }
}
