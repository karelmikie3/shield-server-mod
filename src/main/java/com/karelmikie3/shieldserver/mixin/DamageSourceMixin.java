package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DamageSource.class)
public class DamageSourceMixin {
    @Shadow
    @Final
    public String name;

    @Overwrite
    public Component getDeathMessage(LivingEntity livingEntity_1) {
        Component name = livingEntity_1.getDisplayName();
        if (livingEntity_1 instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) livingEntity_1;
            NickUtil nickUtil = new NickUtil(player);
            name = TextUtil.changeToColored(nickUtil.getName());
        }

        LivingEntity livingEntity_2 = livingEntity_1.method_6124();
        String string_1 = "death.attack." + this.name;
        String string_2 = string_1 + ".player";
        return livingEntity_2 != null ? new TranslatableComponent(string_2, name, livingEntity_2.getDisplayName()) : new TranslatableComponent(string_1, name);
    }
}
