package dev.ftb.mods.ftbunearthed.util;

import com.mojang.serialization.DataResult;
import dev.ftb.mods.ftbunearthed.registry.ModAttachmentTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.Validate;

public class MiscUtil {
    public static final Direction[] HORIZONTALS = new Direction[] {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public static DataResult<Double> validateChanceRange(double d) {
        return d > 0.0 ? DataResult.success(d) : DataResult.error(() -> "must be > 0.0");
    }

    public static int getPlayerUneatherLevel(Player player) {
        return player.hasData(ModAttachmentTypes.UNEARTHER_LEVEL) ? player.getData(ModAttachmentTypes.UNEARTHER_LEVEL) : 1;
    }

    public static void setPlayerUneartherLevel(Player player, int level) {
        Validate.isTrue(level >= 1 && level <= 5, "level must be in range 1-5");
        player.setData(ModAttachmentTypes.UNEARTHER_LEVEL, level);
    }
}
