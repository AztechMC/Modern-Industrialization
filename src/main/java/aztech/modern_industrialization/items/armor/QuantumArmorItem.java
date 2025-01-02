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
import aztech.modern_industrialization.MIRegistries;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.NeoForgeMod;

public class QuantumArmorItem extends ArmorItem {
    public QuantumArmorItem(ArmorItem.Type type, Properties settings) {
        super(MIArmorMaterials.QUANTUM, type, settings.stacksTo(1).attributes(buildModifiers(type)));
    }

    private static ItemAttributeModifiers buildModifiers(ArmorItem.Type type) {
        var builder = ItemAttributeModifiers.builder()
                .add(
                        MIRegistries.QUANTUM_ARMOR,
                        // This needs a unique name for each armor piece or else the value of one piece will override the others
                        new AttributeModifier(MI.id("quantum_armor_%s".formatted(type.getName())), 1, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.bySlot(type.getSlot()));
        if (type == Type.CHESTPLATE) {
            builder.add(
                    NeoForgeMod.CREATIVE_FLIGHT,
                    new AttributeModifier(MI.id("quantum_flight"), 1, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.CHEST);
        }
        return builder.build();
    }
}
