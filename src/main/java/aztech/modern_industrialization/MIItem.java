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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import net.minecraft.item.Item;

public final class MIItem {
    public static SortedMap<String, Item> items = new TreeMap<>();

    public static Item of(String id) {
        return of(Item::new, id, 64);
    }

    public static Item of(Function<Item.Settings, Item> ctor, String id, int maxCount) {
        Item item = ctor.apply(new Item.Settings().maxCount(maxCount).group(ModernIndustrialization.ITEM_GROUP));
        if (items.put(id, item) != null) {
            throw new IllegalArgumentException("Item id already taken : " + id);
        }
        return item;
    }

    public static final Item ITEM_GUIDE_BOOK = of(GuideBookItem::new, "guidebook", 1);

    public static final Item FIRE_CLAY_BRICK = of("fire_clay_brick");

    public static final Item ITEM_UNCOOKED_STEEL_DUST = of("uncooked_steel_dust");

    public static final Item ITEM_LV_MOTOR = of("lv_motor");
    public static final Item ITEM_LV_PISTON = of("lv_piston");
    public static final Item ITEM_LV_CONVEYOR = of("lv_conveyor");
    public static final Item ITEM_LV_ROBOT_ARM = of("lv_robot_arm");
    public static final Item ITEM_LV_CIRCUIT = of("lv_circuit");
    public static final Item ITEM_LV_CIRCUIT_BOARD = of("lv_circuit_board");
    public static final Item ITEM_LV_BATTERY = of("lv_battery");
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
    public static final Item ITEM_SILICON_BATTERY = of("silicon_battery");
    public static final Item ITEM_LARGE_MOTOR = of("large_motor");

    public static final Item ITEM_LARGE_PUMP = of("large_pump");

    public static final Item ITEM_DIGITAL_CIRCUIT = of("digital_circuit");
    public static final Item ITEM_DIGITAL_CIRCUIT_BOARD = of("digital_circuit_board");
    public static final Item ITEM_OP_AMP = of("op_amp");
    public static final Item ITEM_AND_GATE = of("and_gate");
    public static final Item ITEM_OR_GATE = of("or_gate");
    public static final Item ITEM_NOT_GATE = of("not_gate");


    public static final Item ITEM_SODIUM_BATTERY = of("sodium_battery");

    /*
     * FIXME public static final Item ITEM_DEPLETED_URANIUM_FUEL_ROD = new
     * MIItem("depleted_uranium_fuel_rod"); public static final Item
     * ITEM_DEPLETED_URANIUM_FUEL_ROD_DOUBLE = new
     * MIItem("depleted_uranium_fuel_rod_double"); public static final Item
     * ITEM_DEPLETED_URANIUM_FUEL_ROD_QUAD = new
     * MIItem("depleted_uranium_fuel_rod_quad"); public static final Item
     * ITEM_URANIUM_FUEL_ROD = new NuclearFuel("uranium_fuel_rod", URANIUM, 1,
     * ITEM_DEPLETED_URANIUM_FUEL_ROD); public static final Item
     * ITEM_URANIUM_FUEL_ROD_DOUBLE = new NuclearFuel("uranium_fuel_rod_double",
     * URANIUM, 2, ITEM_DEPLETED_URANIUM_FUEL_ROD_DOUBLE); public static final Item
     * ITEM_URANIUM_FUEL_ROD_QUAD = new NuclearFuel("uranium_fuel_rod_quad",
     * URANIUM, 4, ITEM_DEPLETED_URANIUM_FUEL_ROD_QUAD);
     * 
     * public static final Item ITEM_CROWBAR = new MIItem("crowbar");
     * 
     * public static final Item ITEM_DEPLETED_PLUTONIUM_FUEL_ROD = new
     * MIItem("depleted_plutonium_fuel_rod"); public static final Item
     * ITEM_DEPLETED_PLUTONIUM_FUEL_ROD_DOUBLE = new
     * MIItem("depleted_plutonium_fuel_rod_double"); public static final Item
     * ITEM_DEPLETED_PLUTONIUM_FUEL_ROD_QUAD = new
     * MIItem("depleted_plutonium_fuel_rod_quad"); public static final Item
     * ITEM_PLUTONIUM_FUEL_ROD = new NuclearFuel("plutonium_fuel_rod", PLUTONIUM, 1,
     * ITEM_DEPLETED_PLUTONIUM_FUEL_ROD); public static final Item
     * ITEM_PLUTONIUM_FUEL_ROD_DOUBLE = new NuclearFuel("plutonium_fuel_rod_double",
     * PLUTONIUM, 2, ITEM_DEPLETED_PLUTONIUM_FUEL_ROD_DOUBLE); public static final
     * Item ITEM_PLUTONIUM_FUEL_ROD_QUAD = new
     * NuclearFuel("plutonium_fuel_rod_quad", PLUTONIUM, 4,
     * ITEM_DEPLETED_PLUTONIUM_FUEL_ROD_QUAD);
     * 
     * public static final Item ITEM_DEPLETED_MOX_FUEL_ROD = new
     * MIItem("depleted_mox_fuel_rod"); public static final Item
     * ITEM_DEPLETED_MOX_FUEL_ROD_DOUBLE = new
     * MIItem("depleted_mox_fuel_rod_double"); public static final Item
     * ITEM_DEPLETED_MOX_FUEL_ROD_QUAD = new MIItem("depleted_mox_fuel_rod_quad");
     * public static final Item ITEM_MOX_FUEL_ROD = new NuclearFuel("mox_fuel_rod",
     * MOX, 1, ITEM_DEPLETED_MOX_FUEL_ROD); public static final Item
     * ITEM_MOX_FUEL_ROD_DOUBLE = new NuclearFuel("mox_fuel_rod_double", MOX, 2,
     * ITEM_DEPLETED_MOX_FUEL_ROD_DOUBLE); public static final Item
     * ITEM_MOX_FUEL_ROD_QUAD = new NuclearFuel("mox_fuel_rod_quad", MOX, 4,
     * ITEM_DEPLETED_MOX_FUEL_ROD_QUAD);
     * 
     * public static final Item ITEM_SIMPLE_FLUID_COOLANT = new
     * NuclearCoolant("simple_fluid_coolant", 25000, 1);
     */

    // TO MATERIAL ?
    public static final Item ITEM_POLYETHYLENE_SHEET = of("polyethylene_sheet");
    public static final Item ITEM_POLYVINYL_CHLORIDE_SHEET = of("polyvinyl_chloride_sheet");
}
