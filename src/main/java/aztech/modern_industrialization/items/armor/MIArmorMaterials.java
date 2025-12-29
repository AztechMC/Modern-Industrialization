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
import java.util.EnumMap;
import aztech.modern_industrialization.MITags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class MIArmorMaterials {
    public static final ArmorMaterial RUBBER = new ArmorMaterial(
            40,
            Util.make(new EnumMap<>(ArmorType.class), map -> {
                for (var value : ArmorType.values()) {
                    map.put(value, 1);
                }
            }),
            10,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            0,
            0,
            MITags.REPAIRS_RUBBER_ARMOR,
            ResourceKey.create(EquipmentAssets.ROOT_ID, MI.id("rubber")));

    private MIArmorMaterials() {}
}
