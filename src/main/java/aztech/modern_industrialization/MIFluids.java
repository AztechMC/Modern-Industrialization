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

import aztech.modern_industrialization.fluid.MIFluid;
import java.util.ArrayList;
import net.minecraft.core.Registry;

// @formatter:off
public class MIFluids {

    public static final ArrayList<MIFluid> FLUIDS = new ArrayList<>();

    public static final MIFluid ACETYLENE = new MIFluid("acetylene", 0xff603405, true);
    public static final MIFluid ACRYLIC_ACID = new MIFluid("acrylic_acid", 0xff1bdeb5);
    public static final MIFluid ACRYLIC_GLUE = new MIFluid("acrylic_glue", 0xff1bde54);
    public static final MIFluid BENZENE = new MIFluid("benzene", 0xfff0d179);
    public static final MIFluid BOOSTED_DIESEL = new MIFluid("boosted_diesel", 0xfffd9b0a);
    public static final MIFluid BUTADIENE = new MIFluid("butadiene", 0xffd0bd1a);
    public static final MIFluid CAPROLACTAM = new MIFluid("caprolactam", 0xff795450);
    public static final MIFluid CHLORINE = new MIFluid("chlorine", 0xffb7c114, true);
    public static final MIFluid CHROMIUM_HYDROCHLORIC_SOLUTION = new MIFluid("chromium_hydrochloric_solution", 0xfffabe73);
    public static final MIFluid CRUDE_OIL = new MIFluid("crude_oil", 0xff3e3838);
    public static final MIFluid DIESEL = new MIFluid("diesel", 0xffe9bf2d);
    public static final MIFluid DIETHYL_ETHER = new MIFluid("diethyl_ether", 0xff8ec837);
    public static final MIFluid ETHANOL = new MIFluid("ethanol", 0xff608936);
    public static final MIFluid ETHYLBENZENE = new MIFluid("ethylbenzene", 0xffc4fa57);
    public static final MIFluid ETHYLENE = new MIFluid("ethylene", 0xff287671, true);
    public static final MIFluid HEAVY_FUEL = new MIFluid("heavy_fuel", 0xffffdb46);
    public static final MIFluid HYDROCHLORIC_ACID = new MIFluid("hydrochloric_acid", 0xff9ebd06);
    public static final MIFluid HYDROGEN = new MIFluid("hydrogen", 0xff1b4acc, true);
    public static final MIFluid LIGHT_FUEL = new MIFluid("light_fuel", 0xffffe946);
    public static final MIFluid MANGANESE_SULFURIC_SOLUTION = new MIFluid("manganese_sulfuric_solution", 0xffb96c3f);
    public static final MIFluid METHANE = new MIFluid("methane", 0xffb740d9, true);
    public static final MIFluid NAPHTHA = new MIFluid("naphtha", 0xffa5a25e);
    public static final MIFluid NYLON = new MIFluid("nylon", 0xff986a64);
    public static final MIFluid OXYGEN = new MIFluid("oxygen", 0xff3296f2, true);
    public static final MIFluid POLYETHYLENE = new MIFluid("polyethylene", 0xff639c98);
    public static final MIFluid POLYVINYL_CHLORIDE = new MIFluid("polyvinyl_chloride", 0xfff6d3ec);
    public static final MIFluid PROPENE = new MIFluid("propene", 0xff98644c);
    public static final MIFluid RAW_SYNTHETIC_OIL = new MIFluid("raw_synthetic_oil", 0xff474740);
    public static final MIFluid SHALE_OIL = new MIFluid("shale_oil", 0xff6e7373);
    public static final MIFluid SODIUM_HYDROXIDE = new MIFluid("sodium_hydroxide", 0xff5071c9);
    public static final MIFluid STEAM = new MIFluid("steam", 0xffeeeeee, true);
    public static final MIFluid STEAM_CRACKED_NAPHTHA = new MIFluid("steam_cracked_naphtha", 0xffd2d0ae);
    public static final MIFluid STYRENE = new MIFluid("styrene", 0xff9e47f2);
    public static final MIFluid STYRENE_BUTADIENE = new MIFluid("styrene_butadiene", 0xff9c8040);
    public static final MIFluid STYRENE_BUTADIENE_RUBBER = new MIFluid("styrene_butadiene_rubber", 0xff423821);
    public static final MIFluid SULFURIC_ACID = new MIFluid("sulfuric_acid", 0xffe15b00);
    public static final MIFluid SULFURIC_CRUDE_OIL = new MIFluid("sulfuric_crude_oil", 0xff4b5151);
    public static final MIFluid SULFURIC_HEAVY_FUEL = new MIFluid("sulfuric_heavy_fuel", 0xfff2cf3c);
    public static final MIFluid SULFURIC_LIGHT_FUEL = new MIFluid("sulfuric_light_fuel", 0xfff4dd34);
    public static final MIFluid SULFURIC_NAPHTHA = new MIFluid("sulfuric_naphtha", 0xffa5975e);
    public static final MIFluid SYNTHETIC_OIL = new MIFluid("synthetic_oil", 0xff1a1a1a);
    public static final MIFluid SYNTHETIC_RUBBER = new MIFluid("synthetic_rubber", 0xff1a1a1a);
    public static final MIFluid TOLUENE = new MIFluid("toluene", 0xff9ce6ed);
    public static final MIFluid VINYL_CHLORIDE = new MIFluid("vinyl_chloride", 0xffeda7d9);
    public static final MIFluid HELIUM = new MIFluid("helium", 0xffe6e485, true);
    public static final MIFluid ARGON = new MIFluid("argon", 0xffe339a7, true);
    public static final MIFluid HELIUM_3 = new MIFluid("helium_3", 0xff83de52, true);
    public static final MIFluid DEUTERIUM = new MIFluid("deuterium", 0xff941bcc, true);
    public static final MIFluid TRITIUM = new MIFluid("tritium", 0xffcc1b50, true);
    public static final MIFluid HEAVY_WATER = new MIFluid("heavy_water", 0xff6e18f0);
    public static final MIFluid HEAVY_WATER_STEAM = new MIFluid("heavy_water_steam", 0xffd9cfe8, true);
    public static final MIFluid HIGH_PRESSURE_WATER = new MIFluid("high_pressure_water", 0xff144cb8);
    public static final MIFluid HIGH_PRESSURE_STEAM = new MIFluid("high_pressure_steam", 0xff9c9c9c, true);
    public static final MIFluid HIGH_PRESSURE_HEAVY_WATER = new MIFluid("high_pressure_heavy_water", 0xff3d0b8a);
    public static final MIFluid HIGH_PRESSURE_HEAVY_WATER_STEAM = new MIFluid("high_pressure_heavy_water_steam", 0xff6d647a, true);
    public static final MIFluid LEAD_SODIUM_EUTECTIC = new MIFluid("lead_sodium_eutectic", 0xff604170);
    public static final MIFluid SOLDERING_ALLOY = new MIFluid("soldering_alloy", 0xffabc4bf);
    public static final MIFluid LUBRICANT = new MIFluid("lubricant", 0xffffc400);
    public static final MIFluid PLATINUM_SULFURIC_SOLUTION = new MIFluid("platinum_sulfuric_solution", 0xffe69e75);
    public static final MIFluid PURIFIED_PLATINUM_SULFURIC_SOLUTION = new MIFluid("purified_platinum_sulfuric_solution", 0xffedc08a);
    public static final MIFluid CREOSOTE = new MIFluid("creosote", 0xff636050);
    public static final MIFluid LIQUID_AIR = new MIFluid("liquid_air", 0xff76c7f9);
    public static final MIFluid NITROGEN = new MIFluid("nitrogen", 0xff4491a6, true);
    public static final MIFluid CRYOFLUID = new MIFluid("cryofluid", 0xff3cc0e8);
    public static final MIFluid HELIUM_PLASMA = new MIFluid("helium_plasma", 0xfffff85e, true); // 100 MEU / b
    public static final MIFluid UU_MATER = new MIFluid("uu_matter", 0xffff00bf, false);

    public static void setupFluids() {
        for (MIFluid fluid : FLUIDS) {
            registerFluid(fluid);
        }
    }

    private static void registerFluid(MIFluid fluid) {
        String id = fluid.name;
        Registry.register(Registry.FLUID, new MIIdentifier(id), fluid);
        Registry.register(Registry.BLOCK, new MIIdentifier(id), fluid.block);
    }
}
