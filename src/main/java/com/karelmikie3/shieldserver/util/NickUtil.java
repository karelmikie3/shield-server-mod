package com.karelmikie3.shieldserver.util;

import com.karelmikie3.shieldserver.entity.ShieldPlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static com.karelmikie3.shieldserver.ShieldServer.SAVE_DIR;

public class NickUtil {
    private final ServerPlayerEntity player;
    private final String uuid;
    private final File nickData = new File(SAVE_DIR, "data/nick.dat");
    
    {
        if (!nickData.exists()) {
            try {
                if (!nickData.createNewFile()) {
                    System.err.println("Unable to create nicks file.");
                } else {
                    NbtIo.writeCompressed(new CompoundTag(), new FileOutputStream(nickData));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public NickUtil(ServerPlayerEntity player) {
        this.uuid = player.getUuidAsString();
        this.player = player;
    }
    
    public NickUtil(UUID uuid) {
        this(uuid.toString());
    }

    public NickUtil(String uuid) {
        this.uuid = uuid;
        this.player = null;
    }

    public Component getName() {
        Component name = hasNick() ? getNick() : player.getName();
        name = ((ShieldPlayerEntity) player).shield_addTellClickEvent(name);
        return name;
    }

    public boolean hasNick() {
        try {
            CompoundTag nicks = NbtIo.readCompressed(new FileInputStream(nickData));

            return nicks.containsKey(uuid) && !nicks.getString(uuid).isEmpty();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getNickString() {
        try {
            CompoundTag nicks = NbtIo.readCompressed(new FileInputStream(nickData));

            return nicks.getString(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Component getNick() {
        return new TextComponent(getNickString());
    }

    public void setNick(String nick) {
        try {
            CompoundTag nicks = NbtIo.readCompressed(new FileInputStream(nickData));
            nicks.putString(uuid, nick);
            NbtIo.writeCompressed(nicks, new FileOutputStream(nickData));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
