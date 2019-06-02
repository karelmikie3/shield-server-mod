package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.entity.ShieldServerInteractionManager;
import com.karelmikie3.shieldserver.entity.ShieldSignBlockEntity;
import com.karelmikie3.shieldserver.util.WarpUtil;
import net.minecraft.ChatFormat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.UpdateSignC2SPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

import static com.karelmikie3.shieldserver.util.WarpUtil.requirements;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    private MinecraftServer server;

    @Inject(at = @At("TAIL"), method = "onSignUpdate")
    private void onSignupdate(UpdateSignC2SPacket updateSignC2SPacket_1, CallbackInfo info) {
        ServerWorld world = this.server.getWorld(this.player.dimension);
        BlockPos pos = updateSignC2SPacket_1.getPos();

        if (world.isBlockLoaded(pos)) {
            BlockState state = world.getBlockState(pos);

            if (!(state.getBlock() instanceof WallSignBlock))
                return;

            BlockEntity entity = world.getBlockEntity(pos);
            if (!(entity instanceof SignBlockEntity))
                return;

            SignBlockEntity signEntity = (SignBlockEntity) entity;
            ShieldSignBlockEntity shieldSignEntity = (ShieldSignBlockEntity) entity;
            if (!signEntity.isEditable() || signEntity.getEditor() != this.player)
                return;


            String[] text = updateSignC2SPacket_1.getText();

            if (text[1].equals("[warp]")) {
                String id = text[2];

                Direction facing = state.get(WallSignBlock.FACING);
                Direction opposite = facing.getOpposite();

                BlockPos obsidianPos = pos.add(opposite.getVector());

                if (world.getBlockState(obsidianPos).getBlock() != Blocks.OBSIDIAN) {
                    breakInvalidSign(world, pos, state, entity, shieldSignEntity);
                    return;
                }

                BlockPos portalPos1 = obsidianPos.add(opposite.rotateYCounterclockwise().getVector());
                BlockPos portalPos2 = obsidianPos.add(opposite.rotateYClockwise().getVector());

                boolean found1 = world.getBlockState(portalPos1).getBlock() == Blocks.NETHER_PORTAL;
                boolean found2 = world.getBlockState(portalPos2).getBlock() == Blocks.NETHER_PORTAL;

                if (!(found1 || found2)) {
                    breakInvalidSign(world, pos, state, entity, shieldSignEntity);
                    return;
                }
                List<ItemStack> stacks = null;
                if (!player.isCreative()) {
                    List<ItemStack> inventory = player.inventory.main;
                    stacks = requirements
                            .stream()
                            .flatMap(stack -> inventory.stream().filter(stack2 -> stack.getItem() == stack2.getItem()))
                            .collect(Collectors.toList());

                    boolean enough = true;
                    for (ItemStack requirement : requirements) {
                        long amount = stacks
                                .stream()
                                .filter(stack -> requirement.getItem() == stack.getItem())
                                .count();

                        enough &= amount >= requirement.getAmount();
                    }

                    if (!enough) {
                        player.sendChatMessage(new TranslatableComponent("warp.make.nomaterials").modifyStyle(style -> style.setBold(true).setColor(ChatFormat.DARK_RED)), ChatMessageType.GAME_INFO);

                        shieldSignEntity.setGateState(ShieldSignBlockEntity.GateState.FAILED);
                        boolean broken = ((ShieldServerInteractionManager) player.interactionManager).shield_destroyBlock(pos);
                        if (broken)
                            state.getBlock().afterBreak(world, player, pos, state, entity, ItemStack.EMPTY);
                        return;
                    }
                }

                if (!WarpUtil.setWarpLocation(pos, found1 ? portalPos1 : portalPos2, world.dimension.getType().getRawId(), id)) {
                    player.sendChatMessage(new TranslatableComponent("warp.make.alreadylinked", id).modifyStyle(style -> style.setBold(true).setColor(ChatFormat.DARK_RED)), ChatMessageType.GAME_INFO);

                    shieldSignEntity.setGateState(ShieldSignBlockEntity.GateState.FAILED);
                    boolean broken = ((ShieldServerInteractionManager) player.interactionManager).shield_destroyBlock(pos);
                    if (broken)
                        state.getBlock().afterBreak(world, player, pos, state, entity, ItemStack.EMPTY);
                    return;
                }

                shieldSignEntity.setGateState(ShieldSignBlockEntity.GateState.WORKING);

                if (!player.isCreative()) {
                    for (ItemStack requirement : requirements) {
                        int amountLeft = requirement.getAmount();

                        for (ItemStack stack : stacks) {
                            if (requirement.getItem() == stack.getItem()) {
                                int amount = stack.getAmount();

                                if (amount >= amountLeft) {
                                    stack.setAmount(amount - amountLeft);
                                    amountLeft = 0;
                                } else {
                                    stack.setAmount(0);
                                    amountLeft -= amount;
                                }
                            }

                            if (amountLeft == 0)
                                break;
                        }
                    }
                }
                player.sendChatMessage(new TranslatableComponent("warp.make.success", id).modifyStyle(style -> style.setBold(true).setColor(ChatFormat.GREEN)), ChatMessageType.GAME_INFO);
            }
        }
    }

    private void breakInvalidSign(ServerWorld world, BlockPos pos, BlockState state, BlockEntity entity, ShieldSignBlockEntity shieldSignEntity) {
        player.sendChatMessage(new TranslatableComponent("warp.make.invalidportal").modifyStyle(style -> style.setBold(true).setColor(ChatFormat.DARK_RED)), ChatMessageType.GAME_INFO);

        shieldSignEntity.setGateState(ShieldSignBlockEntity.GateState.FAILED);
        boolean broken = ((ShieldServerInteractionManager) player.interactionManager).shield_destroyBlock(pos);
        if (broken)
            state.getBlock().afterBreak(world, player, pos, state, entity, ItemStack.EMPTY);
    }
}
