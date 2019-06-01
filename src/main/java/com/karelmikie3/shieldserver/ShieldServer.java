package com.karelmikie3.shieldserver;

import com.karelmikie3.shieldserver.command.MeCommand;
import com.karelmikie3.shieldserver.command.NickCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.server.world.ServerWorld;

import java.io.File;

public class ShieldServer implements ModInitializer {
	public static File SAVE_DIR;

	@Override
	public void onInitialize() {
		System.out.println("Started weird shield server mod.");

		WorldTickCallback.EVENT.register(listener -> {
			if (listener instanceof ServerWorld && SAVE_DIR == null) {
				SAVE_DIR = ((ServerWorld) listener).getSaveHandler().getWorldDir();
			}
		});
		CommandRegistry.INSTANCE.register(false, NickCommand::register);
		CommandRegistry.INSTANCE.register(false, MeCommand::register);
	}
}
