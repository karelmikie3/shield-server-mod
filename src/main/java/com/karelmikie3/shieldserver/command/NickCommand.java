package com.karelmikie3.shieldserver.command;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormat;
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class NickCommand {
    private static final SimpleCommandExceptionType EMPTY_NOT_ALLOWED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableComponent("commands.nick.error.empty"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager
                .literal("nick")
                .then(CommandManager
                        .literal("set")
                        .then(CommandManager
                                .argument("nick", StringArgumentType.greedyString())
                                        .executes(context -> executeSet(context.getSource(), context.getSource().getPlayer(), StringArgumentType.getString(context, "nick")))
                        ).then(CommandManager
                                .argument("player", EntityArgumentType.player())
                                .requires(source -> source.hasPermissionLevel(3))
                                .then(CommandManager
                                        .argument("nick", StringArgumentType.greedyString())
                                        .executes(context -> executeSet(context.getSource(), EntityArgumentType.getPlayer(context, "player"), StringArgumentType.getString(context, "nick")))
                                )
                        )
                ).then(CommandManager
                        .literal("remove")
                        .executes(context -> execute(context.getSource(), context.getSource().getPlayer(), ""))
                        .then(CommandManager
                                .argument("player", EntityArgumentType.player())
                                .requires(source -> source.hasPermissionLevel(3))
                                .executes(context -> execute(context.getSource(), EntityArgumentType.getPlayer(context, "player"), ""))
                        )
                ).then(CommandManager
                        .literal("get")
                        .executes(context -> executeGet(context.getSource(), context.getSource().getPlayer()))
                        .then(CommandManager
                                        .argument("player", EntityArgumentType.player())
                                        .executes(context-> executeGet(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                        )
                )
        );
    }

    private static int executeGet(ServerCommandSource source, ServerPlayerEntity player) {
        NickUtil nickUtil = new NickUtil(player);
        boolean own = source.getEntity() == player;

        if (nickUtil.hasNick()) {
            Component nick = nickUtil.getNick();
            Component text = new TranslatableComponent("commands.nick.get." + (own ? "self" : "other"), nick, player.getDisplayName());
            text.modifyStyle(style -> style.setColor(ChatFormat.GREEN));
            source.sendFeedback(text, false);
        } else {
            Component text = new TranslatableComponent("commands.nick.get.none." + (own ? "self" : "other"), player.getDisplayName());
            text.modifyStyle(style -> style.setColor(ChatFormat.RED));
            source.sendFeedback(text, false);
        }
        return 1;
    }

    private static int executeSet(ServerCommandSource source, ServerPlayerEntity player, String nick) throws CommandSyntaxException {
        if (nick.isEmpty())
            throw EMPTY_NOT_ALLOWED_EXCEPTION.create();

        return execute(source, player, nick);
    }


    private static int execute(ServerCommandSource source, ServerPlayerEntity player, String nick) {
        NickUtil nickUtil = new NickUtil(player);
        String oldNick = nickUtil.getNickString();
        nick = nick.trim();

        if (!oldNick.equals(nick))
            nickUtil.setNick(nick);

        Component nickC = TextUtil.changeToColored(nickUtil.getNick());

        boolean own = source.getEntity() == player;

        if (oldNick.equals(nick)) {
            Component text = new TranslatableComponent("commands.nick.same." + (own ? "self" : "other"), nickC, player.getDisplayName());
            text.modifyStyle(style -> style.setColor(ChatFormat.YELLOW));
            source.sendFeedback(text, false);


        } else if (nick.isEmpty()) {
            Component text = new TranslatableComponent("commands.nick.removed." + (own ? "self" : "other"), player.getDisplayName());
            text.modifyStyle(style -> style.setColor(ChatFormat.DARK_RED));
            source.sendFeedback(text, false);


        } else {
            Component text = new TranslatableComponent("commands.nick.set." + (own ? "self" : "other"), nickC, player.getDisplayName());
            text.modifyStyle(style -> style.setColor(ChatFormat.GREEN));
            source.sendFeedback(text, false);
        }


        //ignore this weirdness it's to make my packet hack work to display the name above the player's head without the mod on the client.
        PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player);
        player.server.getPlayerManager().sendToAll(packet);

        ServerWorld world = player.server.getWorld(player.world.getDimension().getType());
        world.removePlayer(player);
        player.removed = false;
        player.setWorld(world);
        world.method_18207(player);

        player.networkHandler.requestTeleport(player.x, player.y, player.z, player.yaw, player.pitch);

        return !oldNick.equals(nick) ? 1 : 0;
    }
}
