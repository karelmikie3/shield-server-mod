package com.karelmikie3.shieldserver.command;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class MeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager
                .literal("me")
                .then(CommandManager
                        .argument("action", StringArgumentType.greedyString())
                        .executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "action")))
                )
        );
    }

    private static int execute(ServerCommandSource source, String message) {
        Component name = source.getDisplayName();
        if (source.getEntity() != null && source.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
            NickUtil nickUtil = new NickUtil(player);
            name = nickUtil.getName();
        }
        PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();

        playerManager.sendToAll(new TranslatableComponent("chat.type.emote", TextUtil.changeToColored(name), TextUtil.changeToColored(message)));
        return 1;
    }
}
