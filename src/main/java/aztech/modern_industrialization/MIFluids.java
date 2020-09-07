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
            new CraftingFluid("raw_synthetic_oil", 0xff000000),
            new CraftingFluid("synthetic_oil", 0xff000000),
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

    public static void setupFluidRenders() {
        final Identifier[] waterSpriteIds = new Identifier[] { new Identifier("minecraft:block/water_still"), new Identifier("minecraft:block/water_flow") };
        final Sprite[] waterSprites = new Sprite[2];

        final Identifier listenerId = new MIIdentifier("waterlike_reload_listener");
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return listenerId;
            }

            @Override
            public void apply(ResourceManager manager) {
                for(int i = 0; i < 2; ++i) {
                    waterSprites[i] = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(waterSpriteIds[i]);
                }
            }
        });

        Consumer<CraftingFluid> registerWaterlikeFluid = (fluid) -> {
            FluidRenderHandlerRegistry.INSTANCE.register(fluid, new FluidRenderHandler() {
                @Override
                public Sprite[] getFluidSprites(BlockRenderView blockRenderView, BlockPos blockPos, FluidState fluidState) {
                    return waterSprites;
                }

                @Override
                public int getFluidColor(BlockRenderView view, BlockPos pos, FluidState state) {
                    return fluid.color;
                }
            });
        };

        for(CraftingFluid fluid : FLUIDS) {
            registerWaterlikeFluid.accept(fluid);
        }
    }
}
