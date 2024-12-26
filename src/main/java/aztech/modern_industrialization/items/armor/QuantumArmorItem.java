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
import aztech.modern_industrialization.MIText;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.NeoForgeMod;

public class QuantumArmorItem extends ArmorItem {
    public static final List<QuantumArmorItem> ITEMS = new ArrayList<>();

    public QuantumArmorItem(ArmorItem.Type type, Properties settings) {
        super(MIArmorMaterials.QUANTUM, type, settings.stacksTo(1));
        ITEMS.add(this);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        if (type == Type.CHESTPLATE) {
            return ItemAttributeModifiers.builder()
                    .add(
                            NeoForgeMod.CREATIVE_FLIGHT,
                            new AttributeModifier(MI.id("quantum_flight"), 1, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                    .build()
                    .withTooltip(false);
        } else {
            return ItemAttributeModifiers.EMPTY;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(Component.empty());
        list.add(Component.translatable("item.modifiers." + getType().getSlot().getName()).withStyle(ChatFormatting.GRAY));
        String oneQuarterInfinity = " \u00B9\u2044\u2084 |\u221E> + \u00B3\u2044\u2084 |0>";
        list.add(Component.translatable("attribute.modifier.plus.0", oneQuarterInfinity, Component.translatable("attribute.name.generic.armor"))
                .withStyle(ChatFormatting.BLUE));
        if (type == Type.CHESTPLATE) {
            list.add(MIText.AllowCreativeFlight.text().withStyle(ChatFormatting.BLUE));
        }
    }
}
