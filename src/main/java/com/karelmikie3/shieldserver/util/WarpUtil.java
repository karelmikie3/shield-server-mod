package com.karelmikie3.shieldserver.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.karelmikie3.shieldserver.ShieldServer.SAVE_DIR;

public class WarpUtil {
    public static final List<ItemStack> requirements;

    static {
        requirements = new ArrayList<>();
        requirements.add(new ItemStack(Items.DIAMOND_BLOCK, 1));
        requirements.add(new ItemStack(Items.ENDER_CHEST, 1));
    }

    private static final File warpData = new File(SAVE_DIR, "data/warp.dat");

    static {
        if (!warpData.exists()) {
            try {
                if (!warpData.createNewFile()) {
                    System.err.println("Unable to create nicks file.");
                } else {
                    NbtIo.writeCompressed(new CompoundTag(), new FileOutputStream(warpData));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Pair<BlockPos, Integer> getWarpTarget(BlockPos signPos, int currentDimension, String id) {
        try {
            CompoundTag warps = NbtIo.readCompressed(new FileInputStream(warpData));
            if (!warps.containsKey(id))
                return null;
            CompoundTag warp = warps.getCompound(id);

            if (warp.getSize() != 4)
                return null;

            int[] currentLocation = {signPos.getX(), signPos.getY(), signPos.getZ(), currentDimension};

            if (Arrays.equals(warp.getIntArray("sign1"), currentLocation)) {
                int[] target = warp.getIntArray("target2");
                BlockPos targetPos = new BlockPos(target[0], target[1], target[2]);
                return new Pair<>(targetPos, target[3]);
            } else if (Arrays.equals(warp.getIntArray("sign2"), currentLocation)) {
                int[] target = warp.getIntArray("target1");
                BlockPos targetPos = new BlockPos(target[0], target[1], target[2]);
                return new Pair<>(targetPos, target[3]);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setWarpLocation(BlockPos signPos, BlockPos targetPos, int currentDimension, String id) {
        boolean result = false;
        try {
            CompoundTag warps = NbtIo.readCompressed(new FileInputStream(warpData));
            CompoundTag warp = warps.containsKey(id) ? warps.getCompound(id) : new CompoundTag();
            if (warp.getSize() == 4)
                return false;

            int[] sign = {signPos.getX(), signPos.getY(), signPos.getZ(), currentDimension};
            int[] target = {targetPos.getX(), targetPos.getY(), targetPos.getZ(), currentDimension};

            if (warp.containsKey("sign1") && !warp.containsKey("sign2")) {
                warp.putIntArray("sign2",  sign);
                warp.putIntArray("target2", target);
                result = true;
            } else if (warp.containsKey("sign2") && !warp.containsKey("sign1")) {
                warp.putIntArray("sign1", sign);
                warp.putIntArray("target1", target);
                result = true;
            } else {
                warp.putIntArray("sign1", sign);
                warp.putIntArray("target1", target);
                result = true;
            }

            warps.put(id, warp);
            NbtIo.writeCompressed(warps, new FileOutputStream(warpData));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void removeWarpLocation(BlockPos signPos, int currentDimension, String id) {
        try {
            CompoundTag warps = NbtIo.readCompressed(new FileInputStream(warpData));
            if (!warps.containsKey(id))
                return;
            CompoundTag warp = warps.getCompound(id);

            int[] currentLocation = {signPos.getX(), signPos.getY(), signPos.getZ(), currentDimension};

            if (Arrays.equals(warp.getIntArray("sign1"), currentLocation)) {
                warp.remove("sign1");
                warp.remove("target1");
            } else if (Arrays.equals(warp.getIntArray("sign2"), currentLocation)) {
                warp.remove("sign2");
                warp.remove("target2");
            }

            warps.put(id, warp);
            NbtIo.writeCompressed(warps, new FileOutputStream(warpData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
