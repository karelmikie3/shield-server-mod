package com.karelmikie3.shieldserver.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SharedConstants.class)
public class SharedConstantsMixin {
    @Overwrite
    public static boolean isValidChar(char c) {
        return c >= ' ' && c != 127;
    }
}
