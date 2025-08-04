package dev.ftb.mods.ftbunearthed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlockEntity;
import dev.ftb.mods.ftbunearthed.network.UneartherStatusMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;

public class UneartherRenderer implements BlockEntityRenderer<UneartherCoreBlockEntity> {
    private final BlockEntityRendererProvider.Context ctx;

    public UneartherRenderer(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(UneartherCoreBlockEntity unearther, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (unearther.getLevel().isLoaded(unearther.getBlockPos())) {
            UneartherStatusMessage.ClientStatus status = unearther.getClientStatus();

            if (status.block() != Blocks.AIR) {
                Direction d = unearther.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                poseStack.pushPose();
                poseStack.translate(0.5 * d.getStepX(), 0.5, 0.5 * d.getStepZ());
                renderBlock(poseStack, bufferSource, packedLight, packedOverlay, status.block(), unearther.getClientBreakProgress());
                poseStack.popPose();
            }
        }
    }

    public void renderBlock(PoseStack poseStack, MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn, Block block, int breakProgress) {
        var b = Minecraft.getInstance().levelRenderer.renderBuffers.crumblingBufferSource();
        VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(b.getBuffer(ModelBakery.DESTROY_TYPES.get(breakProgress)), poseStack.last(), 1.0F);
        MultiBufferSource bufferSource1 = type -> {
            VertexConsumer vc = bufferSource.getBuffer(type);
            return type.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, vc) : vc;
        };

        BlockState state = block.defaultBlockState();
        ctx.getBlockRenderDispatcher().renderSingleBlock(state, poseStack, bufferSource1, combinedLightIn, combinedOverlayIn, ModelData.EMPTY, null);
    }
}
