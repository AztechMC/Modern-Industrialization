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
package aztech.modern_industrialization.compat.kubejs.nuclear;

import aztech.modern_industrialization.nuclear.NuclearConstant;
import dev.latvian.mods.kubejs.event.EventJS;
import java.util.SortedMap;
import java.util.TreeMap;

public class CreateIsotopeFuelParamsEventJS extends EventJS {

    public static SortedMap<String, NuclearConstant.IsotopeFuelParams> FUEL_PARAMS = new TreeMap<>();

    public void createIsotopeFuelParams(String name, double thermalAbsorbProba, double thermalScatterings, int maxTemp, int tempLimitLow,
            int tempLimitHigh,
            double neutronsMultiplication, double directEnergyFactor) {
        FUEL_PARAMS.put(name, new NuclearConstant.IsotopeFuelParams(thermalAbsorbProba, thermalScatterings, maxTemp, tempLimitLow, tempLimitHigh,
                neutronsMultiplication, directEnergyFactor));
    }

    public static NuclearConstant.IsotopeFuelParams getIsotopeFuelParams(String name) {
        if (FUEL_PARAMS.get(name) != null) {
            return FUEL_PARAMS.get(name);
        }

        return switch (name) {
        case "U235" -> NuclearConstant.U235;
        case "U238" -> NuclearConstant.Pu239;
        case "U" -> NuclearConstant.U;
        case "LEU" -> NuclearConstant.LEU;
        case "HEU" -> NuclearConstant.HEU;
        case "LE_MOX" -> NuclearConstant.LE_MOX;
        case "HE_MOX" -> NuclearConstant.HE_MOX;
        default -> throw new IllegalArgumentException("Invalid Isotope Params: " + name);
        };
    }

}
