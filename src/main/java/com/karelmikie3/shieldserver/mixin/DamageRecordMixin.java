package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DamageRecord.class)
public abstract class DamageRecordMixin {

    @Shadow
    public abstract DamageSource getDamageSource();

    @Overwrite
    public Component getAttackerName() {
        if (this.getDamageSource().getAttacker() == null) {
            return null;
        }

        if (this.getDamageSource().getAttacker() instanceof ServerPlayerEntity) {
            NickUtil nickUtil = new NickUtil((ServerPlayerEntity) this.getDamageSource().getAttacker());

            return TextUtil.changeToColored(nickUtil.getName());
        }

        return this.getDamageSource().getAttacker().getDisplayName();
    }
}
