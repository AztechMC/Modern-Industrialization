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

import static aztech.modern_industrialization.nuclear.NuclearFuelType.*;

import aztech.modern_industrialization.items.GuideBookItem;
import aztech.modern_industrialization.nuclear.NuclearCoolant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import java.util.SortedMap;
import java.util.TreeMap;
import net.minecraft.item.Item;

public class MIItem extends Item {

    private String id;
    public static SortedMap<String, MIItem> items = new TreeMap<>();

    public MIItem(String id) {
        this(id, 64);
    }

    public MIItem(String id, int maxCount) {
        super(new Item.Settings().maxCount(maxCount).group(ModernIndustrialization.ITEM_GROUP));
        if (items.containsKey(id)) {
            throw new IllegalArgumentException("Item id already taken : " + id);
        } else {
            this.id = id;
            items.put(id, this);
        }
    }

    public String getId() {
        return id;
    }

    public static final MIItem ITEM_GUIDE_BOOK = new GuideBookItem("guidebook");

    public static final MIItem ITEM_BRICK_TINY_DUST = new MIItem("brick_tiny_dust");
    public static final MIItem ITEM_BRICK_DUST = new MIItem("brick_dust");
    public static final MIItem ITEM_FIRE_CLAY_DUST = new MIItem("fire_clay_dust");
    public static final MIItem ITEM_FIRE_CLAY_BRICK = new MIItem("fire_clay_brick");
    public static final MIItem ITEM_COKE = new MIItem("coke");
    public static final MIItem ITEM_COKE_DUST = new MIItem("coke_dust");
    public static final MIItem ITEM_UNCOOKED_STEEL_DUST = new MIItem("uncooked_steel_dust");

    public static final MIItem ITEM_LV_MOTOR = new MIItem("lv_motor");
    public static final MIItem ITEM_LV_PISTON = new MIItem("lv_piston");
    public static final MIItem ITEM_LV_CONVEYOR = new MIItem("lv_conveyor");
    public static final MIItem ITEM_LV_ROBOT_ARM = new MIItem("lv_robot_arm");
    public static final MIItem ITEM_LV_CIRCUIT = new MIItem("lv_circuit");
    public static final MIItem ITEM_LV_CIRCUIT_BOARD = new MIItem("lv_circuit_board");
    public static final MIItem ITEM_LV_BATTERY = new MIItem("lv_battery");
    public static final MIItem ITEM_LV_PUMP = new MIItem("lv_pump");
    public static final MIItem ITEM_RESISTOR = new MIItem("resistor");
    public static final MIItem ITEM_CAPACITOR = new MIItem("capacitor");
    public static final MIItem ITEM_INDUCTOR = new MIItem("inductor");
    public static final MIItem ITEM_STEEL_ROD_MAGNETIC = new MIItem("steel_rod_magnetic");
    public static final MIItem ITEM_WOOD_PULP = new MIItem("wood_pulp");
    public static final MIItem ITEM_RUBBER_SHEET = new MIItem("rubber_sheet");
    public static final MIItem ITEM_INVAR_ROTARY_BLADE = new MIItem("invar_rotary_blade");

    public static final MIItem ITEM_ELECTRONIC_CIRCUIT = new MIItem("electronic_circuit");
    public static final MIItem ITEM_DIODE = new MIItem("diode");
    public static final MIItem ITEM_ELECTRONIC_CIRCUIT_BOARD = new MIItem("electronic_circuit_board");
    public static final MIItem ITEM_TRANSISTOR = new MIItem("transistor");
    public static final MIItem ITEM_SILICON_BATTERY = new MIItem("silicon_battery");
    public static final MIItem ITEM_LARGE_MOTOR = new MIItem("large_motor");
    public static final MIItem ITEM_LARGE_PUMP = new MIItem("large_pump");

    public static final MIItem ITEM_DIGITAL_CIRCUIT = new MIItem("digital_circuit");
    public static final MIItem ITEM_DIGITAL_CIRCUIT_BOARD = new MIItem("digital_circuit_board");
    public static final MIItem ITEM_OP_AMP = new MIItem("op_amp");
    public static final MIItem ITEM_AND_GATE = new MIItem("and_gate");
    public static final MIItem ITEM_OR_GATE = new MIItem("or_gate");
    public static final MIItem ITEM_NOT_GATE = new MIItem("not_gate");

    public static final MIItem ITEM_P_DOPED_SILICON_PLATE = new MIItem("p_doped_silicon_plate");
    public static final MIItem ITEM_N_DOPED_SILICON_PLATE = new MIItem("n_doped_silicon_plate");

