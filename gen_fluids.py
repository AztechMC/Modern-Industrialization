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
    return " ".join([word.capitalize() for word in string.split('_')])


def gen_name(fluid):
    with open('src/main/resources/assets/modern_industrialization/lang/en_us.json', 'r') as lang_file:
        lang_json = json.load(lang_file)
        lang_file.close()

    lang_json['block.modern_industrialization.' + fluid] = clean(fluid)
    lang_json['item.modern_industrialization.bucket_' + fluid] = clean(fluid) + " Bucket"

    with open('src/main/resources/assets/modern_industrialization/lang/en_us.json', 'w') as lang_file:
        json.dump(lang_json, lang_file, indent=4, sort_keys=True)
        lang_file.close()


java_class="""
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
"""
fluid_variables = []


def gen_fluid(name, color, gas=False):
    global java_class
    java_class += '    public static final CraftingFluid %s = new CraftingFluid("%s", 0xff%s);\n' % (name.upper(), name, color[1:])
    global fluid_variables
    fluid_variables.append(name.upper())
    gen_name(name)
    handle_fluid(name, color, gas, False)
    handle_fluid(name, color, gas, True)


def handle_fluid(name, color, gas, is_alt):
    if is_alt:
        content_path = "bucket_content_alt.png"
        bucket_path = "bucket_alt.png"
        output_path = "src/main/resources/alternate/assets/modern_industrialization/textures/items/bucket/"
    else:
        content_path = "bucket_content.png"
        bucket_path = "bucket.png"
        output_path = "src/main/resources/assets/modern_industrialization/textures/items/bucket/"
    bucket_image = image_tint("template/fluid/" + content_path, color)
    bucket_image = Image.alpha_composite(Image.open("template/fluid/" + bucket_path).convert('RGBA'), bucket_image)
    if gas:
        bucket_image = bucket_image.rotate(180)
    bucket_image.save(output_path + "%s.png" % name)


shutil.rmtree("src/main/resources/assets/modern_industrialization/textures/items/bucket", ignore_errors=True)
Path("src/main/resources/assets/modern_industrialization/textures/items/bucket/").mkdir(parents=True, exist_ok=True)
Path("src/main/resources/alternate/assets/modern_industrialization/textures/items/bucket/").mkdir(parents=True, exist_ok=True)

gen_fluid("air", "#76c7f9", True)
gen_fluid("chlorine", "#b7c114", True)
gen_fluid("chrome_hydrochloric_solution", "#fabe73")
gen_fluid("crude_oil", "#3e3838")
gen_fluid("diesel", "#e9bf2d")
gen_fluid("heavy_fuel", "#ffdb46")
gen_fluid("hydrochloric_acid", "#9ebd06")
gen_fluid("hydrogen", "#1b4acc", True)
gen_fluid("light_fuel", "#ffe946")
gen_fluid("manganese_sulfuric_solution", "#b96c3f")
gen_fluid("naphta", "#a5a25e")
gen_fluid("fluorine", "#DBD576", True)
gen_fluid("beryllium_chloride", "#85B354")
gen_fluid("oxygen", "#3296f2", True)
gen_fluid("raw_synthetic_oil", "#474740")
gen_fluid("raw_rubber", "#514a4a")
gen_fluid("rubber", "#1a1a1a")
gen_fluid("sodium_hydroxide", "#5071c9")
gen_fluid("steam", "#eeeeee", True)
gen_fluid("steam_cracked_naphta", "#d2d0ae")
gen_fluid("sulfuric_acid", "#e15b00")
gen_fluid("sulfuric_heavy_fuel", "#f2cf3c")
gen_fluid("sulfuric_light_fuel", "#f4dd34")
gen_fluid("sulfuric_naphta", "#a5975e")
gen_fluid("synthetic_oil", "#1a1a1a")

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