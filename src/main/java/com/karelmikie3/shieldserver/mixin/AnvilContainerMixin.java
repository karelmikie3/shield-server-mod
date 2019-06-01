package com.karelmikie3.shieldserver.mixin;

import net.minecraft.container.AnvilContainer;
import net.minecraft.container.Container;
import net.minecraft.container.Property;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;

import static net.minecraft.container.AnvilContainer.getNextCost;

@Mixin(AnvilContainer.class)
public class AnvilContainerMixin {

    @Shadow
    private Inventory result;

    @Shadow
    private Inventory inventory;

    @Shadow
    private Property levelCost;

    @Shadow
    private int field_7776;

    @Shadow
    private String newItemName;

    @Shadow
    private PlayerEntity player;

    @Overwrite
    public void updateResult() {
        ItemStack itemStack_1 = this.inventory.getInvStack(0);
        this.levelCost.set(1);
        int int_1 = 0;
        int int_2 = 0;
        int int_3 = 0;
        if (itemStack_1.isEmpty()) {
            this.result.setInvStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
        } else {
            ItemStack itemStack_2 = itemStack_1.copy();
            ItemStack itemStack_3 = this.inventory.getInvStack(1);
            Map<Enchantment, Integer> map_1 = EnchantmentHelper.getEnchantments(itemStack_2);
            int_2 = int_2 + itemStack_1.getRepairCost() + (itemStack_3.isEmpty() ? 0 : itemStack_3.getRepairCost());
            this.field_7776 = 0;
            if (!itemStack_3.isEmpty()) {
                boolean boolean_1 = itemStack_3.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantmentTag(itemStack_3).isEmpty();
                int int_7;
                int int_8;
                int int_9;
                if (itemStack_2.hasDurability() && itemStack_2.getItem().canRepair(itemStack_1, itemStack_3)) {
                    int_7 = Math.min(itemStack_2.getDamage(), itemStack_2.getDurability() / 4);
                    if (int_7 <= 0) {
                        this.result.setInvStack(0, ItemStack.EMPTY);
                        this.levelCost.set(0);
                        return;
                    }

                    for(int_8 = 0; int_7 > 0 && int_8 < itemStack_3.getAmount(); ++int_8) {
                        int_9 = itemStack_2.getDamage() - int_7;
                        itemStack_2.setDamage(int_9);
                        ++int_1;
                        int_7 = Math.min(itemStack_2.getDamage(), itemStack_2.getDurability() / 4);
                    }

                    this.field_7776 = int_8;
                } else {
                    if (!boolean_1 && (itemStack_2.getItem() != itemStack_3.getItem() || !itemStack_2.hasDurability())) {
                        this.result.setInvStack(0, ItemStack.EMPTY);
                        this.levelCost.set(0);
                        return;
                    }

                    if (itemStack_2.hasDurability() && !boolean_1) {
                        int_7 = itemStack_1.getDurability() - itemStack_1.getDamage();
                        int_8 = itemStack_3.getDurability() - itemStack_3.getDamage();
                        int_9 = int_8 + itemStack_2.getDurability() * 12 / 100;
                        int int_10 = int_7 + int_9;
                        int int_11 = itemStack_2.getDurability() - int_10;
                        if (int_11 < 0) {
                            int_11 = 0;
                        }

                        if (int_11 < itemStack_2.getDamage()) {
                            itemStack_2.setDamage(int_11);
                            int_1 += 2;
                        }
                    }

                    Map<Enchantment, Integer> map_2 = EnchantmentHelper.getEnchantments(itemStack_3);
                    boolean boolean_2 = false;
                    boolean boolean_3 = false;
                    Iterator var24 = map_2.keySet().iterator();

                    label160:
                    while(true) {
                        Enchantment enchantment_1;
                        do {
                            if (!var24.hasNext()) {
                                if (boolean_3 && !boolean_2) {
                                    this.result.setInvStack(0, ItemStack.EMPTY);
                                    this.levelCost.set(0);
                                    return;
                                }
                                break label160;
                            }

                            enchantment_1 = (Enchantment)var24.next();
                        } while(enchantment_1 == null);

                        int int_12 = map_1.containsKey(enchantment_1) ? (Integer)map_1.get(enchantment_1) : 0;
                        int int_13 = map_2.get(enchantment_1);
                        int_13 = int_12 == int_13 ? int_13 + 1 : Math.max(int_13, int_12);
                        boolean boolean_4 = enchantment_1.isAcceptableItem(itemStack_1);
                        if (this.player.abilities.creativeMode || itemStack_1.getItem() == Items.ENCHANTED_BOOK) {
                            boolean_4 = true;
                        }

                        Iterator var17 = map_1.keySet().iterator();

                        while(var17.hasNext()) {
                            Enchantment enchantment_2 = (Enchantment)var17.next();
                            if (enchantment_2 != enchantment_1 && !enchantment_1.isDifferent(enchantment_2)) {
                                boolean_4 = false;
                                ++int_1;
                            }
                        }

                        if (!boolean_4) {
                            boolean_3 = true;
                        } else {
                            boolean_2 = true;
                            if (int_13 > enchantment_1.getMaximumLevel()) {
                                int_13 = enchantment_1.getMaximumLevel();
                            }

                            map_1.put(enchantment_1, int_13);
                            int int_14 = 0;
                            switch(enchantment_1.getWeight()) {
                                case COMMON:
                                    int_14 = 1;
                                    break;
                                case UNCOMMON:
                                    int_14 = 2;
                                    break;
                                case RARE:
                                    int_14 = 4;
                                    break;
                                case VERY_RARE:
                                    int_14 = 8;
                            }

                            if (boolean_1) {
                                int_14 = Math.max(1, int_14 / 2);
                            }

                            int_1 += int_14 * int_13;
                            if (itemStack_1.getAmount() > 1) {
                                int_1 = 40;
                            }
                        }
                    }
                }
            }

            if (StringUtils.isBlank(this.newItemName)) {
                if (itemStack_1.hasDisplayName()) {
                    int_3 = 1;
                    int_1 += int_3;
                    itemStack_2.removeDisplayName();
                }
            } else if (!this.newItemName.equals(itemStack_1.getDisplayName().getString())) {
                int_3 = 1;
                int_1 += int_3;
                itemStack_2.setDisplayName(new TextComponent(this.newItemName));
            }

            this.levelCost.set(int_2 + int_1);
            if (int_1 <= 0) {
                itemStack_2 = ItemStack.EMPTY;
            }

            if (int_3 == int_1 && int_3 > 0 && this.levelCost.get() >= 40) {
                this.levelCost.set(39);
            }

            if (!itemStack_2.isEmpty()) {
                int int_15 = itemStack_2.getRepairCost();
                if (!itemStack_3.isEmpty() && int_15 < itemStack_3.getRepairCost()) {
                    int_15 = itemStack_3.getRepairCost();
                }

                if (int_3 != int_1 || int_3 == 0) {
                    int_15 = getNextCost(int_15);
                }

                itemStack_2.setRepairCost(int_15);
                EnchantmentHelper.set(map_1, itemStack_2);
            }

            this.result.setInvStack(0, itemStack_2);
            Container.class.cast(this).sendContentUpdates();

            if (player instanceof ServerPlayerEntity)
                ((ServerPlayerEntity) player).sendChatMessage(new TextComponent("cost: " + this.levelCost.get()), ChatMessageType.CHAT);
        }
    }
}
