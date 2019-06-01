package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;

@Mixin(ServerPlayNetworkHandler.class)
public class ChatMixin {
    @Shadow
    public ServerPlayerEntity player;

    private WeakReference<String> message;

    @Inject(at = @At(value = "HEAD"), method = "onChatMessage(Lnet/minecraft/server/network/packet/ChatMessageC2SPacket;)V")
    private void onChatMessage1(ChatMessageC2SPacket chatMessageC2SPacket_1, CallbackInfo info) {
        this.message = new WeakReference<>(chatMessageC2SPacket_1.getChatMessage());
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "net.minecraft.server.MinecraftServer.getPlayerManager()Lnet/minecraft/server/PlayerManager;", ordinal = 0, shift = At.Shift.AFTER), method = "onChatMessage(Lnet/minecraft/server/network/packet/ChatMessageC2SPacket;)V")
    private Component onChatMessage2(Component text) {
        NickUtil nickUtil = new NickUtil(player);
        String message = this.message.get();
        this.message.clear();

        if (message != null) {
            return new TranslatableComponent("chat.type.text", TextUtil.changeToColored(nickUtil.getName()), TextUtil.changeToColored(message));
        } else {
            return text;
        }
    }
}
