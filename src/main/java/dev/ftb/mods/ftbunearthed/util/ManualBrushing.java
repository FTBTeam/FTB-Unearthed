package dev.ftb.mods.ftbunearthed.util;

import dev.ftb.mods.ftbunearthed.config.ServerConfig;
import dev.ftb.mods.ftbunearthed.crafting.RecipeCaches;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.integration.ultimine.UltimineIntegration;
import dev.ftb.mods.ftbunearthed.net.SendMultibreakProgressMessage;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class ManualBrushing {
    private static final Map<ResourceLocation, Object2FloatMap<BlockPos>> brushProgress = new Object2ObjectOpenHashMap<>();

    public static boolean tryManualBrushing(LivingEntity livingEntity, ItemStack stack, BlockHitResult blockHitResult) {
        if (!(livingEntity instanceof ServerPlayer player)) {
            return false;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        ItemStack input = player.level().getBlockState(pos).getBlock().asItem().getDefaultInstance();

        return findRecipe(player, input)
                .map(recipe -> doBrushing(player, stack, pos, recipe))
                .orElse(false);
    }

    private static Optional<UneartherRecipe> findRecipe(ServerPlayer player, ItemStack input) {
        int lvl = MiscUtil.getPlayerUneatherLevel(player);
        ItemStack toolStack = player.getMainHandItem();

        return RecipeCaches.MANUAL_BRUSHING.getCachedRecipe(
                () -> RecipeCaches.sortedUneartherRecipes(player.level()).stream()
                        .filter(holder -> holder.value().testManual(input, lvl, toolStack))
                        .findFirst(),
                () -> Objects.hash(input, ItemStack.hashItemAndComponents(toolStack), lvl)
        ).map(RecipeHolder::value);
    }

    private static boolean doBrushing(ServerPlayer player, ItemStack stack, BlockPos pos, UneartherRecipe recipe) {
        Level level = player.level();

        // TODO this would be nicer as some kind of plugin system, but this works fine for now
        List<BlockPos> allPositions = new ArrayList<>(UltimineIntegration.getSelectedPositions(player, pos));
        if (allPositions.isEmpty()) {
            return true;
        }

        // honour ultimine break prevention config, but only if we are brushing multiple blocks
        int minToolDurability = allPositions.size() > 1 ? UltimineIntegration.minToolDurability() : 0;

        Object2FloatMap<BlockPos> progressMap = brushProgress.computeIfAbsent(level.dimension().location(), k -> new Object2FloatOpenHashMap<>());
        float duration = recipe.getProcessingTime() / ServerConfig.MANUAL_BRUSHING_SPEEDUP.get().floatValue();
        float step = 1 / (duration / 100f);
        float progress = progressMap.getOrDefault(pos, 0f) + step;
        progressMap.put(pos, progress);

        if (progress >= 10f) {
            sendBreakProgress(player, pos, allPositions, -1);
            EquipmentSlot slot = ItemStack.isSameItemSameComponents(stack, player.getItemBySlot(EquipmentSlot.OFFHAND)) ?
                    EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
            for (BlockPos p1 : allPositions) {
                ItemStack tool = player.getItemBySlot(slot);
                if (tool.isEmpty() || tool.getDamageValue() >= tool.getMaxDamage() - minToolDurability) {
                    break;
                }
                level.destroyBlock(p1, false, player);
                recipe.generateOutputs(player.getRandom()).forEach(output -> Block.popResource(level, pos, output));
                if (recipe.getDamageChance() >= 1f || player.getRandom().nextFloat() < recipe.getDamageChance()) {
                    stack.hurtAndBreak(1, player, slot);
                }
            }
            progressMap.removeFloat(pos);
        } else {
            sendBreakProgress(player, pos, allPositions, (int) progress);
        }

        return true;
    }

    private static void sendBreakProgress(ServerPlayer player, BlockPos pos0, Collection<BlockPos> allPositions, int progress) {
        PacketDistributor.sendToPlayersTrackingChunk(
                player.serverLevel(),
                player.chunkPosition(),
                SendMultibreakProgressMessage.create(pos0, allPositions, progress)
        );
    }
}
