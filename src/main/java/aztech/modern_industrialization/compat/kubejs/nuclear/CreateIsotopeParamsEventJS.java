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

public class CreateIsotopeParamsEventJS extends EventJS {

    public static SortedMap<String, NuclearConstant.IsotopeParams> ISOTOPE_PARAMS = new TreeMap<>();

    public void createIsotopeParams(String name, double thermalAbsorbProba, double fastAbsorptionProba, double thermalScatteringProba,
            double fastScatteringProba) {
        ISOTOPE_PARAMS.put(name, new NuclearConstant.IsotopeParams(thermalAbsorbProba, fastAbsorptionProba,
                thermalScatteringProba, fastScatteringProba));
    }

    public static NuclearConstant.IsotopeParams getIsotopeParams(String name) {

        if (ISOTOPE_PARAMS.get(name) != null) {
            return ISOTOPE_PARAMS.get(name);
        }

        return switch (name) {
        case "hydrogen" -> NuclearConstant.HYDROGEN;
        case "deuterium" -> NuclearConstant.DEUTERIUM;
        case "cadmium" -> NuclearConstant.CADMIUM;
        case "carbon" -> NuclearConstant.CARBON;
        case "invar" -> NuclearConstant.INVAR;
        default -> throw new IllegalArgumentException("Invalid Isotope Params: " + name);
        };

    }

}
