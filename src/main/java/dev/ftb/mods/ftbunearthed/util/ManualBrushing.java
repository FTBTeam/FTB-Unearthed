package dev.ftb.mods.ftbunearthed.util;

import dev.ftb.mods.ftbunearthed.crafting.RecipeCaches;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
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
import java.util.UUID;

public class ManualBrushing {
    private static final Map<UUID, Object2IntMap<BlockPos>> brushProgress = new Object2ObjectOpenHashMap<>();

    public static boolean tryManualBrushing(Level level, LivingEntity livingEntity, BlockHitResult blockHitResult) {
        if (level.isClientSide || !(livingEntity instanceof ServerPlayer player)) {
            return false;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        ItemStack input = level.getBlockState(pos).getBlock().asItem().getDefaultInstance();

        return findRecipe(player, input)
                .map(recipe -> doBrushing(level, player, pos, recipe))
                .orElse(false);
    }

    private static Optional<UneartherRecipe> findRecipe(ServerPlayer player, ItemStack input) {
        return RecipeCaches.MANUAL_BRUSHING.getCachedRecipe(
                        () -> player.level().getRecipeManager().getAllRecipesFor(ModRecipes.UNEARTHER_TYPE.get()).stream()
                                .filter(holder -> holder.value().testManual(input, player.getMainHandItem()))
                                .findFirst(),
                        () -> Objects.hash(
                                input,
                                ItemStack.hashItemAndComponents(player.getMainHandItem())
                        ))
                .map(RecipeHolder::value);
    }

    private static boolean doBrushing(Level level, ServerPlayer player, BlockPos pos, UneartherRecipe recipe) {
        Object2IntMap<BlockPos> posMap = brushProgress.computeIfAbsent(player.getUUID(), k -> new Object2IntOpenHashMap<>());
        int progress = posMap.getOrDefault(pos, 1);
        posMap.put(pos, progress + 1);
        if (progress >= 10) {
            level.destroyBlock(pos, false, player);
            recipe.generateOutputs(level.random).forEach(stack -> Block.popResource(level, pos, stack));
            posMap.removeInt(pos);
        } else {
            Vec3 vec = Vec3.atCenterOf(pos);
            for (ServerPlayer serverplayer : level.getServer().getPlayerList().getPlayers()) {
                if (serverplayer != null && serverplayer.level() == level && serverplayer.distanceToSqr(vec) < 1024.0 * 1024.0) {
                    serverplayer.connection.send(new ClientboundBlockDestructionPacket(player.getId(), pos, progress));
                }
            }
        }
        return true;
    }
}
