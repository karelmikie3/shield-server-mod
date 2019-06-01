package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(PlayerListS2CPacket.class)
public class PlayerListMixin {

    @Shadow
    private List<PlayerListS2CPacket.Entry> entries;

    @ModifyArg(at = @At(value = "INVOKE", target = "com.mojang.authlib.GameProfile.getName()Ljava/lang/String;", shift = At.Shift.AFTER), method = "write(Lnet/minecraft/util/PacketByteBuf;)V")
    private String changeName(String name) {
        if (entries != null) {
            for (PlayerListS2CPacket.Entry entry : entries) {
                if (entry.getProfile().getName().equals(name)) {
                    NickUtil nickUtil = new NickUtil(entry.getProfile().getId());

                    if (nickUtil.hasNick()) {
                        String nick = TextUtil.changeToColored(nickUtil.getNickString()).getFormattedText();
                        int maxLength = (nick.length() < 16) ? nick.length() : 16;
                        nick = nick.substring(0, maxLength);
                        return nick;
                    } else {
                        return name;
                    }
                }
            }
        }
        return name;
    }
}
