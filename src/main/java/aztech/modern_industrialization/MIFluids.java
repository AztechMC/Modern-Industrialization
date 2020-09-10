package aztech.modern_industrialization;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.SimpleFluidKey;
import aztech.modern_industrialization.fluid.CraftingFluid;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FluidState;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;

import java.util.function.Consumer;

import static aztech.modern_industrialization.ModernIndustrialization.MOD_ID;
import static aztech.modern_industrialization.ModernIndustrialization.RESOURCE_PACK;

public class MIFluids {
    public static final CraftingFluid FLUID_STEAM = new CraftingFluid("steam", 0xffeeeeee);
    public static final CraftingFluid[] FLUIDS = new CraftingFluid[] {
            FLUID_STEAM,
            new CraftingFluid("raw_synthetic_oil", 0xff474740),
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
