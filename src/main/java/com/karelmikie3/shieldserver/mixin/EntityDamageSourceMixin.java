package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityDamageSource.class)
public class EntityDamageSourceMixin extends DamageSource {
    @Shadow
    @Final
    public Entity source;

    public EntityDamageSourceMixin() {
        super(null);
    }

    @Overwrite
    public Component getDeathMessage(LivingEntity livingEntity_1) {
        Component name = livingEntity_1.getDisplayName();
        if (livingEntity_1 instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) livingEntity_1;
            NickUtil nickUtil = new NickUtil(player);
            name = TextUtil.changeToColored(nickUtil.getName());
        }

        Component name2 = this.source.getDisplayName();
        if (this.source instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) this.source;
            NickUtil nickUtil = new NickUtil(player);
            name2 = TextUtil.changeToColored(nickUtil.getName());
        }

        ItemStack itemStack_1 = this.source instanceof LivingEntity ? ((LivingEntity)this.source).getMainHandStack() : ItemStack.EMPTY;
        String string_1 = "death.attack." + this.name;
        return !itemStack_1.isEmpty() && itemStack_1.hasDisplayName() ? new TranslatableComponent(string_1 + ".item", name, name2, itemStack_1.toTextComponent()) : new TranslatableComponent(string_1, name, name2);
    }
}
