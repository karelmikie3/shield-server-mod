package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.entity.ShieldServerInteractionManager;
import com.karelmikie3.shieldserver.entity.ShieldSignBlockEntity;
import net.minecraft.ChatFormat;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin implements ShieldServerInteractionManager {
    @Shadow
    public ServerWorld world;

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    abstract boolean destroyBlock(BlockPos pos);

    @Override
    public boolean shield_destroyBlock(BlockPos pos) {
        return destroyBlock(pos);
    }

    private SignBlockEntity signBlockEntity;

    @Inject(at=@At("HEAD"), method = "tryBreakBlock")
    public void tryBreakBlock2(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof SignBlockEntity) {
            signBlockEntity = (SignBlockEntity) world.getBlockEntity(pos);
        }
    }

    @Inject(at=@At("TAIL"), method = "tryBreakBlock")
    public void tryBreakBlock1(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (signBlockEntity != null) {
            ShieldSignBlockEntity shieldSignBlockEntity = (ShieldSignBlockEntity) signBlockEntity;
            if (shieldSignBlockEntity.getGateState() == ShieldSignBlockEntity.GateState.REMOVED)
                player.sendChatMessage(new TranslatableComponent("warp.removed", signBlockEntity.text[2].getText().trim()).modifyStyle(style -> style.setBold(true).setColor(ChatFormat.YELLOW)), ChatMessageType.GAME_INFO);

            signBlockEntity = null;
        }
    }
}
