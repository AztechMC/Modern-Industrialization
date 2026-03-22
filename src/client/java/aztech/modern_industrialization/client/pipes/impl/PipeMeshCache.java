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
import aztech.modern_industrialization.client.util.RenderHelper;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.impl.PipePartBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class PipeMeshCache implements PipeRenderer {
    private record Mesh(List<BakedQuad> pipeQuads, List<BakedQuad> innerQuads) {}

    /**
     * The cached meshes for the connections. Indexed by: [endpoint
     * type][logicalSlot][direction id]["render type" - 1]. "render type" is 0, 1,
     * 2, 3 for straight, short bend, far short bend and long bend. Then it is 4, 5,
     * 6, 7 for conflict handling.
     */
    private final ConcurrentMap<ConnectionMeshKey, Mesh> connectionMeshes = new ConcurrentHashMap<>(128, 0.5f);
    private final Function<ConnectionMeshKey, Mesh> connectionMeshBuilder;

    private record ConnectionMeshKey(int endpointType, int logicalSlot, int directionId, int renderType) {}

    /**
     * The meshes for the center connector. Indexed by: [logicalSlot][bitmask]. The
     * bitmask stores for which direction there is a connection.
     */
    private final ConcurrentMap<CenterMeshKey, Mesh> centerMeshes = new ConcurrentHashMap<>(128, 0.5f);
    private final Function<CenterMeshKey, Mesh> centerMeshBuilder;

    private record CenterMeshKey(int logicalSlot, int bitmask) {}

    /**
     * Create a new `PipeMeshCache`, and populate it.
     *
     * @param innerQuads Whether to add inner quads, e.g. for fluid rendering.
     */
    public PipeMeshCache(SpriteGetter spriteGetter, Material[] spriteIds, boolean innerQuads) {
        // Build the connection cache
        connectionMeshBuilder = key -> {
            int i = key.endpointType;
            int logicalSlot = key.logicalSlot;
            Direction direction = Direction.from3DDataValue(key.directionId);
            int j = key.renderType;

            TextureAtlasSprite sprite = spriteGetter.get(spriteIds[i], () -> "pipe model");

            var mesh = new Mesh(new ArrayList<>(), new ArrayList<>());
            PipeMeshBuilder pmb = new PipeMeshBuilder(mesh.pipeQuads, mesh.innerQuads, innerQuads, PipePartBuilder.getSlotPos(logicalSlot), direction, sprite);
            boolean reduced = j >= 4;
            boolean end = i != 0;
            int renderType = j % 4;
            if (renderType == 0) {
                pmb.straightLine(reduced, end);
            } else if (renderType == 1) {
                pmb.shortBend(reduced, end);
            } else if (renderType == 2) {
                pmb.farShortBend(reduced, end);
            } else {
                pmb.longBend(reduced, end);
            }

            return mesh;
        };

        // Build the center cache
        TextureAtlasSprite sprite = spriteGetter.get(spriteIds[0], () -> "pipe model");
        centerMeshBuilder = key -> {
            int logicalSlot = key.logicalSlot;
            int mask = key.bitmask;

            var mesh = new Mesh(new ArrayList<>(), new ArrayList<>());
            for (Direction direction : Direction.values()) {
                PipeMeshBuilder pmb = new PipeMeshBuilder(mesh.pipeQuads, mesh.innerQuads, innerQuads, PipePartBuilder.getSlotPos(logicalSlot), direction, sprite);
                pmb.noConnection(mask);
            }

            return mesh;
        };
    }

    public void draw(
            Consumer<BakedQuad> cutoutQuads, Consumer<BakedQuad> translucentQuads,
            @Nullable BlockAndTintGetter view, @Nullable BlockPos pos,
            int logicalSlot, PipeEndpointType[][] connections,
            int color, @Nullable Object customData) {
        // The render type of the connections (0 for no connection, 1 for straight pipe,
        // 2 for short bend, etc...)
        int[] renderTypes = new int[6];
        // The initial direction of the connections
        Direction[] initialDirections = new Direction[6];
        // How many connections actually start in the specified direction
        int[] connectionsInDirection = new int[6];
        // A bitmask for the initial directions
        int directionsMask = 0;

        // Compute these variables
        for (Direction direction : Direction.values()) {
            int i = direction.get3DDataValue();
            renderTypes[i] = PipePartBuilder.getRenderType(logicalSlot, direction, connections);
            if (renderTypes[i] != 0) {
                initialDirections[i] = PipePartBuilder.getInitialDirection(logicalSlot, direction, renderTypes[i]);
                connectionsInDirection[initialDirections[i].get3DDataValue()]++;
                directionsMask |= 1 << initialDirections[i].get3DDataValue();
            }
        }

        TextureAtlasSprite still;
        int fluidColor;
        if (customData instanceof FluidResource fluid) {
            still = RenderHelper.getFluidSprite(fluid);
            fluidColor = RenderHelper.getFluidColor(fluid, view, pos);
        } else {
            still = null;
            fluidColor = -1;
        }

        // Render every connection
        for (int i = 0; i < 6; ++i) {
            PipeEndpointType endpointType = connections[logicalSlot][i];
            if (endpointType != null) {
                int renderType = renderTypes[i] - 1;
                if (connectionsInDirection[initialDirections[i].get3DDataValue()] > 1) {
                    renderType += 4; // Conflict handling
                }
                Mesh mesh = connectionMeshes.computeIfAbsent(
                        new ConnectionMeshKey(endpointType.getId(), logicalSlot, i, renderType),
                        connectionMeshBuilder);

                for (var pipeQuad : mesh.pipeQuads) {
                    cutoutQuads.accept(setColor(pipeQuad, color));
                }
                if (still != null) {
                    for (var innerQuad : mesh.innerQuads) {
                        var newUVs = ModelHelper.bakeUvs(new Vector3fc[] {
                                innerQuad.position0(), innerQuad.position1(), innerQuad.position2(), innerQuad.position3(),
                        }, still, innerQuad.direction());

                        translucentQuads.accept(new BakedQuad(
                                innerQuad.position0(), innerQuad.position1(), innerQuad.position2(), innerQuad.position3(),
                                newUVs[0], newUVs[1], newUVs[2], newUVs[3],
                                // TODO 26.1 - double check light emission
                                innerQuad.tintIndex(), innerQuad.direction(), still, innerQuad.shade(), 15,
                                innerQuad.bakedNormals(), BakedColors.of(fluidColor), innerQuad.hasAmbientOcclusion()));
                    }
                }
            }
        }

        // Render the center connector
        var centerMesh = centerMeshes.computeIfAbsent(new CenterMeshKey(logicalSlot, directionsMask), centerMeshBuilder);
        for (var pipeQuad : centerMesh.pipeQuads) {
            cutoutQuads.accept(setColor(pipeQuad, color));
        }
    }

    private static BakedQuad setColor(BakedQuad quad, int newColor) {
        return new BakedQuad(
                quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission(),
                quad.bakedNormals(), BakedColors.of(newColor), quad.hasAmbientOcclusion());
    }
}
