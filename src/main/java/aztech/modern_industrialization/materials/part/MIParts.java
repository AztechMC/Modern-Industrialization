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
package aztech.modern_industrialization.materials.part;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MIParts {

    public static final String BATTERY = "battery";
    public static final String BLADE = "blade";
    public static final String BLOCK = "block";
    public static final String BOLT = "bolt";
    public static final String CABLE = "cable";
    public static final String COIL = "coil";
    public static final String CRUSHED_DUST = "crushed_dust";
    public static final String CURVED_PLATE = "curved_plate";
    public static final String DOUBLE_INGOT = "double_ingot";
    public static final String DRILL_HEAD = "drill_head";
    public static final String DRILL = "drill";
    public static final String DUST = "dust";
    public static final String FINE_WIRE = "fine_wire";
    public static final String GEAR = "gear";
    public static final String GEM = "gem";
    public static final String HOT_INGOT = "hot_ingot";
    public static final String INGOT = "ingot";
    public static final String LARGE_PLATE = "large_plate";
    public static final String MACHINE_CASING = "machine_casing";
    public static final String MACHINE_CASING_PIPE = "machine_casing_pipe";
    public static final String MACHINE_CASING_SPECIAL = "machine_casing_special";
    public static final String NUGGET = "nugget";
    public static final String ORE = "ore";
    public static final String ORE_DEEPLSATE = "ore_deepslate";
    public static final String PLATE = "plate";
    public static final String RAW_METAL = "raw_metal";
    public static final String RAW_METAL_BLOCK = "raw_metal_block";
    public static final String RING = "ring";
    public static final String ROD = "rod";
    public static final String ROD_MAGNETIC = "rod_magnetic";
    public static final String ROTOR = "rotor";
    public static final String TANK = "tank";
    public static final String TINY_DUST = "tiny_dust";
    public static final String WIRE = "wire";
    public static final String WIRE_MAGNETIC = "wire_magnetic";
    public static final String FUEL_ROD = "fuel_rod";
    public static final String FUEL_ROD_DOUBLE = "fuel_rod_double";
    public static final String FUEL_ROD_QUAD = "fuel_rod_quad";

    public static final String N_DOPED_PLATE = "n_doped_plate";
    public static final String P_DOPED_PLATE = "p_doped_plate";

    public static final String[] ITEM_PURE_NON_METAL = new String[] { TINY_DUST, DUST, CRUSHED_DUST };
    public static final String[] ITEM_PURE_METAL = new String[] { INGOT, NUGGET, TINY_DUST, DUST };

    public static final List<String> TAGGED_PARTS_LIST = Arrays.asList(BLOCK, DUST, GEAR, INGOT, NUGGET, ORE, PLATE, TINY_DUST);
    public static final Set<String> TAGGED_PARTS = new HashSet<>(TAGGED_PARTS_LIST);

    public static final String[] BLOCKS = { ORE, BLOCK, COIL, MACHINE_CASING, MACHINE_CASING_SPECIAL, MACHINE_CASING_PIPE };

    public static String idOfPart(String part, String materialName) {
        return "modern_industrialization:" + materialName + "_" + part;
    }

}
