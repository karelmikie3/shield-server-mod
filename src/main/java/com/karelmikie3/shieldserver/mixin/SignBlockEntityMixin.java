package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.entity.ShieldSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin implements ShieldSignBlockEntity {
    private GateState state = GateState.NOT;

    @Override
    public void setGateState(GateState state) {
        System.out.println("changing state to: " + state);
        this.state = state;
    }

    @Override
    public GateState getGateState() {
        return state;
    }

    @Inject(at = @At("HEAD"), method = "toTag")
    public void toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info) {
        tag.putByte("warpstate", state.id);
    }

    @Inject(at = @At("HEAD"), method = "fromTag")
    public void fromTag(CompoundTag tag, CallbackInfo info) {
        state = GateState.byId(tag.getByte("warpstate"));
    }
}
