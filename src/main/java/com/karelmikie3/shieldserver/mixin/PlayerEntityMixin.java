package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.entity.ShieldPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ShieldPlayerEntity {
    @Shadow
    abstract Component addTellClickEvent(Component textComponent_1);

    @Override
    public Component shield_addTellClickEvent(Component textComponent) {
        return addTellClickEvent(textComponent);
    }
}
