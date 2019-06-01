package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.util.NickUtil;
import com.karelmikie3.shieldserver.util.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(DamageTracker.class)
public abstract class DamageTrackerMixin {
    @Shadow
    @Final
    public LivingEntity entity;

    @Shadow
    @Final
    public List<DamageRecord> recentDamage;

    @Shadow
    public abstract DamageRecord getBiggestFall();

    @Shadow
    public abstract String getFallDeathSuffix(DamageRecord damageRecord_1);

    @Overwrite
    public Component getDeathMessage() {
        Component name = this.entity.getDisplayName();
        if (this.entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) this.entity;
            NickUtil nickUtil = new NickUtil(player);
            name = TextUtil.changeToColored(nickUtil.getName());
        }

        if (this.recentDamage.isEmpty()) {
            return new TranslatableComponent("death.attack.generic", name);
        } else {
            DamageRecord damageRecord_1 = this.getBiggestFall();
            DamageRecord damageRecord_2 = this.recentDamage.get(this.recentDamage.size() - 1);
            Component component_1 = damageRecord_2.getAttackerName();
            Entity entity_1 = damageRecord_2.getDamageSource().getAttacker();
            Component component_9;
            if (damageRecord_1 != null && damageRecord_2.getDamageSource() == DamageSource.FALL) {
                Component component_2 = damageRecord_1.getAttackerName();
                if (damageRecord_1.getDamageSource() != DamageSource.FALL && damageRecord_1.getDamageSource() != DamageSource.OUT_OF_WORLD) {
                    if (component_2 == null || component_2.equals(component_1)) {
                        if (component_1 != null) {
                            ItemStack itemStack_2 = entity_1 instanceof LivingEntity ? ((LivingEntity)entity_1).getMainHandStack() : ItemStack.EMPTY;
                            if (!itemStack_2.isEmpty() && itemStack_2.hasDisplayName()) {
                                component_9 = new TranslatableComponent("death.fell.finish.item", name, component_1, itemStack_2.toTextComponent());
                            } else {
                                component_9 = new TranslatableComponent("death.fell.finish", name, component_1);
                            }
                        } else {
                            component_9 = new TranslatableComponent("death.fell.killer", name);
                        }
                    } else {
                        Entity entity_2 = damageRecord_1.getDamageSource().getAttacker();
                        ItemStack itemStack_1 = entity_2 instanceof LivingEntity ? ((LivingEntity)entity_2).getMainHandStack() : ItemStack.EMPTY;
                        if (!itemStack_1.isEmpty() && itemStack_1.hasDisplayName()) {
                            component_9 = new TranslatableComponent("death.fell.assist.item", name, component_2, itemStack_1.toTextComponent());
                        } else {
                            component_9 = new TranslatableComponent("death.fell.assist", name, component_2);
                        }
                    }
                } else {
                    component_9 = new TranslatableComponent("death.fell.accident." + this.getFallDeathSuffix(damageRecord_1), name);
                }
            } else {
                component_9 = damageRecord_2.getDamageSource().getDeathMessage(this.entity);
            }

            return component_9;
        }
    }
}
