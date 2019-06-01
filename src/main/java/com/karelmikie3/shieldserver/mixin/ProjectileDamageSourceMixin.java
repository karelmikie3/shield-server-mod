package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ProjectileDamageSource.class)
public class ProjectileDamageSourceMixin extends EntityDamageSource {
    @Shadow
    @Final
    public Entity attacker;

    public ProjectileDamageSourceMixin() {
        super(null, null);
    }

    @Overwrite
    public Component getDeathMessage(LivingEntity livingEntity_1) {
        //DamageSource current = DamageSource.class.cast(this);

        Component name = livingEntity_1.getDisplayName();
        if (livingEntity_1 instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) livingEntity_1;
            NickUtil nickUtil = new NickUtil(player);
            name = TextUtil.changeToColored(nickUtil.getName());
        }

        Component name2;

        if (this.attacker == null) {
            name2 = source.getDisplayName();
            if (source instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) source;
                NickUtil nickUtil = new NickUtil(player);
                name2 = TextUtil.changeToColored(nickUtil.getName());
            }
        } else {
            name2 = this.attacker.getDisplayName();
            if (this.attacker instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) this.attacker;
                NickUtil nickUtil = new NickUtil(player);
                name2 = TextUtil.changeToColored(nickUtil.getName());
            }
        }


        ItemStack itemStack_1 = this.attacker instanceof LivingEntity ? ((LivingEntity)this.attacker).getMainHandStack() : ItemStack.EMPTY;
        String string_1 = "death.attack." + this.name;
        String string_2 = string_1 + ".item";
        return !itemStack_1.isEmpty() && itemStack_1.hasDisplayName() ? new TranslatableComponent(string_2, name, name2, itemStack_1.toTextComponent()) : new TranslatableComponent(string_1, name, name2);
    }
}
