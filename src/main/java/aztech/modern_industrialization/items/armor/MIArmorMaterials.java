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
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MIArmorMaterials {
    private static final DeferredRegister<ArmorMaterial> DR = DeferredRegister.create(Registries.ARMOR_MATERIAL, MI.ID);

    public static final Holder<ArmorMaterial> DIESEL_JETPACK = DR.register("diesel_jetpack", location -> new ArmorMaterial(
            Map.of(),
            0,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            () -> {
                throw new UnsupportedOperationException("Cannot repair Diesel Jetpack");
            },
            List.of(new ArmorMaterial.Layer(location)),
            0,
            0));
    public static final Holder<ArmorMaterial> GRAVICHESTPLATE = DR.register("gravichestplate", location -> new ArmorMaterial(
            Map.of(),
            0,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            () -> {
                throw new UnsupportedOperationException("Cannot repair GraviChestPlate");
            },
            List.of(new ArmorMaterial.Layer(location)),
            0,
            0));
    public static final Holder<ArmorMaterial> RUBBER = DR.register("rubber", location -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                for (var value : ArmorItem.Type.values()) {
                    map.put(value, 1);
                }
            }),
            10,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            () -> Ingredient.of(BuiltInRegistries.ITEM.get(MI.id("rubber_sheet"))),
            List.of(new ArmorMaterial.Layer(location)),
            0,
            0));
    public static final Holder<ArmorMaterial> QUANTUM = DR.register("quantum", location -> new ArmorMaterial(
            Map.of(),
            0,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            () -> {
                throw new UnsupportedOperationException("Cannot repair quantum armor");
            },
            List.of(new ArmorMaterial.Layer(location)),
            0,
            0));

    public static void init(IEventBus modEventBus) {
        DR.register(modEventBus);
    }

    private MIArmorMaterials() {}
}
