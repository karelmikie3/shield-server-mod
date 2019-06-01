package com.karelmikie3.shieldserver.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.PortalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    @Shadow
    public abstract int getTickRate(ViewableWorld world);

    @Overwrite
    public void onBlockAdded(BlockState blockState_1, World world_1, BlockPos blockPos_1, BlockState blockState_2, boolean boolean_1) {
        if (blockState_2.getBlock() != blockState_1.getBlock()) {
            if (!((PortalBlock) Blocks.NETHER_PORTAL).createPortalAt(world_1, blockPos_1)) {
                if (!blockState_1.canPlaceAt(world_1, blockPos_1)) {
                    world_1.clearBlockState(blockPos_1, false);
                } else {
                    world_1.getBlockTickScheduler().schedule(blockPos_1, FireBlock.class.cast(this), this.getTickRate(world_1) + world_1.random.nextInt(10));
                }
            }
        }
    }
}