    public static final MIItem ITEM_CARBON_DUST = new MIItem("carbon_dust");
    public static final MIItem ITEM_SODIUM_BATTERY = new MIItem("sodium_battery");

    public static final MIItem ITEM_RUBY_DUST = new MIItem("ruby_dust");
    public static final MIItem ITEM_STAINLESS_STEEL_HOT_INGOT = new MIItem("stainless_steel_hot_ingot");
    public static final MIItem ITEM_CHROME_HOT_INGOT = new MIItem("chrome_hot_ingot");

    public static final MIItem ITEM_DEPLETED_URANIUM_FUEL_ROD = new MIItem("depleted_uranium_fuel_rod");
    public static final MIItem ITEM_DEPLETED_URANIUM_FUEL_ROD_DOUBLE = new MIItem("depleted_uranium_fuel_rod_double");
    public static final MIItem ITEM_DEPLETED_URANIUM_FUEL_ROD_QUAD = new MIItem("depleted_uranium_fuel_rod_quad");
    public static final MIItem ITEM_URANIUM_FUEL_ROD = new NuclearFuel("uranium_fuel_rod", URANIUM, 1, ITEM_DEPLETED_URANIUM_FUEL_ROD);
    public static final MIItem ITEM_URANIUM_FUEL_ROD_DOUBLE = new NuclearFuel("uranium_fuel_rod_double", URANIUM, 2,
            ITEM_DEPLETED_URANIUM_FUEL_ROD_DOUBLE);
    public static final MIItem ITEM_URANIUM_FUEL_ROD_QUAD = new NuclearFuel("uranium_fuel_rod_quad", URANIUM, 4, ITEM_DEPLETED_URANIUM_FUEL_ROD_QUAD);

    public static final MIItem ITEM_DEPLETED_PLUTONIUM_FUEL_ROD = new MIItem("depleted_plutonium_fuel_rod");
    public static final MIItem ITEM_DEPLETED_PLUTONIUM_FUEL_ROD_DOUBLE = new MIItem("depleted_plutonium_fuel_rod_double");
    public static final MIItem ITEM_DEPLETED_PLUTONIUM_FUEL_ROD_QUAD = new MIItem("depleted_plutonium_fuel_rod_quad");
    public static final MIItem ITEM_PLUTONIUM_FUEL_ROD = new NuclearFuel("plutonium_fuel_rod", PLUTONIUM, 1, ITEM_DEPLETED_PLUTONIUM_FUEL_ROD);
    public static final MIItem ITEM_PLUTONIUM_FUEL_ROD_DOUBLE = new NuclearFuel("plutonium_fuel_rod_double", PLUTONIUM, 2,
            ITEM_DEPLETED_PLUTONIUM_FUEL_ROD_DOUBLE);
    public static final MIItem ITEM_PLUTONIUM_FUEL_ROD_QUAD = new NuclearFuel("plutonium_fuel_rod_quad", PLUTONIUM, 4,
            ITEM_DEPLETED_PLUTONIUM_FUEL_ROD_QUAD);

    public static final MIItem ITEM_DEPLETED_MOX_FUEL_ROD = new MIItem("depleted_mox_fuel_rod");
    public static final MIItem ITEM_DEPLETED_MOX_FUEL_ROD_DOUBLE = new MIItem("depleted_mox_fuel_rod_double");
    public static final MIItem ITEM_DEPLETED_MOX_FUEL_ROD_QUAD = new MIItem("depleted_mox_fuel_rod_quad");
    public static final MIItem ITEM_MOX_FUEL_ROD = new NuclearFuel("mox_fuel_rod", MOX, 1, ITEM_DEPLETED_MOX_FUEL_ROD);
    public static final MIItem ITEM_MOX_FUEL_ROD_DOUBLE = new NuclearFuel("mox_fuel_rod_double", MOX, 2, ITEM_DEPLETED_MOX_FUEL_ROD_DOUBLE);
    public static final MIItem ITEM_MOX_FUEL_ROD_QUAD = new NuclearFuel("mox_fuel_rod_quad", MOX, 4, ITEM_DEPLETED_MOX_FUEL_ROD_QUAD);

    public static final MIItem ITEM_SIMPLE_FLUID_COOLANT = new NuclearCoolant("simple_fluid_coolant", 25000, 1);

    public static final MIItem ITEM_POLYETHYLENE_SHEET = new MIItem("polyethylene_sheet");
    public static final MIItem ITEM_POLYVINYL_CHLORIDE_SHEET = new MIItem("polyvinyl_chloride_sheet");
}
