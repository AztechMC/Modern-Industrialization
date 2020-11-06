
package aztech.modern_industrialization;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.SimpleFluidKey;
import aztech.modern_industrialization.fluid.CraftingFluid;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.*;
import net.minecraft.util.registry.Registry;

import static aztech.modern_industrialization.ModernIndustrialization.MOD_ID;
import static aztech.modern_industrialization.ModernIndustrialization.RESOURCE_PACK;

/**
 * This is auto-generated, don't edit by hand!
 */
public class MIFluids {
    public static final CraftingFluid ACETYLENE = new CraftingFluid("acetylene", 0xff603405);
    public static final CraftingFluid ACRYLIC_ACID = new CraftingFluid("acrylic_acid", 0xff1bdeb5);
    public static final CraftingFluid ACRYLIC_GLUE = new CraftingFluid("acrylic_glue", 0xff1bde54);
    public static final CraftingFluid AIR = new CraftingFluid("air", 0xff76c7f9);
    public static final CraftingFluid BENZENE = new CraftingFluid("benzene", 0xfff0d179);
    public static final CraftingFluid BOOSTED_DIESEL = new CraftingFluid("boosted_diesel", 0xfffd9b0a);
    public static final CraftingFluid BUTADIENE = new CraftingFluid("butadiene", 0xffd0bd1a);
    public static final CraftingFluid CAPROLACTAM = new CraftingFluid("caprolactam", 0xff795450);
    public static final CraftingFluid CHLORINE = new CraftingFluid("chlorine", 0xffb7c114);
    public static final CraftingFluid CHROME_HYDROCHLORIC_SOLUTION = new CraftingFluid("chrome_hydrochloric_solution", 0xfffabe73);
    public static final CraftingFluid CRUDE_OIL = new CraftingFluid("crude_oil", 0xff3e3838);
    public static final CraftingFluid DIESEL = new CraftingFluid("diesel", 0xffe9bf2d);
    public static final CraftingFluid DIETHYL_ETHER = new CraftingFluid("diethyl_ether", 0xff8ec837);
    public static final CraftingFluid ETHANOL = new CraftingFluid("ethanol", 0xff608936);
    public static final CraftingFluid ETHYLBENZENE = new CraftingFluid("ethylbenzene", 0xffc4fa57);
    public static final CraftingFluid ETHYLENE = new CraftingFluid("ethylene", 0xff287671);
    public static final CraftingFluid HEAVY_FUEL = new CraftingFluid("heavy_fuel", 0xffffdb46);
    public static final CraftingFluid HYDROCHLORIC_ACID = new CraftingFluid("hydrochloric_acid", 0xff9ebd06);
    public static final CraftingFluid HYDROGEN = new CraftingFluid("hydrogen", 0xff1b4acc);
    public static final CraftingFluid LIGHT_FUEL = new CraftingFluid("light_fuel", 0xffffe946);
    public static final CraftingFluid MANGANESE_SULFURIC_SOLUTION = new CraftingFluid("manganese_sulfuric_solution", 0xffb96c3f);
    public static final CraftingFluid METHANE = new CraftingFluid("methane", 0xffb740d9);
    public static final CraftingFluid NAPHTHA = new CraftingFluid("naphtha", 0xffa5a25e);
    public static final CraftingFluid NYLON = new CraftingFluid("nylon", 0xff986a64);
    public static final CraftingFluid OXYGEN = new CraftingFluid("oxygen", 0xff3296f2);
    public static final CraftingFluid POLYETHYLENE = new CraftingFluid("polyethylene", 0xff639c98);
    public static final CraftingFluid POLYVINYL_CHLORIDE = new CraftingFluid("polyvinyl_chloride", 0xfff6d3ec);
    public static final CraftingFluid PROPENE = new CraftingFluid("propene", 0xff98644c);
    public static final CraftingFluid RAW_SYNTHETIC_OIL = new CraftingFluid("raw_synthetic_oil", 0xff474740);
    public static final CraftingFluid RAW_RUBBER = new CraftingFluid("raw_rubber", 0xff514a4a);
    public static final CraftingFluid RUBBER = new CraftingFluid("rubber", 0xff1a1a1a);
    public static final CraftingFluid SHALE_OIL = new CraftingFluid("shale_oil", 0xff6e7373);
    public static final CraftingFluid SODIUM_HYDROXIDE = new CraftingFluid("sodium_hydroxide", 0xff5071c9);
    public static final CraftingFluid STEAM = new CraftingFluid("steam", 0xffeeeeee);
    public static final CraftingFluid STEAM_CRACKED_NAPHTHA = new CraftingFluid("steam_cracked_naphtha", 0xffd2d0ae);
    public static final CraftingFluid STYRENE = new CraftingFluid("styrene", 0xff9e47f2);
    public static final CraftingFluid SULFURIC_ACID = new CraftingFluid("sulfuric_acid", 0xffe15b00);
    public static final CraftingFluid SULFURIC_CRUDE_OIL = new CraftingFluid("sulfuric_crude_oil", 0xff4b5151);
    public static final CraftingFluid SULFURIC_HEAVY_FUEL = new CraftingFluid("sulfuric_heavy_fuel", 0xfff2cf3c);
    public static final CraftingFluid SULFURIC_LIGHT_FUEL = new CraftingFluid("sulfuric_light_fuel", 0xfff4dd34);
    public static final CraftingFluid SULFURIC_NAPHTHA = new CraftingFluid("sulfuric_naphtha", 0xffa5975e);
    public static final CraftingFluid SYNTHETIC_OIL = new CraftingFluid("synthetic_oil", 0xff1a1a1a);
    public static final CraftingFluid TOLUENE = new CraftingFluid("toluene", 0xff9ce6ed);
    public static final CraftingFluid VINYL_CHLORIDE = new CraftingFluid("vinyl_chloride", 0xffeda7d9);
    public static final CraftingFluid[] FLUIDS = new CraftingFluid[] {
            ACETYLENE,
            ACRYLIC_ACID,
            ACRYLIC_GLUE,
            AIR,
            BENZENE,
            BOOSTED_DIESEL,
            BUTADIENE,
            CAPROLACTAM,
            CHLORINE,
            CHROME_HYDROCHLORIC_SOLUTION,
            CRUDE_OIL,
            DIESEL,
            DIETHYL_ETHER,
            ETHANOL,
            ETHYLBENZENE,
            ETHYLENE,
            HEAVY_FUEL,
            HYDROCHLORIC_ACID,
            HYDROGEN,
            LIGHT_FUEL,
            MANGANESE_SULFURIC_SOLUTION,
            METHANE,
            NAPHTHA,
            NYLON,
            OXYGEN,
            POLYETHYLENE,
            POLYVINYL_CHLORIDE,
            PROPENE,
            RAW_SYNTHETIC_OIL,
            RAW_RUBBER,
            RUBBER,
            SHALE_OIL,
            SODIUM_HYDROXIDE,
            STEAM,
            STEAM_CRACKED_NAPHTHA,
            STYRENE,
            SULFURIC_ACID,
            SULFURIC_CRUDE_OIL,
            SULFURIC_HEAVY_FUEL,
            SULFURIC_LIGHT_FUEL,
            SULFURIC_NAPHTHA,
            SYNTHETIC_OIL,
            TOLUENE,
            VINYL_CHLORIDE,
    };
    
    public static void setupFluids() {
    
    }
    
    static {
        for(CraftingFluid fluid : FLUIDS) {
            registerFluid(fluid);
    
            if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                Text name = fluid.getDefaultState().getBlockState().getBlock().getName();
                fluid.key = new SimpleFluidKey(new FluidKey.FluidKeyBuilder(fluid).setName(name).setRenderColor(fluid.color));
            } else {
                fluid.key = FluidKeys.get(fluid);
            }
            FluidKeys.put(fluid, fluid.key);
        }
    }
    
    private static void registerFluid(CraftingFluid fluid) {
        String id = fluid.name;
        Registry.register(Registry.FLUID, new MIIdentifier(id), fluid);
        Registry.register(Registry.ITEM, new MIIdentifier("bucket_" + id), fluid.getBucketItem());
        RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated").textures(new JTextures().layer0(MOD_ID + ":items/bucket/" + id)), new MIIdentifier("item/bucket_" + id));
    }
}
