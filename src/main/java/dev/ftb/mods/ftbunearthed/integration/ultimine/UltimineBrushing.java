package dev.ftb.mods.ftbunearthed.integration.ultimine;

import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Optional;

public class UltimineBrushing {
    public static Optional<Collection<BlockPos>> getPositions(ServerPlayer player) {
        return FTBUltimineAPI.api().currentBlockSelection(player);
    }
}
