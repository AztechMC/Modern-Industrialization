
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
            new CraftingFluid("air", 0xff76c7f9),
            new CraftingFluid("bauxite_solution", 0xffd05739),
            new CraftingFluid("chlorine", 0xffb7c114),
            new CraftingFluid("hydrogen", 0xff1b4acc),
            new CraftingFluid("oxygen", 0xff3296f2),
            new CraftingFluid("raw_synthetic_oil", 0xff474740),
            new CraftingFluid("rubber", 0xff1a1a1a),
            new CraftingFluid("sodium_hydroxide", 0xff5071c9),
            new CraftingFluid("sulfuric_acid", 0xffe15b00),
            new CraftingFluid("synthetic_oil", 0xff1a1a1a),
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
