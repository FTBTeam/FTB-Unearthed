package dev.ftb.mods.ftbunearthed.item;

import dev.ftb.mods.ftbunearthed.item.WorkerToken.WorkerData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EchoEncoder extends Item {
    private static final int DURABILITY_PER_USE = 50;

    public EchoEncoder(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof Villager villager && villager.getAge() >= 0) {
            if (player.level() instanceof ServerLevel serverLevel) {
                if (stack.getMaxDamage() - stack.getDamageValue() < DURABILITY_PER_USE) {
                    player.displayClientMessage(Component.translatable("ftbunearthed.message.item_too_damaged", stack.getHoverName()).withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }
                ItemStack token = WorkerToken.createWithData(WorkerData.fromVillagerData(villager.getVillagerData()));
                Block.popResource(player.level(), villager.blockPosition(), token);

                serverLevel.playSound(null, villager.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1f);
                Vec3 pos = villager.getPosition(1f).add(0, 1, 0);
                serverLevel.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 50, 0.2, 0.5, 0.2, 0);

                villager.setTradingPlayer(null);
                villager.releasePoi(MemoryModuleType.HOME);
                villager.releasePoi(MemoryModuleType.JOB_SITE);
                villager.releasePoi(MemoryModuleType.POTENTIAL_JOB_SITE);
                villager.releasePoi(MemoryModuleType.MEETING_POINT);
                villager.discard();

                stack.hurtAndBreak(DURABILITY_PER_USE, player, usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("ftbunearthed.tooltip.echo_encoder_charges",
                (stack.getMaxDamage() - stack.getDamageValue()) / DURABILITY_PER_USE).withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("ftbunearthed.tooltip.echo_encoder_usage").withStyle(ChatFormatting.GRAY));
    }
}
