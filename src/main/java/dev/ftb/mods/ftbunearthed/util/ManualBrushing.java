package dev.ftb.mods.ftbunearthed.util;

import dev.ftb.mods.ftbunearthed.crafting.RecipeCaches;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ManualBrushing {
    private static final Map<ResourceLocation, Object2FloatMap<BlockPos>> brushProgress = new Object2ObjectOpenHashMap<>();

    public static boolean tryManualBrushing(LivingEntity livingEntity, BlockHitResult blockHitResult) {
        if (!(livingEntity instanceof ServerPlayer player)) {
            return false;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        ItemStack input = player.level().getBlockState(pos).getBlock().asItem().getDefaultInstance();

        return findRecipe(player, input)
                .map(recipe -> doBrushing(player, pos, recipe))
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

    private static boolean doBrushing(ServerPlayer player, BlockPos pos, UneartherRecipe recipe) {
        Level level = player.level();

        Object2FloatMap<BlockPos> progressMap = brushProgress.computeIfAbsent(level.dimension().location(), k -> new Object2FloatOpenHashMap<>());
        float step = 1 / (recipe.getProcessingTime() / 100f);
        float progress = progressMap.getOrDefault(pos, 0f) + step;
        progressMap.put(pos, progress);

        if (progress >= 10) {
            level.destroyBlock(pos, false, player);
            recipe.generateOutputs(level.random).forEach(stack -> Block.popResource(level, pos, stack));
            progressMap.removeFloat(pos);
            sendBreakProgress(player, pos, 0);
        } else {
            sendBreakProgress(player, pos, (int) progress);
        }

        return true;
    }

    private static void sendBreakProgress(ServerPlayer player, BlockPos pos, int progress) {
        Vec3 vec = Vec3.atCenterOf(pos);
        for (ServerPlayer serverplayer : player.getServer().getPlayerList().getPlayers()) {
            if (serverplayer != null && serverplayer.level() == player.level() && serverplayer.distanceToSqr(vec) < 1024.0 * 1024.0) {
                serverplayer.connection.send(new ClientboundBlockDestructionPacket(player.getId(), pos, progress));
            }
        }
    }
}
