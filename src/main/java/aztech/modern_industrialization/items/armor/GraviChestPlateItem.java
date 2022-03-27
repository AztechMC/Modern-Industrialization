/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.items.armor;

import aztech.modern_industrialization.util.TextHelper;
import io.github.ladysnake.pal.VanillaAbilities;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleBatteryItem;

public class GraviChestPlateItem extends ArmorItem implements Wearable, ActivatableChestItem, SimpleBatteryItem {
    public GraviChestPlateItem(Properties settings) {
        super(buildMaterial(), EquipmentSlot.CHEST, settings.stacksTo(1).rarity(Rarity.EPIC));
    }

    private static ArmorMaterial buildMaterial() {
        return new ArmorMaterial() {
            @Override
            public int getDurabilityForSlot(EquipmentSlot slot) {
                return 0;
            }

            @Override
            public int getDefenseForSlot(EquipmentSlot slot) {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_GENERIC;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null;
            }

            @Override
            public String getName() {
                return "modern_industrialization/gravichestplate";
            }

            @Override
            public float getToughness() {
                return 0;
            }

            @Override
            public float getKnockbackResistance() {
                return 0;
            }
        };
    }

    public long getEnergy(ItemStack stack) {
        return getStoredEnergy(stack);
    }

    public void setEnergy(ItemStack stack, long energy) {
        setStoredEnergy(stack, energy);
    }

    public static final long FLIGHT_COST = 1024;
    public static final long ENERGY_CAPACITY = 1 << 24;

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide())
            return;
        if (entity instanceof Player player && stack == player.getItemBySlot(EquipmentSlot.CHEST)) {
            if (MIArmorEffects.SRC.grants(player, VanillaAbilities.ALLOW_FLYING) && player.getAbilities().flying) {
                setEnergy(stack, Math.max(0, getEnergy(stack) - FLIGHT_COST));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(TextHelper.getEuTextMaxed(getEnergy(stack), ENERGY_CAPACITY, true));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(getEnergy(stack) / (double) ENERGY_CAPACITY * 13);
    }

    @Override
    public long getEnergyCapacity() {
        return ENERGY_CAPACITY;
    }

    @Override
    public long getEnergyMaxInput() {
        return ENERGY_CAPACITY;
    }

    @Override
    public long getEnergyMaxOutput() {
        return 0;
    }
}
