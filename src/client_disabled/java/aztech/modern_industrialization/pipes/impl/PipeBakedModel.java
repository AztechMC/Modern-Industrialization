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
package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import java.util.*;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * The models of a pipe block. It can handle up to three different pipe types.
 * The block is divided in five slots of width SIDE, three for the main pipes
 * and two for connection handling.
 */
public class PipeBakedModel implements BakedModel, FabricBakedModel {
    private final TextureAtlasSprite particleSprite;
    private final Map<PipeRenderer.Factory, PipeRenderer> renderers;
    private final BakedModel[] meWireConnectors;
    private final RenderMaterial translucentMaterial;

    public PipeBakedModel(TextureAtlasSprite particleSprite, Map<PipeRenderer.Factory, PipeRenderer> renderers,
            @Nullable BakedModel[] meWireConnectors, RenderMaterial translucentMaterial) {
        this.particleSprite = particleSprite;
        this.renderers = renderers;
        this.meWireConnectors = meWireConnectors;
        this.translucentMaterial = translucentMaterial;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockRenderView, BlockState state, BlockPos pos, Supplier<RandomSource> supplier,
            RenderContext renderContext) {
        var maybeAttachment = ((RenderAttachedBlockView) blockRenderView).getBlockEntityRenderAttachment(pos);
        if (maybeAttachment instanceof PipeBlockEntity.RenderAttachment attachment) {
            var camouflage = attachment.camouflage();

            if (camouflage == null || MIPipesClient.transparentCamouflage) {
                int centerSlots = attachment.types().length;
                for (int slot = 0; slot < centerSlots; slot++) {
                    // Set color
                    int color = attachment.types()[slot].getColor();
                    renderContext.pushTransform(getColorTransform(color));

                    renderers.get(PipeRenderer.get(attachment.types()[slot])).draw(blockRenderView, pos, renderContext, slot,
                            attachment.renderedConnections(), attachment.customData()[slot]);

                    renderContext.popTransform();
                }

                boolean hasMeWire = false;
                if (meWireConnectors != null) {
                    for (var type : attachment.types()) {
                        if (type.getIdentifier().getPath().endsWith("me_wire")) {
                            hasMeWire = true;
                        }
                    }
                }
                if (hasMeWire) {
                    // Render connector if needed
                    for (var direction : Direction.values()) {
                        boolean renderConnector = false;
                        for (int slot = 0; slot < attachment.types().length; ++slot) {
                            var conn = attachment.renderedConnections()[slot][direction.get3DDataValue()];
                            if (conn == PipeEndpointType.BLOCK && attachment.types()[slot].getIdentifier().getPath().endsWith("me_wire")) {
                                renderConnector = true;
                            }
                        }

                        if (renderConnector) {
                            meWireConnectors[direction.get3DDataValue()].emitBlockQuads(blockRenderView, state, pos, supplier, renderContext);
                        }
                    }
                }
            }

            if (camouflage != null) {
                renderContext.pushTransform(quad -> {
                    // Fix tinting
                    if (quad.colorIndex() != -1) {
                        var blockColorMap = Minecraft.getInstance().getBlockColors();

                        int color = 0xFF000000 | blockColorMap.getColor(camouflage, blockRenderView, pos, quad.colorIndex());
                        quad.colorIndex(-1);

                        for (int vertex = 0; vertex < 4; ++vertex) {
                            quad.color(vertex, multiplyColor(color, quad.color(vertex)));
                        }
                    }

                    if (MIPipesClient.transparentCamouflage) {
                        quad.material(translucentMaterial);

                        for (int vertex = 0; vertex < 4; ++vertex) {
                            quad.color(vertex, multiplyColor(0x9FFFFFFF, quad.color(vertex)));
                        }
                    }

                    return true;
                });

                var camouflageModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(camouflage);
                camouflageModel.emitBlockQuads(blockRenderView, state, pos, supplier, renderContext);

                renderContext.popTransform();
            }
        }
    }

    // Thank you Indigo!
    private static int multiplyColor(int color1, int color2) {
        if (color1 == -1) {
            return color2;
        } else if (color2 == -1) {
            return color1;
        }

        final int alpha = ((color1 >> 24) & 0xFF) * ((color2 >> 24) & 0xFF) / 0xFF;
        final int red = ((color1 >> 16) & 0xFF) * ((color2 >> 16) & 0xFF) / 0xFF;
        final int green = ((color1 >> 8) & 0xFF) * ((color2 >> 8) & 0xFF) / 0xFF;
        final int blue = (color1 & 0xFF) * (color2 & 0xFF) / 0xFF;

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<RandomSource> supplier, RenderContext renderContext) {
        Item item = itemStack.getItem();
        if (item instanceof PipeItem) {
            // TODO: remove allocation if it becomes an issue
            PipeNetworkType type = ((PipeItem) item).type;
            int color = type.getColor();
            renderContext.pushTransform(getColorTransform(color));
            renderContext.pushTransform(ITEM_TRANSFORM);

            PipeEndpointType[][] connections = new PipeEndpointType[][] {
                    { null, null, null, null, PipeEndpointType.BLOCK, PipeEndpointType.BLOCK } };
            renderers.get(PipeRenderer.get(type)).draw(null, null, renderContext, 0, connections, new CompoundTag());

            renderContext.popTransform();
            renderContext.popTransform();
        }
    }

    private static RenderContext.QuadTransform getColorTransform(int color) {
        return quad -> {
            if (quad.tag() == 0) {
                quad.color(color, color, color, color);
            }
            return true;
        };
    }

    private static final RenderContext.QuadTransform ITEM_TRANSFORM = quad -> {
        // Scale pipe to make items look better
        for (int i = 0; i < 4; ++i) {
            Vector3f pos = quad.copyPos(i, null);
            quad.pos(i, pos.x(), pos.y() * 2 - 0.5f, pos.z() * 2 - 0.5f);
        }
        // Remove fluid quads
        if (quad.tag() == 1) {
            return false;
        }
        return true;
    };

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, RandomSource random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particleSprite;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
