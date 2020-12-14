
package aztech.modern_industrialization;

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
    public static final CraftingFluid AIR = new CraftingFluid("air", 0xff76c7f9);
    public static final CraftingFluid CHLORINE = new CraftingFluid("chlorine", 0xffb7c114);
    public static final CraftingFluid CHROME_HYDROCHLORIC_SOLUTION = new CraftingFluid("chrome_hydrochloric_solution", 0xfffabe73);
    public static final CraftingFluid CRUDE_OIL = new CraftingFluid("crude_oil", 0xff3e3838);
    public static final CraftingFluid DIESEL = new CraftingFluid("diesel", 0xffe9bf2d);
    public static final CraftingFluid HEAVY_FUEL = new CraftingFluid("heavy_fuel", 0xffffdb46);
    public static final CraftingFluid HYDROCHLORIC_ACID = new CraftingFluid("hydrochloric_acid", 0xff9ebd06);
    public static final CraftingFluid HYDROGEN = new CraftingFluid("hydrogen", 0xff1b4acc);
    public static final CraftingFluid LIGHT_FUEL = new CraftingFluid("light_fuel", 0xffffe946);
    public static final CraftingFluid MANGANESE_SULFURIC_SOLUTION = new CraftingFluid("manganese_sulfuric_solution", 0xffb96c3f);
    public static final CraftingFluid NAPHTA = new CraftingFluid("naphta", 0xffa5a25e);
    public static final CraftingFluid OXYGEN = new CraftingFluid("oxygen", 0xff3296f2);
    public static final CraftingFluid RAW_SYNTHETIC_OIL = new CraftingFluid("raw_synthetic_oil", 0xff474740);
    public static final CraftingFluid RAW_RUBBER = new CraftingFluid("raw_rubber", 0xff514a4a);
    public static final CraftingFluid RUBBER = new CraftingFluid("rubber", 0xff1a1a1a);
    public static final CraftingFluid SODIUM_HYDROXIDE = new CraftingFluid("sodium_hydroxide", 0xff5071c9);
    public static final CraftingFluid STEAM = new CraftingFluid("steam", 0xffeeeeee);
    public static final CraftingFluid STEAM_CRACKED_NAPHTA = new CraftingFluid("steam_cracked_naphta", 0xffd2d0ae);
    public static final CraftingFluid SULFURIC_ACID = new CraftingFluid("sulfuric_acid", 0xffe15b00);
    public static final CraftingFluid SULFURIC_HEAVY_FUEL = new CraftingFluid("sulfuric_heavy_fuel", 0xfff2cf3c);
    public static final CraftingFluid SULFURIC_LIGHT_FUEL = new CraftingFluid("sulfuric_light_fuel", 0xfff4dd34);
    public static final CraftingFluid SULFURIC_NAPHTA = new CraftingFluid("sulfuric_naphta", 0xffa5975e);
    public static final CraftingFluid SYNTHETIC_OIL = new CraftingFluid("synthetic_oil", 0xff1a1a1a);
    public static final CraftingFluid[] FLUIDS = new CraftingFluid[] {
            AIR,
            CHLORINE,
            CHROME_HYDROCHLORIC_SOLUTION,
            CRUDE_OIL,
            DIESEL,
            HEAVY_FUEL,
            HYDROCHLORIC_ACID,
            HYDROGEN,
            LIGHT_FUEL,
            MANGANESE_SULFURIC_SOLUTION,
            NAPHTA,
            OXYGEN,
            RAW_SYNTHETIC_OIL,
            RAW_RUBBER,
            RUBBER,
            SODIUM_HYDROXIDE,
            STEAM,
            STEAM_CRACKED_NAPHTA,
            SULFURIC_ACID,
            SULFURIC_HEAVY_FUEL,
            SULFURIC_LIGHT_FUEL,
            SULFURIC_NAPHTA,
            SYNTHETIC_OIL,
    };
    
    public static void setupFluids() {
    
    }
    
    static {
        for(CraftingFluid fluid : FLUIDS) {
            registerFluid(fluid);
        }
    }
    
    private static void registerFluid(CraftingFluid fluid) {
        String id = fluid.name;
        Registry.register(Registry.FLUID, new MIIdentifier(id), fluid);
        Registry.register(Registry.ITEM, new MIIdentifier("bucket_" + id), fluid.getBucketItem());
        RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated").textures(new JTextures().layer0(MOD_ID + ":items/bucket/" + id)), new MIIdentifier("item/bucket_" + id));
    }
}
