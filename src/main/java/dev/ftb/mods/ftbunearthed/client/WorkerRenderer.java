package dev.ftb.mods.ftbunearthed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ftb.mods.ftbunearthed.entity.Worker;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WorkerRenderer extends VillagerRenderer {
    public WorkerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);

        layers.removeIf(layer -> layer instanceof CrossedArmsItemLayer<Villager, VillagerModel<Villager>>);
        addLayer(new AnimatedToolLayer(this, ctx.getItemInHandRenderer()));
    }

    private static class AnimatedToolLayer extends RenderLayer<Villager, VillagerModel<Villager>> {
        private final ItemInHandRenderer itemInHandRenderer;

        public AnimatedToolLayer(WorkerRenderer workerRenderer, ItemInHandRenderer itemInHandRenderer) {
            super(workerRenderer);

            this.itemInHandRenderer = itemInHandRenderer;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Villager livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            boolean busy = livingEntity instanceof Worker w && w.isBusy();
            poseStack.pushPose();
            float xOff = busy ? getMovingOffset(livingEntity, partialTick): 0f;
            poseStack.translate(0F, 0.4F, -0.4F);
            if (busy) {
                poseStack.mulPose(Axis.YP.rotationDegrees(xOff + 90));
                poseStack.translate(0f, -0.1f, 0f);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-60));
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            }
            ItemStack itemstack = livingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
            this.itemInHandRenderer.renderItem(livingEntity, itemstack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight);
            poseStack.popPose();
        }

        private float getMovingOffset(LivingEntity e, float partialTick) {
            float t = (e.tickCount % 20 + partialTick) / 20 * Mth.TWO_PI;
            return Mth.sin(t) * 40f;
        }
    }
}
