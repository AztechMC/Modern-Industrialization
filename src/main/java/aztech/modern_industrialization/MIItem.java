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
package aztech.modern_industrialization;

import aztech.modern_industrialization.items.GuideBookItem;
import aztech.modern_industrialization.items.SteamDrillItem;
import aztech.modern_industrialization.items.armor.RubberArmorMaterial;
import aztech.modern_industrialization.items.tools.CrowbarItem;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;

public final class MIItem {
    public static SortedMap<String, Item> items = new TreeMap<>();

    public static Item of(String id) {
        return of(Item::new, id, 64);
    }

    public static Item of(String id, int maxCount) {
        return of(Item::new, id, maxCount);
    }

    public static Item of(Function<Item.Settings, Item> ctor, String id, int maxCount) {
        Item item = ctor.apply(new Item.Settings().maxCount(maxCount).group(ModernIndustrialization.ITEM_GROUP));
        if (items.put(id, item) != null) {
            throw new IllegalArgumentException("Item id already taken : " + id);
        }
        return item;
    }

    public static final Item ITEM_CROWBAR = of(CrowbarItem::new, "crowbar", 1);
    public static final Item ITEM_GUIDE_BOOK = of(GuideBookItem::new, "guidebook", 64);
    public static final Item ITEM_STEAM_DRILL = of(SteamDrillItem::new, "steam_mining_drill", 1);
    public static final Item ITEM_UNCOOKED_STEEL_DUST = of("uncooked_steel_dust");

    public static final Item RUBBER_HELMET = of(s -> new ArmorItem(RubberArmorMaterial.INSTANCE, EquipmentSlot.HEAD, s), "rubber_helmet", 1);
    public static final Item RUBBER_BOOTS = of(s -> new ArmorItem(RubberArmorMaterial.INSTANCE, EquipmentSlot.FEET, s), "rubber_boots", 1);

    public static final Item ITEM_LV_MOTOR = of("lv_motor");
    public static final Item ITEM_LV_PISTON = of("lv_piston");
    public static final Item ITEM_LV_CONVEYOR = of("lv_conveyor");
    public static final Item ITEM_LV_ROBOT_ARM = of("lv_robot_arm");
    public static final Item ITEM_LV_CIRCUIT = of("lv_circuit");
    public static final Item ITEM_LV_CIRCUIT_BOARD = of("lv_circuit_board");
    public static final Item ITEM_LV_PUMP = of("lv_pump");
    public static final Item ITEM_RESISTOR = of("resistor");
    public static final Item ITEM_CAPACITOR = of("capacitor");
    public static final Item ITEM_INDUCTOR = of("inductor");
    public static final Item ITEM_WOOD_PULP = of("wood_pulp");
    public static final Item ITEM_RUBBER_SHEET = of("rubber_sheet");
    public static final Item ITEM_INVAR_ROTARY_BLADE = of("invar_rotary_blade");

    public static final Item ITEM_ELECTRONIC_CIRCUIT = of("electronic_circuit");
    public static final Item ITEM_DIODE = of("diode");
    public static final Item ITEM_ELECTRONIC_CIRCUIT_BOARD = of("electronic_circuit_board");
    public static final Item ITEM_TRANSISTOR = of("transistor");
    public static final Item ITEM_LARGE_MOTOR = of("large_motor");

    public static final Item ITEM_LARGE_PUMP = of("large_pump");

    public static final Item ITEM_DIGITAL_CIRCUIT = of("digital_circuit");
    public static final Item ITEM_DIGITAL_CIRCUIT_BOARD = of("digital_circuit_board");
    public static final Item ITEM_OP_AMP = of("op_amp");
    public static final Item ITEM_AND_GATE = of("and_gate");
    public static final Item ITEM_OR_GATE = of("or_gate");
    public static final Item ITEM_NOT_GATE = of("not_gate");

    public static final Item ITEM_PROCESSING_UNIT = of("processing_unit");
    public static final Item ITEM_PROCESSING_UNIT_BOARD = of("processing_unit_board");
    public static final Item ITEM_ARITHMETIC_LOGIC_UNIT = of("arithmetic_logic_unit");
    public static final Item ITEM_RANDOM_ACCESS_MEMORY = of("random_access_memory");
    public static final Item ITEM_MEMORY_MANAGEMENT_UNIT = of("memory_management_unit");

    public static final Item ITEM_MONOCRYSTALLINE_SILICON = of("monocrystalline_silicon");
    public static final Item ITEM_SILICON_WAFER = of("silicon_wafer");

    public static final Item BASIC_UPGRADE = of("basic_upgrade");
    public static final Item ADVANCED_UPGRADE = of("advanced_upgrade");
    public static final Item TURBO_UPGRADE = of("turbo_upgrade");
    public static final Item HIGHLY_ADVANCED_UPGRADE = of("highly_advanced_upgrade");

    public static final Item ADVANCED_MOTOR = of("advanced_motor");
    public static final Item LARGE_ADVANCED_MOTOR = of("large_advanced_motor");
    public static final Item ADVANCED_PUMP = of("advanced_pump");
    public static final Item LARGE_ADVANCED_PUMP = of("large_advanced_pump");

    public static final Item MIXED_INGOT_BLASTPROOF = of("mixed_ingot_blastproof");
    public static final Item MIXED_PLATE_NUCLEAR = of("mixed_plate_nuclear");
    public static final Item AIR_INTAKE = of("air_intake", 1);

    // TO MATERIAL ?
    public static final Item ITEM_POLYETHYLENE_SHEET = of("polyethylene_sheet");
    public static final Item ITEM_POLYVINYL_CHLORIDE_SHEET = of("polyvinyl_chloride_sheet");
}
