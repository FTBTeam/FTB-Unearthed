package dev.ftb.mods.ftbunearthed.command;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ModCommands {
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(FTBUnearthed.MODID)
                .then(GetLevelCommand.register())
                .then(SetLevelCommand.register())
        );
    }
}
