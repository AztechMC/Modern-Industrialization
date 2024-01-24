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

import aztech.modern_industrialization.thirdparty.fabricrendering.ModelHelper;
import aztech.modern_industrialization.thirdparty.fabricrendering.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
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
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * The models of a pipe block. It can handle up to three different pipe types.
 * The block is divided in five slots of width SIDE, three for the main pipes
 * and two for connection handling.
 */
public class PipeBakedModel implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet RENDER_TYPES_NORMAL = ChunkRenderTypeSet.of(RenderType.cutout(), RenderType.translucent());

    private final TextureAtlasSprite particleSprite;
    private final Map<PipeRenderer.Factory, PipeRenderer> renderers;
    private final BakedModel[] meWireConnectors;
    private final SpriteFinder spriteFinder;

    public PipeBakedModel(TextureAtlasSprite particleSprite, Map<PipeRenderer.Factory, PipeRenderer> renderers,
            @Nullable BakedModel[] meWireConnectors, SpriteFinder spriteFinder) {
        this.particleSprite = particleSprite;
        this.renderers = renderers;
        this.meWireConnectors = meWireConnectors;
        this.spriteFinder = spriteFinder;
    }

    private record ExtraData(BlockAndTintGetter level, BlockPos pos) {
        private static final ModelProperty<ExtraData> KEY = new ModelProperty<>();
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return modelData.derive()
                .with(ExtraData.KEY, new ExtraData(level, pos))
                .build();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        var attachment = data.get(PipeBlockEntity.RenderAttachment.KEY);

        if (attachment == null || attachment.camouflage() == null) {
            return RENDER_TYPES_NORMAL;
        } else {
            return ChunkRenderTypeSet.all();
        }
    }

    private static boolean checkRenderType(RenderType target, @Nullable RenderType type) {
        return type == target || type == null;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        List<BakedQuad> ret = null;
        var attachment = data.get(PipeBlockEntity.RenderAttachment.KEY);
        var extraData = data.get(ExtraData.KEY);

        if (attachment == null || extraData == null) {
            return List.of();
        }

        var camouflage = attachment.camouflage();

        if (camouflage == null || MIPipesClient.transparentCamouflage) {
            var renderNormal = checkRenderType(RenderType.cutout(), renderType);
            var renderFluid = checkRenderType(RenderType.translucent(), renderType);

            if (renderNormal || renderFluid) {
                var renderContext = new PipeRenderContext(spriteFinder, renderNormal, renderFluid);
                ret = renderContext.quads;

                int centerSlots = attachment.types().length;
                for (int slot = 0; slot < centerSlots; slot++) {
                    // Set color
                    int color = attachment.types()[slot].getColor();
                    renderContext.pushTransform(getColorTransform(color));

                    renderers.get(PipeRenderer.get(attachment.types()[slot])).draw(extraData.level(), extraData.pos(), renderContext, slot,
                            attachment.renderedConnections(), attachment.customData()[slot]);

                    renderContext.popTransform();
                }
            }

            if (renderNormal) {
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
                            ret.addAll(meWireConnectors[direction.get3DDataValue()].getQuads(state, side, rand, data, renderType));
                        }
                    }
                }
            }
        }

        if (camouflage != null) {
            // TODO NEO
//            renderContext.pushTransform(quad -> {
//                // Fix tinting
//                if (quad.colorIndex() != -1) {
//                    var blockColorMap = Minecraft.getInstance().getBlockColors();
//
//                    int color = 0xFF000000 | blockColorMap.getColor(camouflage, blockRenderView, pos, quad.colorIndex());
//                    quad.colorIndex(-1);
//
//                    for (int vertex = 0; vertex < 4; ++vertex) {
//                        quad.color(vertex, multiplyColor(color, quad.color(vertex)));
//                    }
//                }
//
//                if (MIPipesClient.transparentCamouflage) {
//                    quad.material(translucentMaterial);
//
//                    for (int vertex = 0; vertex < 4; ++vertex) {
//                        quad.color(vertex, multiplyColor(0x9FFFFFFF, quad.color(vertex)));
//                    }
//                }
//
//                return true;
//            });
//
//            var camouflageModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(camouflage);
//            camouflageModel.emitBlockQuads(blockRenderView, state, pos, supplier, renderContext);
//
//            renderContext.popTransform();
        }

        return ret != null ? ret : List.of();
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

    private static PipeRenderContext.QuadTransform getColorTransform(int color) {
        return quad -> {
            if (quad.tag() == 0) {
                quad.color(color, color, color, color);
            }
            return true;
        };
    }

    private static final PipeRenderContext.QuadTransform ITEM_TRANSFORM = quad -> {
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

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        if (itemStack.getItem() instanceof PipeItem pipe) {
            PipeNetworkType type = pipe.type;
            int color = type.getColor();

            return List.of(new PipeBakedModel(particleSprite, renderers, meWireConnectors, spriteFinder) {
                @Override
                public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
                    if (side != null) {
                        return List.of();
                    }

                    var renderContext = new PipeRenderContext(spriteFinder, true, true);

                    renderContext.pushTransform(getColorTransform(color));
                    renderContext.pushTransform(ITEM_TRANSFORM);

                    PipeEndpointType[][] connections = new PipeEndpointType[][] {
                            { null, null, null, null, PipeEndpointType.BLOCK, PipeEndpointType.BLOCK } };
                    renderers.get(PipeRenderer.get(type)).draw(null, null, renderContext, 0, connections, new CompoundTag());

                    renderContext.popTransform();
                    renderContext.popTransform();

                    return renderContext.quads;
                }
            });
        } else {
            return List.of(this);
        }
    }
}
