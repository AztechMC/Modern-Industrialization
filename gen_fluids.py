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

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.SimpleFluidKey;
import aztech.modern_industrialization.fluid.CraftingFluid;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.minecraft.text.*;
import net.minecraft.util.registry.Registry;

import static aztech.modern_industrialization.ModernIndustrialization.MOD_ID;
import static aztech.modern_industrialization.ModernIndustrialization.RESOURCE_PACK;

/**
 * This is auto-generated, don't edit by hand!
 */
public class MIFluids {
    public static final CraftingFluid FLUID_STEAM = new CraftingFluid("steam", 0xffeeeeee);
    public static final CraftingFluid[] FLUIDS = new CraftingFluid[] {
            FLUID_STEAM,
"""


def gen_fluid(name, color, gas=False, register=True):
    if register:
        global java_class
        java_class += '            new CraftingFluid("%s", 0xff%s),\n' % (name, color[1:])
    gen_name(name)
    bucket_image = image_tint("template/fluid/bucket_content.png", color)
    bucket_image = Image.alpha_composite(bucket_image, Image.open("template/fluid/bucket.png"))
    if gas:
        bucket_image = bucket_image.rotate(180)
    bucket_image.save("src/main/resources/assets/modern_industrialization/textures/items/bucket/%s.png" % name)


shutil.rmtree("src/main/resources/assets/modern_industrialization/textures/items/bucket", ignore_errors=True)
Path("src/main/resources/assets/modern_industrialization/textures/items/bucket").mkdir(parents=True, exist_ok=True)

gen_fluid("steam", "#eeeeee", True, register=False)
gen_fluid("air", "#76c7f9", True)
gen_fluid("bauxite_solution", "#d05739")
gen_fluid("chlorine", "#b7c114", True)
gen_fluid("crude_oil", "#3e3838")
gen_fluid("hydrogen", "#1b4acc", True)
gen_fluid("oxygen", "#3296f2", True)
gen_fluid("raw_synthetic_oil", "#474740")
gen_fluid("rubber", "#1a1a1a")
gen_fluid("sodium_hydroxide", "#5071c9")
gen_fluid("sulfuric_acid", "#e15b00")
gen_fluid("synthetic_oil", "#1a1a1a")

java_class += """\
    };
    
    public static void setupFluids() {
    
    }
    
    static {
        for(CraftingFluid fluid : FLUIDS) {
            registerFluid(fluid);
    
            Text name = new TranslatableText(fluid.getDefaultState().getBlockState().getBlock().getTranslationKey()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(fluid.color)));
            fluid.key = new SimpleFluidKey(new FluidKey.FluidKeyBuilder(fluid).setName(name).setRenderColor(fluid.color));
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