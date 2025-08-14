package dev.ftb.mods.ftbunearthed.util;

import com.mojang.serialization.DataResult;
import dev.ftb.mods.ftbunearthed.registry.ModAttachmentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.Validate;

public class MiscUtil {
    public static final Direction[] HORIZONTALS = new Direction[] {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    public static final ChatFormatting[] LEVEL_COLORS = new ChatFormatting[] {
            ChatFormatting.GRAY, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.AQUA
    };

    public static DataResult<Double> validateChanceRange(double d) {
        return d > 0.0 ? DataResult.success(d) : DataResult.error(() -> "must be > 0.0");
    }

    public static int getPlayerUneatherLevel(Player player) {
        return Mth.clamp(player.getExistingData(ModAttachmentTypes.UNEARTHER_LEVEL).orElse(1), 1, 5);
    }

    public static void setPlayerUneartherLevel(Player player, int level) {
        Validate.isTrue(level >= 1 && level <= 5, "level must be in range 1-5");
        player.setData(ModAttachmentTypes.UNEARTHER_LEVEL, level);
    }

    public static Component formatPlayerUneartherLevel(Player player) {
        int lvl = MiscUtil.getPlayerUneatherLevel(player);

        return Component.translatable("ftbunearthed.message.unearther_level.player",
                player.getDisplayName(),
                Component.literal(String.valueOf(lvl)).withStyle(LEVEL_COLORS[lvl - 1]),
                Component.translatable("merchant.level." + lvl).withStyle(LEVEL_COLORS[lvl - 1])
        );
    }

    public static Component formatUneartherLevel(Player player) {
        int lvl = MiscUtil.getPlayerUneatherLevel(player);

        return Component.translatable("ftbunearthed.message.unearther_level",
                Component.literal(String.valueOf(lvl)).withStyle(LEVEL_COLORS[lvl - 1]),
                Component.translatable("merchant.level." + lvl).withStyle(LEVEL_COLORS[lvl - 1])
        );
    }
}
