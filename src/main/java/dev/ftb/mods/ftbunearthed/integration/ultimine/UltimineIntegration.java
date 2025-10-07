package dev.ftb.mods.ftbunearthed.integration.ultimine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import java.util.Collection;
import java.util.List;

public class UltimineIntegration {
    private static boolean ultimineInstalled = false;

    public static void init() {
        ultimineInstalled = ModList.get().isLoaded("ftbultimine");
    }

    public static Collection<BlockPos> getSelectedPositions(ServerPlayer player, BlockPos origin) {
        return ultimineInstalled ?
                UltimineBrushing.getPositions(player).orElse(List.of(origin)) :
                List.of(origin);
    }

    public static int minToolDurability() {
        return ultimineInstalled ? UltimineBrushing.minToolDurability() : 0;
    }
}
