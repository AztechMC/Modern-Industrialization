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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIComponents;
import aztech.modern_industrialization.items.ActivatableItem;
import aztech.modern_industrialization.items.MIEnergyItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jspecify.annotations.Nullable;

public class GraviChestPlateItem extends Item implements ActivatableItem, MIEnergyItem {
    public GraviChestPlateItem(Properties properties) {
        super(properties
                // TODO 1.21.11 - do I need to set the assetId?
                .equippable(EquipmentSlot.CHEST)
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .component(MIComponents.ACTIVATED.get(), false));
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (this.getEnergy(stack) > 0 && this.isActivated(stack)) {
            return ItemAttributeModifiers.builder()
                    .add(
                            NeoForgeMod.CREATIVE_FLIGHT,
                            new AttributeModifier(MI.id("gravichestplate_flight"), 1, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                    .build();
        }
        return ItemAttributeModifiers.EMPTY;
    }

    public static final long FLIGHT_COST = 1024;
    public static final long ENERGY_CAPACITY = 1 << 24;

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof Player player && slot == EquipmentSlot.CHEST) {
            if (player.getAbilities().flying) {
                setEnergy(stack, Math.max(0, getEnergy(stack) - FLIGHT_COST));
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(getEnergy(stack) / (double) ENERGY_CAPACITY * 13);
    }
}
