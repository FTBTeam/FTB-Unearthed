package dev.ftb.mods.ftbunearthed.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbunearthed.registry.ModAttachmentTypes;
import dev.ftb.mods.ftbunearthed.util.MiscUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GetLevelCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("getlevel")
                .requires(ctx -> ctx.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(argument("player", EntityArgument.player())
                        .executes(ctx -> getLevel(ctx, EntityArgument.getPlayer(ctx, "player")))
                );
    }

    private static int getLevel(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        ctx.getSource().sendSuccess(() -> MiscUtil.formatPlayerUneartherLevel(player), true);
        return Command.SINGLE_SUCCESS;
    }
}
