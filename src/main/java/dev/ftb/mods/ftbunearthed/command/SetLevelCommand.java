package dev.ftb.mods.ftbunearthed.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbunearthed.util.MiscUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SetLevelCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("setlevel")
                .requires(ctx -> ctx.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(argument("player", EntityArgument.player())
                        .then(argument("level", IntegerArgumentType.integer(1, 5))
                                .executes(ctx -> setLevel(ctx,
                                        EntityArgument.getPlayer(ctx, "player"),
                                        IntegerArgumentType.getInteger(ctx, "level")
                                ))
                        )
                );
    }

    private static int setLevel(CommandContext<CommandSourceStack> ctx, ServerPlayer player, int level) {
        MiscUtil.setPlayerUneartherLevel(player, level);
        ctx.getSource().sendSuccess(() -> MiscUtil.formatPlayerUneartherLevel(player), true);
        return Command.SINGLE_SUCCESS;
    }
}
