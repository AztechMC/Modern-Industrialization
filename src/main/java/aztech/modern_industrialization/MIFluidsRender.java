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

import static aztech.modern_industrialization.MIFluids.FLUIDS;

import aztech.modern_industrialization.fluid.CraftingFluid;
import java.util.function.Consumer;
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

public class MIFluidsRender {
    public static void setupFluidRenders() {
        final Identifier[] waterSpriteIds = new Identifier[] { new Identifier("minecraft:block/water_still"),
                new Identifier("minecraft:block/water_flow") };
        final Sprite[] waterSprites = new Sprite[2];

        final Identifier listenerId = new MIIdentifier("waterlike_reload_listener");
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return listenerId;
            }

            @Override
            public void apply(ResourceManager manager) {
                for (int i = 0; i < 2; ++i) {
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

        for (CraftingFluid fluid : FLUIDS) {
            registerWaterlikeFluid.accept(fluid);
        }
    }
}
