package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.entity.ShieldSignBlockEntity;
import com.karelmikie3.shieldserver.util.WarpUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At("HEAD"), method = "onBlockRemoved")
    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState state2, boolean bool, CallbackInfo info) {
        if (state.getBlock() != state2.getBlock() && state.getBlock() instanceof WallSignBlock) {
            System.out.println("sign broken");
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof SignBlockEntity) {


                SignBlockEntity signBlockEntity = (SignBlockEntity) blockEntity;
                ShieldSignBlockEntity shieldSignBlockEntity = (ShieldSignBlockEntity) signBlockEntity;

                if (signBlockEntity.text[1].getText().equals("[warp]") && shieldSignBlockEntity.getGateState() == ShieldSignBlockEntity.GateState.WORKING) {
                    String id = signBlockEntity.text[2].getText();

                    WarpUtil.removeWarpLocation(pos, world.getDimension().getType().getRawId(), id);

                    System.out.println("Removing warp gate!");

                    shieldSignBlockEntity.setGateState(ShieldSignBlockEntity.GateState.REMOVED);
                }
            } else {
                System.err.println("No block entity for broken sign!");
            }
        }
    }
}
