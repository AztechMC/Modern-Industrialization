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

import aztech.modern_industrialization.definition.FluidDefinition;
import java.util.SortedMap;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;

// @formatter:off
@SuppressWarnings("unused")
public class MIFluids {

    public static SortedMap<ResourceLocation, FluidDefinition> FLUIDS = new TreeMap<>();

    public static final FluidDefinition ACETYLENE = fluid("Acetylene", "acetylene", 0xff603405, true);
    public static final FluidDefinition ACRYLIC_ACID = fluid("Acrylic Acid", "acrylic_acid", 0xff1bdeb5);
    public static final FluidDefinition ACRYLIC_GLUE = fluid("Acrylic Glue", "acrylic_glue", 0xff1bde54);
    public static final FluidDefinition ARGON = fluid("Argon", "argon", 0xffe339a7, true);
    public static final FluidDefinition BENZENE = fluid("Benzene", "benzene", 0xfff0d179);
    public static final FluidDefinition BOOSTED_DIESEL = fluid("Boosted Diesel", "boosted_diesel", 0xfffd9b0a);
    public static final FluidDefinition BUTADIENE = fluid("Butadiene", "butadiene", 0xffd0bd1a);
    public static final FluidDefinition CAPROLACTAM = fluid("Caprolactam", "caprolactam", 0xff795450);
    public static final FluidDefinition CHLORINE = fluid("Chlorine", "chlorine", 0xffb7c114, true);
    public static final FluidDefinition CHROMIUM_HYDROCHLORIC_SOLUTION = fluid("Chromium Hydrochloric Solution","chromium_hydrochloric_solution", 0xfffabe73);
    public static final FluidDefinition CREOSOTE = fluid("Creosote", "creosote", 0xff636050);
    public static final FluidDefinition CRUDE_OIL = fluid("Crude Oil", "crude_oil", 0xff3e3838);
    public static final FluidDefinition CRYOFLUID = fluid("Cryofluid", "cryofluid", 0xff3cc0e8);
    public static final FluidDefinition DEUTERIUM = fluid("Deuterium", "deuterium", 0xff941bcc, true);
    public static final FluidDefinition DIESEL = fluid("Diesel", "diesel", 0xffe9bf2d);
    public static final FluidDefinition DIETHYL_ETHER = fluid("Diethyl Ether", "diethyl_ether", 0xff8ec837);
    public static final FluidDefinition ETHANOL = fluid("Ethanol", "ethanol", 0xff608936);
    public static final FluidDefinition ETHYLBENZENE = fluid("Ethylbenzene", "ethylbenzene", 0xffc4fa57);
    public static final FluidDefinition ETHYLENE = fluid("Ethylene", "ethylene", 0xff287671, true);
    public static final FluidDefinition HEAVY_FUEL = fluid("Heavy Fuel","heavy_fuel", 0xffffdb46);
    public static final FluidDefinition HEAVY_WATER = fluid("Heavy Water", "heavy_water", 0xff6e18f0);
    public static final FluidDefinition HEAVY_WATER_STEAM = fluid("Heavy Water Steam", "heavy_water_steam", 0xffd9cfe8, true);
    public static final FluidDefinition HELIUM = fluid("Helium", "helium", 0xffe6e485, true);
    public static final FluidDefinition HELIUM_PLASMA = fluid("Helium Plasma", "helium_plasma", 0xfffff85e, true); // 100 MEU / b
    public static final FluidDefinition HELIUM_3 = fluid("Helium 3", "helium_3", 0xff83de52, true);
    public static final FluidDefinition HIGH_PRESSURE_HEAVY_WATER = fluid("High Pressure Heavy Water", "high_pressure_heavy_water", 0xff3d0b8a);
    public static final FluidDefinition HIGH_PRESSURE_HEAVY_WATER_STEAM = fluid("High Pressure Heavy Water Steam", "high_pressure_heavy_water_steam", 0xff6d647a, true);
    public static final FluidDefinition HIGH_PRESSURE_STEAM = fluid("High Pressure Steam", "high_pressure_steam", 0xff9c9c9c, true);
    public static final FluidDefinition HIGH_PRESSURE_WATER = fluid("High Pressure Water", "high_pressure_water", 0xff144cb8);
    public static final FluidDefinition HYDROCHLORIC_ACID = fluid("Hydrochloric Acid", "hydrochloric_acid", 0xff9ebd06);
    public static final FluidDefinition HYDROGEN = fluid("Hydrogen", "hydrogen", 0xff1b4acc, true);
    public static final FluidDefinition LIGHT_FUEL = fluid("Light Fuel", "light_fuel", 0xffffe946);
    public static final FluidDefinition LIQUID_AIR = fluid("Liquid Air", "liquid_air", 0xff76c7f9);
    public static final FluidDefinition LUBRICANT = fluid("Lubricant", "lubricant", 0xffffc400);
    public static final FluidDefinition MANGANESE_SULFURIC_SOLUTION = fluid("Manganese Sulfuric Solution", "manganese_sulfuric_solution", 0xffb96c3f);
    public static final FluidDefinition METHANE = fluid("Methane", "methane", 0xffb740d9, true);
    public static final FluidDefinition NAPHTHA = fluid("Naphtha", "naphtha", 0xffa5a25e);
    public static final FluidDefinition NITROGEN = fluid("Nitrogen", "nitrogen", 0xff4491a6, true);
    public static final FluidDefinition NYLON = fluid("Nylon", "nylon", 0xff986a64);
    public static final FluidDefinition OXYGEN = fluid("Oxygen", "oxygen", 0xff3296f2, true);
    public static final FluidDefinition PLATINUM_SULFURIC_SOLUTION = fluid("Platinum Sulfuric Solution", "platinum_sulfuric_solution", 0xffe69e75);
    public static final FluidDefinition POLYETHYLENE = fluid("Polyethylene", "polyethylene", 0xff639c98);
    public static final FluidDefinition POLYVINYL_CHLORIDE = fluid("Polyvinyl Chloride", "polyvinyl_chloride", 0xfff6d3ec);
    public static final FluidDefinition PROPENE = fluid("Propene", "propene", 0xff98644c);
    public static final FluidDefinition PURIFIED_PLATINUM_SULFURIC_SOLUTION = fluid("Purified Platinum Sulfuric Solution","purified_platinum_sulfuric_solution", 0xffedc08a);
    public static final FluidDefinition RAW_SYNTHETIC_OIL = fluid("Raw Synthetic Oil", "raw_synthetic_oil", 0xff474740);
    public static final FluidDefinition SHALE_OIL = fluid("Shale Oil", "shale_oil", 0xff6e7373);
    public static final FluidDefinition SODIUM_HYDROXIDE = fluid("Sodium Hydroxide", "sodium_hydroxide", 0xff5071c9);
    public static final FluidDefinition SOLDERING_ALLOY = fluid("Soldering Alloy", "soldering_alloy", 0xffabc4bf);
    public static final FluidDefinition STEAM = fluid("Steam", "steam", 0xffeeeeee, true);
    public static final FluidDefinition STEAM_CRACKED_NAPHTHA = fluid("Steam-Cracked Naphtha", "steam_cracked_naphtha", 0xffd2d0ae);
    public static final FluidDefinition STYRENE = fluid("Styrene","styrene", 0xff9e47f2);
    public static final FluidDefinition STYRENE_BUTADIENE = fluid("Styrene-Butadiene", "styrene_butadiene", 0xff9c8040);
    public static final FluidDefinition STYRENE_BUTADIENE_RUBBER = fluid("Styrene-Butadiene Rubber", "styrene_butadiene_rubber", 0xff423821);
    public static final FluidDefinition SULFURIC_ACID = fluid("Sulfuric Acid", "sulfuric_acid", 0xffe15b00);
    public static final FluidDefinition SULFURIC_CRUDE_OIL = fluid("Sulfuric Crude Oil", "sulfuric_crude_oil", 0xff4b5151);
    public static final FluidDefinition SULFURIC_HEAVY_FUEL = fluid("Sulfuric Heavy Fuel", "sulfuric_heavy_fuel", 0xfff2cf3c);
    public static final FluidDefinition SULFURIC_LIGHT_FUEL = fluid("Sulfuric Light Fuel","sulfuric_light_fuel", 0xfff4dd34);
    public static final FluidDefinition SULFURIC_NAPHTHA = fluid("Sulfuric Naphtha", "sulfuric_naphtha", 0xffa5975e);
    public static final FluidDefinition SYNTHETIC_OIL = fluid("Synthetic Oil", "synthetic_oil", 0xff1a1a1a);
    public static final FluidDefinition SYNTHETIC_RUBBER = fluid("Synthetic Rubber", "synthetic_rubber", 0xff1a1a1a);
    public static final FluidDefinition TOLUENE = fluid("Toluene", "toluene", 0xff9ce6ed);
    public static final FluidDefinition TRITIUM = fluid("Tritium", "tritium", 0xffcc1b50, true);
    public static final FluidDefinition UU_MATER = fluid("UU Matter", "uu_matter", 0xffff00bf, false);
    public static final FluidDefinition VINYL_CHLORIDE = fluid("Vinyl Chloride", "vinyl_chloride", 0xffeda7d9);

    public static FluidDefinition fluid(String englishName, String id, int color, boolean isGas){
        FluidDefinition definition =  new FluidDefinition(englishName, id, color, isGas);
        if (FLUIDS.put(definition.getId(), definition) != null) {
            throw new IllegalArgumentException("Fluid id already taken : " + definition.getId());
        }
        return definition;
    }

    public static FluidDefinition fluid(String englishName, String id, int color){
        return fluid(englishName, id, color, false);
    }
}
