package dev.ftb.mods.ftbunearthed.datagen;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class BlockStatesGenerator extends BlockStateProvider {
    public BlockStatesGenerator(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, FTBUnearthed.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        rotatableBlock(ModBlocks.CORE);

        rotatableBlock(ModBlocks.L_CORNER);
        rotatableBlock(ModBlocks.L_EDGE);
        rotatableBlock(ModBlocks.M_EDGE);
        rotatableBlock(ModBlocks.M_FACE);
        rotatableBlock(ModBlocks.U_CORNER);
        rotatableBlock(ModBlocks.U_EDGE);
        rotatableBlock(ModBlocks.U_FACE);

        simpleBlockItem(ModBlocks.CORE.get(), models().getExistingFile(modLoc("item/unearther_item")));
    }

    private void rotatableBlock(DeferredBlock<? extends Block> block) {
        var model = models().getExistingFile(modLoc("block/" + block.getId().getPath()));
        getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(model)
                .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360).build());
    }
}
