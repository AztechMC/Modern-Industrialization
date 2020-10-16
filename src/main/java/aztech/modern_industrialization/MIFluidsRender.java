package aztech.modern_industrialization;

import aztech.modern_industrialization.fluid.CraftingFluid;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.function.Consumer;

import static aztech.modern_industrialization.MIFluids.FLUIDS;

public class MIFluidsRender {
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
                    waterSprites[i] = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(waterSpriteIds[i]);
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
