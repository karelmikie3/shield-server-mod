package com.karelmikie3.shieldserver.mixin;

import com.google.gson.JsonParseException;
import com.karelmikie3.shieldserver.util.TextUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract CompoundTag getSubCompoundTag(String string_1);

    @Shadow
    public abstract Item getItem();

    @Overwrite
    public Component getDisplayName() {
        CompoundTag compoundTag_1 = this.getSubCompoundTag("display");
        if (compoundTag_1 != null && compoundTag_1.containsKey("Name", 8)) {
            try {
                Component component_1 = Component.Serializer.fromJsonString(compoundTag_1.getString("Name"));
                if (component_1 != null) {
                    return TextUtil.changeToColored(component_1);
                }

                compoundTag_1.remove("Name");
            } catch (JsonParseException var3) {
                compoundTag_1.remove("Name");
            }
        }

        return this.getItem().getTranslatedNameTrimmed(ItemStack.class.cast(this));
    }
}
