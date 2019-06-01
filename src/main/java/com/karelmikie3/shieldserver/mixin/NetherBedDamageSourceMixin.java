package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.NetherBedDamageSource;
import net.minecraft.network.chat.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(NetherBedDamageSource.class)
public class NetherBedDamageSourceMixin {
    @Overwrite
    public Component getDeathMessage(LivingEntity livingEntity_1) {
        Component name = livingEntity_1.getDisplayName();
        if (livingEntity_1 instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) livingEntity_1;
            NickUtil nickUtil = new NickUtil(player);
            name = TextUtil.changeToColored(nickUtil.getName());
        }

        Component component_1 = Components.bracketed(new TranslatableComponent("death.attack.netherBed.link")).modifyStyle((style_1) ->
                style_1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("MCPE-28723"))));
        return new TranslatableComponent("death.attack.netherBed.message", name, component_1);
    }
}
