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

package aztech.modern_industrialization.client.pipes.impl;

import aztech.modern_industrialization.client.pipes.api.PipeRenderer;
import aztech.modern_industrialization.client.util.ModelHelper;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * The models of a pipe block. It can handle up to three different pipe types.
 * The block is divided in five slots of width SIDE, three for the main pipes
 * and two for connection handling.
 */
public class PipeBlockStateModel implements DynamicBlockStateModel {
    private final Material.Baked particleMaterial;
    final Map<PipeRenderer.Factory, PipeRenderer> renderers;
    private final BlockStateModelPart @Nullable [] meWireConnectors;

    public PipeBlockStateModel(Material.Baked particleMaterial, Map<PipeRenderer.Factory, PipeRenderer> renderers, BlockStateModelPart @Nullable [] meWireConnectors) {
        this.particleMaterial = particleMaterial;
        this.renderers = renderers;
        this.meWireConnectors = meWireConnectors;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        var attachment = level.getModelData(pos).get(PipeBlockEntity.RenderAttachment.KEY);
        if (attachment == null) {
            return;
        }

        var camouflage = attachment.camouflage();

        if (camouflage == null || MIPipes.transparentCamouflage) {
            var cutoutQuads = new QuadCollection.Builder();
            var translucentQuads = new QuadCollection.Builder();

            int centerSlots = attachment.types().length;
            for (int slot = 0; slot < centerSlots; slot++) {
                int color = attachment.types()[slot].getColor();
                renderers.get(PipeRenderer.get(attachment.types()[slot])).draw(
                        cutoutQuads::addUnculledFace, translucentQuads::addUnculledFace, level, pos, slot,
                        attachment.renderedConnections(), color, attachment.customData()[slot]);
            }

            parts.add(new SimpleModelWrapper(cutoutQuads.build(), true, particleMaterial));
            var allTranslucentQuads = translucentQuads.build();
            if (!allTranslucentQuads.getAll().isEmpty()) {
                parts.add(new SimpleModelWrapper(allTranslucentQuads, true, particleMaterial));
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
                        parts.add(meWireConnectors[direction.get3DDataValue()]);
                    }
                }
            }
        }

        if (camouflage != null) {
            var adjacentCamouflages = EnumSet.noneOf(Direction.class);
            for (var direction : Direction.values()) {
                var adjacentModelData = level.getModelData(pos.relative(direction))
                        .get(PipeBlockEntity.RenderAttachment.KEY);
                if (adjacentModelData != null && adjacentModelData.camouflage() != null) {
                    adjacentCamouflages.add(direction);
                }
            }

            var camouflageModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(camouflage);
            var camouflageParts = new ArrayList<BlockStateModelPart>();
            camouflageModel.collectParts(level, pos, state, random, camouflageParts);
            for (var basePart : camouflageParts) {
                var newQuads = new QuadCollection.Builder();
                for (var direction : aztech.modern_industrialization.client.util.ModelHelper.DIRECTIONS_WITH_NULL) {
                    if (adjacentCamouflages.contains(direction)) {
                        // Don't draw faces between camouflaged pipes
                        continue;
                    }

                    for (var baseQuad : basePart.getQuads(direction)) {
                        int colorMultiplier = -1;

                        if (baseQuad.materialInfo().isTinted()) {
                            var blockColorMap = Minecraft.getInstance().getBlockColors();
                            var tintSource = blockColorMap.getTintSource(camouflage, baseQuad.materialInfo().tintIndex());
                            if (tintSource != null) {
                                colorMultiplier = 0xFF000000 | tintSource.colorInWorld(camouflage, level, pos);
                            }
                        }

                        if (MIPipes.transparentCamouflage) {
                            colorMultiplier = ARGB.multiply(0x9FFFFFFF, colorMultiplier);
                        }

                        BakedQuad newQuad;
                        if (colorMultiplier != -1) {
                            var baseInfo = baseQuad.materialInfo();
                            var newMaterialInfo = BakedQuad.MaterialInfo.of(
                                    new Material.Baked(baseInfo.sprite(), false),
                                    MIPipes.transparentCamouflage ? baseInfo.sprite().transparency() : baseInfo.sprite().transparency().or(Transparency.TRANSLUCENT),
                                    -1, baseInfo.shade(), baseInfo.lightEmission(), baseInfo.ambientOcclusion());
                            newQuad = new BakedQuad(
                                    baseQuad.position0(), baseQuad.position1(), baseQuad.position2(), baseQuad.position3(),
                                    baseQuad.packedUV0(), baseQuad.packedUV1(), baseQuad.packedUV2(), baseQuad.packedUV3(),
                                    baseQuad.direction(), newMaterialInfo,
                                    baseQuad.bakedNormals(),
                                    multiplyColor(baseQuad.bakedColors(), colorMultiplier));
                        } else {
                            newQuad = baseQuad;
                        }

                        ModelHelper.addQuad(newQuads, direction, newQuad);
                    }
                }

                parts.add(new PipeCamouflagePart(
                        camouflage,
                        newQuads.build(),
                        basePart.ambientOcclusion(),
                        particleMaterial));
            }
        }
    }

    private static BakedColors multiplyColor(BakedColors colors, int color2) {
        return switch (colors) {
            case BakedColors.PerQuad(int color) -> BakedColors.of(ARGB.multiply(color, color2));
            case BakedColors.PerVertex pv -> BakedColors.of(
                    ARGB.multiply(pv.color0(), color2),
                    ARGB.multiply(pv.color1(), color2),
                    ARGB.multiply(pv.color2(), color2),
                    ARGB.multiply(pv.color3(), color2));
        };
    }

    @Deprecated
    @Override
    public Material.Baked particleMaterial() {
        return particleMaterial;
    }

    @Deprecated
    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return BakedQuad.FLAG_TRANSLUCENT | BakedQuad.FLAG_ANIMATED;
    }
}
