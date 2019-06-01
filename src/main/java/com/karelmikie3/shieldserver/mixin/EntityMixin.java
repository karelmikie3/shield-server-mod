package com.karelmikie3.shieldserver.mixin;

import com.karelmikie3.shieldserver.entity.ShieldSignBlockEntity;
import com.karelmikie3.shieldserver.util.WarpUtil;
import net.minecraft.ChatFormat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PortalBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public double x;

    @Shadow
    public double y;

    @Shadow
    public double z;

    @Shadow
    public float yaw;

    @Shadow
    public float pitch;

    @Shadow
    public World world;

    @Shadow
    protected boolean inPortal;

    @Shadow
    protected int portalTime;

    @Shadow
    public int portalCooldown;

    @Shadow
    public DimensionType dimension;

    @Shadow
    private Vec3d velocity;

    @Shadow
    protected Direction field_6028;

    @Shadow
    protected Vec3d field_6020;

    @Shadow
    public boolean removed;

    @Shadow
    public abstract Entity changeDimension(DimensionType type);

    @Shadow
    protected abstract void tickPortalCooldown();

    @Shadow
    public abstract int getMaxPortalTime();

    @Shadow
    public abstract boolean hasVehicle();

    @Shadow
    public abstract int getDefaultPortalCooldown();

    @Shadow
    public abstract void setPositionAndAngles(double x, double y, double z, float yaw, float pitch);

    @Shadow
    public abstract void setPositionAndAngles(BlockPos pos, float yaw, float pitch);

    @Shadow
    public abstract EntityType<?> getType();

    private BlockPos getPortalPos(BlockPos pos) {

        if (this.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL)
            return pos;
        else if (this.world.getBlockState(pos.north()).getBlock() == Blocks.NETHER_PORTAL)
            return pos.north();
        else if (this.world.getBlockState(pos.east()).getBlock() == Blocks.NETHER_PORTAL)
            return pos.east();
        else if (this.world.getBlockState(pos.south()).getBlock() == Blocks.NETHER_PORTAL)
            return pos.south();
        else if (this.world.getBlockState(pos.west()).getBlock() == Blocks.NETHER_PORTAL)
            return pos.west();
        else
            return pos;
    }


    @Overwrite
    public void tickPortal() {
        if (this.world instanceof ServerWorld) {
            int int_1 = this.getMaxPortalTime();
            if (this.inPortal) {
                if (this.world.getServer().isNetherAllowed() && !this.hasVehicle() && this.portalTime++ >= int_1) {
                    this.world.getProfiler().push("portal");
                    this.portalTime = int_1;
                    this.portalCooldown = this.getDefaultPortalCooldown();

                    BlockPos portalPos = getPortalPos(new BlockPos(x, y, z));
                    BlockState portalState = this.world.getBlockState(portalPos);

                    boolean normal = true;

                    double totalTime = System.nanoTime();

                    if (portalState.getBlock() == Blocks.NETHER_PORTAL) {
                        Set<BlockPos> signs = getSigns(portalPos, portalState);

                        System.out.println(signs);

                        for (BlockPos sign : signs) {
                            SignBlockEntity signEntity = (SignBlockEntity) world.getBlockEntity(sign);
                            ShieldSignBlockEntity shieldSignEntity = (ShieldSignBlockEntity) signEntity;

                            if (signEntity != null && signEntity.text[1].getText().equals("[warp]") && shieldSignEntity.getGateState() == ShieldSignBlockEntity.GateState.WORKING) {
                                normal = false;

                                String id = signEntity.text[2].getText();
                                System.out.println("warping: " + id);
                                Pair<BlockPos, Integer> posDimPair = WarpUtil.getWarpTarget(sign, dimension.getRawId(), id);

                                if (posDimPair != null) {
                                    BlockPos targetPos = posDimPair.getLeft();

                                    Integer targetDim = posDimPair.getRight();

                                    ServerWorld targetWorld = world.getServer().getWorld(DimensionType.byRawId(targetDim));
                                    ServerWorld serverWorld = world.getServer().getWorld(world.dimension.getType());

                                    BlockPattern.Result result = ((PortalBlock) Blocks.NETHER_PORTAL).findPortal(serverWorld, targetPos);
                                    BlockPattern.TeleportTarget teleportTarget = result.method_18478(field_6028, targetPos, field_6020.y, velocity, field_6020.x);

                                    float addYaw = 0F;
                                    if (teleportTarget != null) {
                                        System.out.println("using good target!");

                                        targetPos = new BlockPos(teleportTarget.pos);
                                        velocity = teleportTarget.velocity;
                                        addYaw = teleportTarget.yaw;
                                    }

                                    if (ServerPlayerEntity.class.isAssignableFrom(this.getClass())) {
                                        ServerPlayerEntity player = ServerPlayerEntity.class.cast(this);

                                        player.teleport(targetWorld, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, yaw + addYaw, pitch);
                                        player.sendChatMessage(new TranslatableComponent("warp.use.success", id).modifyStyle(style -> style.setBold(true).setColor(ChatFormat.GREEN)), ChatMessageType.GAME_INFO);
                                    } else {

                                        if (world == targetWorld) {
                                            setPositionAndAngles(targetPos, yaw + addYaw, pitch);
                                        } else {
                                            Entity entity_1 = this.getType().create(targetWorld);
                                            if (entity_1 != null) {
                                                entity_1.method_5878(Entity.class.cast(this));
                                                entity_1.setPositionAndAngles(targetPos, entity_1.yaw + addYaw, entity_1.pitch);
                                                entity_1.setVelocity(velocity);
                                                targetWorld.method_18769(entity_1);
                                            }

                                            this.removed = true;
                                        }
                                    }
                                } else {
                                    if (ServerPlayerEntity.class.isAssignableFrom(this.getClass())) {
                                        ServerPlayerEntity player = ServerPlayerEntity.class.cast(this);

                                        player.sendChatMessage(new TranslatableComponent("warp.use.targetnotfound", id).modifyStyle(style -> style.setBold(true).setColor(ChatFormat.DARK_RED)), ChatMessageType.GAME_INFO);
                                    }
                                    System.out.println("No target found!");
                                }

                                break;
                            }

                        }
                    }

                    totalTime = System.nanoTime() - totalTime;
                    System.out.println("totalTime = " + totalTime * 1e-9);

                    if (normal && this.world.dimension.getType() != DimensionType.THE_END)
                        this.changeDimension(this.world.dimension.getType() == DimensionType.THE_NETHER ? DimensionType.OVERWORLD : DimensionType.THE_NETHER);

                    this.world.getProfiler().pop();
                }

                this.inPortal = false;
            } else {
                if (this.portalTime > 0) {
                    this.portalTime -= 4;
                }

                if (this.portalTime < 0) {
                    this.portalTime = 0;
                }
            }

            this.tickPortalCooldown();
        }
    }

    private Set<BlockPos> getSigns(BlockPos portalPos, BlockState portalState) {
        Direction.Axis axis = portalState.get(PortalBlock.AXIS);
        Set<BlockPos> result = new HashSet<>();

        if (axis == Direction.Axis.X) {
            BlockPos lookingPos1 = portalPos;
            boolean found1 = false;

            BlockPos lookingPos2 = portalPos;
            boolean found2 = false;

            do {
                if (!found1)
                    lookingPos1 = lookingPos1.add(1, 0, 0);

                if (!found2)
                    lookingPos2 = lookingPos2.add(-1, 0, 0);

                found1 = this.world.getBlockState(lookingPos1).getBlock() == Blocks.OBSIDIAN;
                found2 = this.world.getBlockState(lookingPos2).getBlock() == Blocks.OBSIDIAN;
            } while (!(found1 && found2) && portalPos.isWithinDistance(lookingPos1, 23) && portalPos.isWithinDistance(lookingPos2, 23));

            if (!(found1 && found2))
                return result;

            result.addAll(findHorizontal(lookingPos1, BlockPos::north, BlockPos::south));
            result.addAll(findHorizontal(lookingPos2, BlockPos::north, BlockPos::south));
        } else if (axis == Direction.Axis.Z) {
            BlockPos lookingPos1 = portalPos;
            boolean found1 = false;

            BlockPos lookingPos2 = portalPos;
            boolean found2 = false;

            do {
                if (!found1)
                    lookingPos1 = lookingPos1.add(0, 0, 1);

                if (!found2)
                    lookingPos2 = lookingPos2.add(0, 0, -1);

                found1 = this.world.getBlockState(lookingPos1).getBlock() == Blocks.OBSIDIAN;
                found2 = this.world.getBlockState(lookingPos2).getBlock() == Blocks.OBSIDIAN;
            } while (!(found1 && found2) && portalPos.isWithinDistance(lookingPos1, 23) && portalPos.isWithinDistance(lookingPos2, 23));

            if (!(found1 && found2))
                return result;

            result.addAll(findHorizontal(lookingPos1, BlockPos::east, BlockPos::west));
            result.addAll(findHorizontal(lookingPos2, BlockPos::east, BlockPos::west));
        }

        return result;
    }

    private Set<BlockPos> findHorizontal(BlockPos origin, Function<BlockPos, BlockPos> dir1, Function<BlockPos, BlockPos> dir2) {
        Set<BlockPos> result = new HashSet<>();

        if (this.world.getBlockState(dir1.apply(origin)).getBlock() instanceof WallSignBlock)
            result.add(dir1.apply(origin));

        if (this.world.getBlockState(dir2.apply(origin)).getBlock() instanceof WallSignBlock)
            result.add(dir2.apply(origin));

        BlockPos lookingPos1 = origin;
        boolean found1 = this.world.getBlockState(lookingPos1).getBlock() != Blocks.OBSIDIAN;;

        BlockPos lookingPos2 = origin;
        boolean found2 = this.world.getBlockState(lookingPos2).getBlock() != Blocks.OBSIDIAN;

        do {
            lookingPos1 = lookingPos1.add(0, 1, 0);
            lookingPos2 = lookingPos2.add(0, -1, 0);

            found1 = look(dir1, dir2, result, lookingPos1, found1);

            found2 = look(dir1, dir2, result, lookingPos2, found2);
        } while (!(found1 && found2));

        return result;
    }

    private boolean look(Function<BlockPos, BlockPos> dir1, Function<BlockPos, BlockPos> dir2, Set<BlockPos> result, BlockPos lookingPos2, boolean found2) {
        if (!found2) {
            found2 = this.world.getBlockState(lookingPos2).getBlock() != Blocks.OBSIDIAN;

            if (this.world.getBlockState(dir1.apply(lookingPos2)).getBlock() instanceof WallSignBlock)
                result.add(dir1.apply(lookingPos2));

            if (this.world.getBlockState(dir2.apply(lookingPos2)).getBlock() instanceof WallSignBlock)
                result.add(dir2.apply(lookingPos2));
        }
        return found2;
    }
}
