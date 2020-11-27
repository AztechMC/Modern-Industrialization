from PIL import Image
from PIL.ImageOps import grayscale, colorize

import json
import shutil
from pathlib import Path

def image_tint(src, tint='#ffffff'):
    src = Image.open(src).convert('RGBA')
    r, g, b, alpha = src.split()
    gray = grayscale(src)
    result = colorize(gray, (0, 0, 0, 0), tint)
    result.putalpha(alpha)
    return result


def clean(string):
    return " ".join([word.title() for word in string.split('_')])


def gen_name(fluid):
    fluid_no_minus = fluid.replace("-", "_")
    with open('src/main/resources/assets/modern_industrialization/lang/en_us.json', 'r') as lang_file:
        lang_json = json.load(lang_file)
        lang_file.close()

    lang_json['block.modern_industrialization.' + fluid_no_minus] = clean(fluid)
    lang_json['item.modern_industrialization.bucket_' + fluid_no_minus] = clean(fluid) + " Bucket"

    with open('src/main/resources/assets/modern_industrialization/lang/en_us.json', 'w') as lang_file:
        json.dump(lang_json, lang_file, indent=4, sort_keys=True)
        lang_file.close()


java_class="""
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
"""
fluid_variables = []


def gen_fluid(name, color, gas=False):
    name_no_minus = name.replace("-", "_")
    global java_class
    java_class += '    public static final CraftingFluid %s = new CraftingFluid("%s", 0xff%s);\n' % (name_no_minus.upper(), name_no_minus, color[1:])
    global fluid_variables
    fluid_variables.append(name_no_minus.upper())
    gen_name(name)
    bucket_image = image_tint("template/fluid/bucket_content.png", color)
    bucket_image = Image.alpha_composite(bucket_image, Image.open("template/fluid/bucket.png"))
    if gas:
        bucket_image = bucket_image.rotate(180)
    bucket_image.save("src/main/resources/assets/modern_industrialization/textures/items/bucket/%s.png" % name_no_minus)


shutil.rmtree("src/main/resources/assets/modern_industrialization/textures/items/bucket", ignore_errors=True)
Path("src/main/resources/assets/modern_industrialization/textures/items/bucket").mkdir(parents=True, exist_ok=True)

gen_fluid("acetylene", '#603405', True)
gen_fluid("acrylic_acid", "#1bdeb5")
gen_fluid("acrylic_glue", "#1bde54")
gen_fluid("air", "#76c7f9", True)
gen_fluid("benzene", "#f0d179")
gen_fluid("boosted_diesel", "#fd9b0a")
gen_fluid("butadiene", "#d0bd1a")
gen_fluid("caprolactam", "#795450")
gen_fluid("chlorine", "#b7c114", True)
gen_fluid("chrome_hydrochloric_solution", "#fabe73")
gen_fluid("crude_oil", "#3e3838")
gen_fluid("diesel", "#e9bf2d")
gen_fluid("diethyl_ether", "#8ec837")
gen_fluid("ethanol", "#608936")
gen_fluid("ethylbenzene", "#c4fa57")
gen_fluid("ethylene", "#287671")
gen_fluid("heavy_fuel", "#ffdb46")
gen_fluid("hydrochloric_acid", "#9ebd06")
gen_fluid("hydrogen", "#1b4acc", True)
gen_fluid("light_fuel", "#ffe946")
gen_fluid("manganese_sulfuric_solution", "#b96c3f")
gen_fluid("methane", "#b740d9")
gen_fluid("naphtha", "#a5a25e")
gen_fluid("nylon", "#986a64")
gen_fluid("oxygen", "#3296f2", True)
gen_fluid("polyethylene", "#639c98")
gen_fluid("polyvinyl_chloride", "#f6d3ec")
gen_fluid("propene", "#98644c")
gen_fluid("raw_synthetic_oil", "#474740")
gen_fluid("raw_rubber", "#514a4a")
gen_fluid("rubber", "#1a1a1a")
gen_fluid("shale_oil", "#6e7373", True)
gen_fluid("sodium_hydroxide", "#5071c9")
gen_fluid("steam", "#eeeeee", True)
gen_fluid("steam-cracked_naphtha", "#d2d0ae")
gen_fluid("styrene", "#9e47f2")
gen_fluid("styrene-butadiene", "#9c8040")
gen_fluid("styrene-butadiene_rubber", "#423821")
gen_fluid("sulfuric_acid", "#e15b00")
gen_fluid("sulfuric_crude_oil", "#4b5151")
gen_fluid("sulfuric_heavy_fuel", "#f2cf3c")
gen_fluid("sulfuric_light_fuel", "#f4dd34")
gen_fluid("sulfuric_naphtha", "#a5975e")
gen_fluid("synthetic_oil", "#1a1a1a")
gen_fluid("toluene", "#9ce6ed")
gen_fluid("vinyl_chloride", "#eda7d9")
gen_fluid("helium", "#e6e485", True)
gen_fluid("argon", "#e339a7", True)
gen_fluid("helium_3", "#83de52", True)
gen_fluid("deuterium", "#941bcc", True)
gen_fluid("tritium", "#cc1b50", True)
gen_fluid("heavy_water", "#6e18f0")
gen_fluid("heavy_water_steam", "#d9cfe8", True)
gen_fluid("high_pressure_water", "#144cb8")
gen_fluid("high_pressure_steam", "#9c9c9c", True)
gen_fluid("high_pressure_heavy_water", "#3d0b8a")
gen_fluid("high_pressure_heavy_water_steam", "#6d647a", True)
gen_fluid("lead_sodium_eutectic", "#604170")
gen_fluid("soldering_alloy", "#abc4bf")
gen_fluid("lubricant", "#ffc400")


java_class += "    public static final CraftingFluid[] FLUIDS = new CraftingFluid[] {\n"
for var in fluid_variables:
    java_class += "            " + var + ",\n"

java_class += """\
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
"""
with open("src/main/java/aztech/modern_industrialization/MIFluids.java", "w") as f:
    f.write(java_class)