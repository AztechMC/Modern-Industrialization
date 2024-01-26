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

import static aztech.modern_industrialization.definition.FluidDefinition.*;

import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.definition.FluidTexture;
import java.util.SortedMap;
import java.util.TreeMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// @formatter:off
@SuppressWarnings("unused")
public class MIFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MI.ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MI.ID);
    public static final SortedMap<ResourceLocation, FluidDefinition> FLUID_DEFINITIONS = new TreeMap<>();

    public static void init(IEventBus modBus) {
        FLUIDS.register(modBus);
        FLUID_TYPES.register(modBus);
    }

    public static final FluidDefinition ACETYLENE = fluid("Acetylene", "acetylene", 0xff603405, true);
    public static final FluidDefinition ACRYLIC_ACID = fluid("Acrylic Acid", "acrylic_acid", 0xff1bdeb5, MEDIUM_OPACITY);
    public static final FluidDefinition ACRYLIC_GLUE = fluid("Acrylic Glue", "acrylic_glue", 0xff1bde54, FULL_OPACITY);
    public static final FluidDefinition ARGON = fluid("Argon", "argon", 0xffe339a7, true);
    public static final FluidDefinition BENZENE = fluid("Benzene", "benzene",  0xfff0d179, NEAR_OPACITY);
    public static final FluidDefinition BIODIESEL = fluid("Biodiesel", "biodiesel", 0xff5cb020, NEAR_OPACITY);
    public static final FluidDefinition BOOSTED_DIESEL = fluid("Boosted Diesel", "boosted_diesel", 0xfffd9b0a, NEAR_OPACITY);
    public static final FluidDefinition BUTADIENE = fluid("Butadiene", "butadiene", 0xffd0bd1a, NEAR_OPACITY);
    public static final FluidDefinition CAPROLACTAM = fluid("Caprolactam", "caprolactam", 0xff795450, NEAR_OPACITY);
    public static final FluidDefinition CHLORINE = fluid("Chlorine", "chlorine", 0xffb7c114, true);
    public static final FluidDefinition CHROMIUM_HYDROCHLORIC_SOLUTION = fluid("Chromium Hydrochloric Solution","chromium_hydrochloric_solution",0xfffabe73, MEDIUM_OPACITY);
    public static final FluidDefinition CREOSOTE = fluid("Creosote", "creosote", 0xff636050, NEAR_OPACITY);
    public static final FluidDefinition CRUDE_OIL = fluid("Crude Oil", "crude_oil", 0xff3e3838, NEAR_OPACITY);
    public static final FluidDefinition CRYOFLUID = fluid("Cryofluid", "cryofluid", 0xff3cc0e8, MEDIUM_OPACITY);
    public static final FluidDefinition DEUTERIUM = fluid("Deuterium", "deuterium", 0xff941bcc, true);
    public static final FluidDefinition DIESEL = fluid("Diesel", "diesel", 0xffe9bf2d, NEAR_OPACITY);
    public static final FluidDefinition DIETHYL_ETHER = fluid("Diethyl Ether", "diethyl_ether", 0xff8ec837, NEAR_OPACITY);
    public static final FluidDefinition ETHANOL = fluid("Ethanol", "ethanol", 0xff608936, NEAR_OPACITY);
    public static final FluidDefinition ETHYLBENZENE = fluid("Ethylbenzene", "ethylbenzene", 0xffc4fa57, NEAR_OPACITY);
    public static final FluidDefinition ETHYLENE = fluid("Ethylene", "ethylene", 0xff287671, true);
    public static final FluidDefinition HEAVY_FUEL = fluid("Heavy Fuel","heavy_fuel", 0xffffdb46, NEAR_OPACITY);
    public static final FluidDefinition HEAVY_WATER = fluid("Heavy Water", "heavy_water", 0xff6e18f0);
    public static final FluidDefinition HEAVY_WATER_STEAM = fluid("Heavy Water Steam", "heavy_water_steam", 0xffd9cfe8, MEDIUM_OPACITY, FluidTexture.STEAM_LIKE, true);
    public static final FluidDefinition HELIUM = fluid("Helium", "helium", 0xffe6e485, true);
    public static final FluidDefinition HELIUM_PLASMA = fluid("Helium Plasma", "helium_plasma", 0xfffff85e, FULL_OPACITY, FluidTexture.PLASMA_LIKE, true); // 100 MEU / b
    public static final FluidDefinition HELIUM_3 = fluid("Helium 3", "helium_3", 0xff83de52, true);
    public static final FluidDefinition HIGH_PRESSURE_HEAVY_WATER = fluid("High Pressure Heavy Water", "high_pressure_heavy_water", 0xff3d0b8a, MEDIUM_OPACITY);
    public static final FluidDefinition HIGH_PRESSURE_HEAVY_WATER_STEAM = fluid("High Pressure Heavy Water Steam", "high_pressure_heavy_water_steam", 0xff6d647a, NEAR_OPACITY, FluidTexture.STEAM_LIKE, true);
    public static final FluidDefinition HIGH_PRESSURE_STEAM = fluid("High Pressure Steam", "high_pressure_steam", 0xff9c9c9c, NEAR_OPACITY, FluidTexture.STEAM_LIKE,true);
    public static final FluidDefinition HIGH_PRESSURE_WATER = fluid("High Pressure Water", "high_pressure_water", 0xff144cb8, MEDIUM_OPACITY);
    public static final FluidDefinition HYDROCHLORIC_ACID = fluid("Hydrochloric Acid", "hydrochloric_acid", 0xff9ebd06);
    public static final FluidDefinition HYDROGEN = fluid("Hydrogen", "hydrogen", 0xff1b4acc, true);
    public static final FluidDefinition LIGHT_FUEL = fluid("Light Fuel", "light_fuel", 0xffffe946, NEAR_OPACITY);
    public static final FluidDefinition LIQUID_AIR = fluid("Liquid Air", "liquid_air", 0xff76c7f9, MEDIUM_OPACITY);
    public static final FluidDefinition LUBRICANT = fluid("Lubricant", "lubricant", 0xffffc400, FULL_OPACITY);
    public static final FluidDefinition MANGANESE_SULFURIC_SOLUTION = fluid("Manganese Sulfuric Solution", "manganese_sulfuric_solution", 0xffb96c3f, MEDIUM_OPACITY);
    public static final FluidDefinition METHANE = fluid("Methane", "methane", 0xffb740d9, true);
    public static final FluidDefinition MOLTEN_REDSTONE = fluid("Molten Redstone", "molten_redstone", 0xffac0c04, FULL_OPACITY);
    public static final FluidDefinition NAPHTHA = fluid("Naphtha", "naphtha", 0xffa5a25e, NEAR_OPACITY);
    public static final FluidDefinition NITROGEN = fluid("Nitrogen", "nitrogen", 0xff4491a6, true);
    public static final FluidDefinition NYLON = fluid("Nylon", "nylon", 0xff986a64, FULL_OPACITY);
    public static final FluidDefinition OXYGEN = fluid("Oxygen", "oxygen", 0xff3296f2, true);
    public static final FluidDefinition PLANT_OIL = fluid("Plant Oil", "plant_oil", 0xff78bd1e, NEAR_OPACITY);
    public static final FluidDefinition PLATINUM_SULFURIC_SOLUTION = fluid("Platinum Sulfuric Solution", "platinum_sulfuric_solution", 0xffe69e75, MEDIUM_OPACITY);
    public static final FluidDefinition POLYETHYLENE = fluid("Polyethylene", "polyethylene", 0xff639c98, NEAR_OPACITY);
    public static final FluidDefinition POLYVINYL_CHLORIDE = fluid("Polyvinyl Chloride", "polyvinyl_chloride", 0xfff6d3ec, true);
    public static final FluidDefinition PROPENE = fluid("Propene", "propene", 0xff98644c, NEAR_OPACITY);
    public static final FluidDefinition PURIFIED_PLATINUM_SULFURIC_SOLUTION = fluid("Purified Platinum Sulfuric Solution","purified_platinum_sulfuric_solution", 0xffedc08a, MEDIUM_OPACITY);
    public static final FluidDefinition RAW_BIODIESEL = fluid("Raw Biodiesel", "raw_biodiesel", 0xff2c8009, FULL_OPACITY);
    public static final FluidDefinition RAW_SYNTHETIC_OIL = fluid("Raw Synthetic Oil", "raw_synthetic_oil", 0xff474740, NEAR_OPACITY);
    public static final FluidDefinition SHALE_OIL = fluid("Shale Oil", "shale_oil", 0xff6e7373, NEAR_OPACITY);
    public static final FluidDefinition SODIUM_HYDROXIDE = fluid("Sodium Hydroxide", "sodium_hydroxide", 0xff5071c9);
    public static final FluidDefinition SOLDERING_ALLOY = fluid("Soldering Alloy", "soldering_alloy", 0xffabc4bf, FULL_OPACITY , FluidTexture.LAVA_LIKE, false);
    public static final FluidDefinition SUGAR_SOLUTION = fluid("Sugar Solution", "sugar_solution", 0xff8fccdb, MEDIUM_OPACITY);
    public static final FluidDefinition STEAM = fluid("Steam", "steam", 0xffeeeeee, MEDIUM_OPACITY, FluidTexture.STEAM_LIKE,true);
    public static final FluidDefinition STEAM_CRACKED_HEAVY_FUEL = fluid("Steam-Cracked Heavy Fuel", "steam_cracked_heavy_fuel", 0xffffe57d, NEAR_OPACITY);
    public static final FluidDefinition STEAM_CRACKED_LIGHT_FUEL = fluid("Steam-Cracked Light Fuel", "steam_cracked_light_fuel", 0xffffeca4, NEAR_OPACITY);
    public static final FluidDefinition STEAM_CRACKED_NAPHTHA = fluid("Steam-Cracked Naphtha", "steam_cracked_naphtha", 0xffd2d0ae, NEAR_OPACITY);
    public static final FluidDefinition STYRENE = fluid("Styrene","styrene", 0xff9e47f2, NEAR_OPACITY);
    public static final FluidDefinition STYRENE_BUTADIENE = fluid("Styrene-Butadiene", "styrene_butadiene", 0xff9c8040, NEAR_OPACITY);
    public static final FluidDefinition STYRENE_BUTADIENE_RUBBER = fluid("Styrene-Butadiene Rubber", "styrene_butadiene_rubber", 0xff423821, FULL_OPACITY);
    public static final FluidDefinition SULFURIC_ACID = fluid("Sulfuric Acid", "sulfuric_acid", 0xffe15b00);
    public static final FluidDefinition SULFURIC_CRUDE_OIL = fluid("Sulfuric Crude Oil", "sulfuric_crude_oil", 0xff4b5151, NEAR_OPACITY);
    public static final FluidDefinition SULFURIC_HEAVY_FUEL = fluid("Sulfuric Heavy Fuel", "sulfuric_heavy_fuel", 0xfff2cf3c, NEAR_OPACITY);
    public static final FluidDefinition SULFURIC_LIGHT_FUEL = fluid("Sulfuric Light Fuel","sulfuric_light_fuel", 0xfff4dd34, NEAR_OPACITY);
    public static final FluidDefinition SULFURIC_NAPHTHA = fluid("Sulfuric Naphtha", "sulfuric_naphtha", 0xffa5975e, NEAR_OPACITY);
    public static final FluidDefinition SYNTHETIC_OIL = fluid("Synthetic Oil", "synthetic_oil", 0xff1a1a1a, NEAR_OPACITY);
    public static final FluidDefinition SYNTHETIC_RUBBER = fluid("Synthetic Rubber", "synthetic_rubber", 0xff1a1a1a, FULL_OPACITY);
    public static final FluidDefinition TOLUENE = fluid("Toluene", "toluene", 0xff9ce6ed, NEAR_OPACITY);
    public static final FluidDefinition TRITIUM = fluid("Tritium", "tritium", 0xffcc1b50, true);
    public static final FluidDefinition UU_MATER = fluid("UU Matter", "uu_matter", 0xffff00bf, FULL_OPACITY, false);
    public static final FluidDefinition VINYL_CHLORIDE = fluid("Vinyl Chloride", "vinyl_chloride", 0xffeda7d9, MEDIUM_OPACITY);

    static {
        KubeJSProxy.instance.fireRegisterFluidsEvent();
    }

    public static FluidDefinition fluid(String englishName, String id, int color, int opacity, FluidTexture texture,  boolean isGas){
        var definition = new FluidDefinition(englishName, id, color, opacity, texture, isGas);
        if (FLUID_DEFINITIONS.put(definition.getId(), definition) != null) {
            throw new IllegalArgumentException("Fluid id already taken : " + definition.getId());
        }
        return definition;
    }

    public static FluidDefinition fluid(String englishName, String id, int color, int opacity, boolean isGas) {
        return fluid(englishName, id, color, opacity, FluidTexture.WATER_LIKE, isGas);
    }

    public static FluidDefinition fluid(String englishName, String id, int color, int opacity) {
        return fluid(englishName, id, color, opacity, FluidTexture.WATER_LIKE, false);
    }

    public static FluidDefinition fluid(String englishName, String id, int color,  boolean isGas) {
        return fluid(englishName, id, color, FluidDefinition.LOW_OPACITY, FluidTexture.WATER_LIKE, isGas);
    }

    public static FluidDefinition fluid(String englishName, String id, int color){
        return fluid(englishName, id, color, false);
    }
}
