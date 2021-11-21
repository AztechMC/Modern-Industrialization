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

import static aztech.modern_industrialization.ModernIndustrialization.MOD_ID;
import static aztech.modern_industrialization.ModernIndustrialization.RESOURCE_PACK;

import aztech.modern_industrialization.fluid.CraftingFluid;
import java.util.ArrayList;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.minecraft.util.registry.Registry;

// @formatter:off
public class MIFluids {

    public static final ArrayList<CraftingFluid> FLUIDS = new ArrayList<>();

    public static final CraftingFluid ACETYLENE = new CraftingFluid("acetylene", 0xff603405, true);
    public static final CraftingFluid ACRYLIC_ACID = new CraftingFluid("acrylic_acid", 0xff1bdeb5);
    public static final CraftingFluid ACRYLIC_GLUE = new CraftingFluid("acrylic_glue", 0xff1bde54);
    public static final CraftingFluid BENZENE = new CraftingFluid("benzene", 0xfff0d179);
    public static final CraftingFluid BOOSTED_DIESEL = new CraftingFluid("boosted_diesel", 0xfffd9b0a);
    public static final CraftingFluid BUTADIENE = new CraftingFluid("butadiene", 0xffd0bd1a);
    public static final CraftingFluid CAPROLACTAM = new CraftingFluid("caprolactam", 0xff795450);
    public static final CraftingFluid CHLORINE = new CraftingFluid("chlorine", 0xffb7c114, true);
    public static final CraftingFluid CHROME_HYDROCHLORIC_SOLUTION = new CraftingFluid("chrome_hydrochloric_solution", 0xfffabe73);
    public static final CraftingFluid CRUDE_OIL = new CraftingFluid("crude_oil", 0xff3e3838);
    public static final CraftingFluid DIESEL = new CraftingFluid("diesel", 0xffe9bf2d);
    public static final CraftingFluid DIETHYL_ETHER = new CraftingFluid("diethyl_ether", 0xff8ec837);
    public static final CraftingFluid ETHANOL = new CraftingFluid("ethanol", 0xff608936);
    public static final CraftingFluid ETHYLBENZENE = new CraftingFluid("ethylbenzene", 0xffc4fa57);
    public static final CraftingFluid ETHYLENE = new CraftingFluid("ethylene", 0xff287671);
    public static final CraftingFluid HEAVY_FUEL = new CraftingFluid("heavy_fuel", 0xffffdb46);
    public static final CraftingFluid HYDROCHLORIC_ACID = new CraftingFluid("hydrochloric_acid", 0xff9ebd06);
    public static final CraftingFluid HYDROGEN = new CraftingFluid("hydrogen", 0xff1b4acc, true);
    public static final CraftingFluid LIGHT_FUEL = new CraftingFluid("light_fuel", 0xffffe946);
    public static final CraftingFluid MANGANESE_SULFURIC_SOLUTION = new CraftingFluid("manganese_sulfuric_solution", 0xffb96c3f);
    public static final CraftingFluid METHANE = new CraftingFluid("methane", 0xffb740d9, true);
    public static final CraftingFluid NAPHTHA = new CraftingFluid("naphtha", 0xffa5a25e);
    public static final CraftingFluid NYLON = new CraftingFluid("nylon", 0xff986a64);
    public static final CraftingFluid OXYGEN = new CraftingFluid("oxygen", 0xff3296f2, true);
    public static final CraftingFluid POLYETHYLENE = new CraftingFluid("polyethylene", 0xff639c98);
    public static final CraftingFluid POLYVINYL_CHLORIDE = new CraftingFluid("polyvinyl_chloride", 0xfff6d3ec);
    public static final CraftingFluid PROPENE = new CraftingFluid("propene", 0xff98644c);
    public static final CraftingFluid RAW_SYNTHETIC_OIL = new CraftingFluid("raw_synthetic_oil", 0xff474740);
    public static final CraftingFluid SHALE_OIL = new CraftingFluid("shale_oil", 0xff6e7373, true);
    public static final CraftingFluid SODIUM_HYDROXIDE = new CraftingFluid("sodium_hydroxide", 0xff5071c9);
    public static final CraftingFluid STEAM = new CraftingFluid("steam", 0xffeeeeee, true);
    public static final CraftingFluid STEAM_CRACKED_NAPHTHA = new CraftingFluid("steam_cracked_naphtha", 0xffd2d0ae);
    public static final CraftingFluid STYRENE = new CraftingFluid("styrene", 0xff9e47f2);
    public static final CraftingFluid STYRENE_BUTADIENE = new CraftingFluid("styrene_butadiene", 0xff9c8040);
    public static final CraftingFluid STYRENE_BUTADIENE_RUBBER = new CraftingFluid("styrene_butadiene_rubber", 0xff423821);
    public static final CraftingFluid SULFURIC_ACID = new CraftingFluid("sulfuric_acid", 0xffe15b00);
    public static final CraftingFluid SULFURIC_CRUDE_OIL = new CraftingFluid("sulfuric_crude_oil", 0xff4b5151);
    public static final CraftingFluid SULFURIC_HEAVY_FUEL = new CraftingFluid("sulfuric_heavy_fuel", 0xfff2cf3c);
    public static final CraftingFluid SULFURIC_LIGHT_FUEL = new CraftingFluid("sulfuric_light_fuel", 0xfff4dd34);
    public static final CraftingFluid SULFURIC_NAPHTHA = new CraftingFluid("sulfuric_naphtha", 0xffa5975e);
    public static final CraftingFluid SYNTHETIC_OIL = new CraftingFluid("synthetic_oil", 0xff1a1a1a);
    public static final CraftingFluid SYNTHETIC_RUBBER = new CraftingFluid("synthetic_rubber", 0xff1a1a1a);
    public static final CraftingFluid TOLUENE = new CraftingFluid("toluene", 0xff9ce6ed);
    public static final CraftingFluid VINYL_CHLORIDE = new CraftingFluid("vinyl_chloride", 0xffeda7d9);
    public static final CraftingFluid HELIUM = new CraftingFluid("helium", 0xffe6e485, true);
    public static final CraftingFluid ARGON = new CraftingFluid("argon", 0xffe339a7, true);
    public static final CraftingFluid HELIUM_3 = new CraftingFluid("helium_3", 0xff83de52, true);
    public static final CraftingFluid DEUTERIUM = new CraftingFluid("deuterium", 0xff941bcc, true);
    public static final CraftingFluid TRITIUM = new CraftingFluid("tritium", 0xffcc1b50, true);
    public static final CraftingFluid HEAVY_WATER = new CraftingFluid("heavy_water", 0xff6e18f0);
    public static final CraftingFluid HEAVY_WATER_STEAM = new CraftingFluid("heavy_water_steam", 0xffd9cfe8, true);
    public static final CraftingFluid HIGH_PRESSURE_WATER = new CraftingFluid("high_pressure_water", 0xff144cb8);
    public static final CraftingFluid HIGH_PRESSURE_STEAM = new CraftingFluid("high_pressure_steam", 0xff9c9c9c, true);
    public static final CraftingFluid HIGH_PRESSURE_HEAVY_WATER = new CraftingFluid("high_pressure_heavy_water", 0xff3d0b8a);
    public static final CraftingFluid HIGH_PRESSURE_HEAVY_WATER_STEAM = new CraftingFluid("high_pressure_heavy_water_steam", 0xff6d647a, true);
    public static final CraftingFluid LEAD_SODIUM_EUTECTIC = new CraftingFluid("lead_sodium_eutectic", 0xff604170);
    public static final CraftingFluid SOLDERING_ALLOY = new CraftingFluid("soldering_alloy", 0xffabc4bf);
    public static final CraftingFluid LUBRICANT = new CraftingFluid("lubricant", 0xffffc400);
    public static final CraftingFluid PLATINUM_SULFURIC_SOLUTION = new CraftingFluid("platinum_sulfuric_solution", 0xffe69e75);
    public static final CraftingFluid PURIFIED_PLATINUM_SULFURIC_SOLUTION = new CraftingFluid("purified_platinum_sulfuric_solution", 0xffedc08a);
    public static final CraftingFluid CREOSOTE = new CraftingFluid("creosote", 0xff636050);
    public static final CraftingFluid LIQUID_AIR = new CraftingFluid("liquid_air", 0xff76c7f9);
    public static final CraftingFluid NITROGEN = new CraftingFluid("nitrogen", 0xff4491a6, true);
    public static final CraftingFluid CRYOFLUID = new CraftingFluid("cryofluid", 0xff3cc0e8);
    public static final CraftingFluid HELIUM_PLASMA = new CraftingFluid("helium_plasma", 0xfffff85e, true); // 100 MEU / b
    public static final CraftingFluid UU_MATER = new CraftingFluid("uu_matter", 0xffff00bf, false);

    public static void setupFluids() {

    }

    static {
        for (CraftingFluid fluid : FLUIDS) {
            registerFluid(fluid);
        }
    }

    private static void registerFluid(CraftingFluid fluid) {
        String id = fluid.name;
        Registry.register(Registry.FLUID, new MIIdentifier(id), fluid);
        Registry.register(Registry.ITEM, new MIIdentifier("bucket_" + id), fluid.getBucketItem());
        RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated").textures(new JTextures().layer0(MOD_ID + ":items/bucket/" + id)),
                new MIIdentifier("item/bucket_" + id));
    }
}
