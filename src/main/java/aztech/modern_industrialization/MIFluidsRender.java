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

import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.fluid.MIFluid;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;

public class MIFluidsRender {
    public static void setupFluidRenders() {
        final ResourceLocation[] waterSpriteIds = new ResourceLocation[] { new ResourceLocation("minecraft:block/water_still"),
                new ResourceLocation("minecraft:block/water_flow") };
        final TextureAtlasSprite[] waterSprites = new TextureAtlasSprite[2];

        final ResourceLocation listenerId = new MIIdentifier("waterlike_reload_listener");
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return listenerId;
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                for (int i = 0; i < 2; ++i) {
                    waterSprites[i] = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(waterSpriteIds[i]);
                }
            }
        });

        Consumer<MIFluid> registerWaterlikeFluid = (fluid) -> {
            FluidRenderHandlerRegistry.INSTANCE.register(fluid, new FluidRenderHandler() {
                @Override
                public TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter blockRenderView, BlockPos blockPos, FluidState fluidState) {
                    return waterSprites;
                }

                @Override
                public int getFluidColor(BlockAndTintGetter view, BlockPos pos, FluidState state) {
                    return fluid.color;
                }
            });
        };

        for (FluidDefinition fluid : FLUIDS.values()) {
            registerWaterlikeFluid.accept(fluid.fluid);
        }
    }
}
