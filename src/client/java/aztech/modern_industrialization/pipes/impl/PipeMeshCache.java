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

import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import aztech.modern_industrialization.thirdparty.fabricrendering.Mesh;
import aztech.modern_industrialization.thirdparty.fabricrendering.MeshBuilder;
import aztech.modern_industrialization.thirdparty.fabricrendering.MeshBuilderImpl;
import aztech.modern_industrialization.thirdparty.fabricrendering.MutableQuadView;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.client.fluid.FluidVariantRendering;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

public class PipeMeshCache implements PipeRenderer {
    /**
     * The cached meshes for the connections. Indexed by: [endpoint
     * type][logicalSlot][direction id]["render type" - 1]. "render type" is 0, 1,
     * 2, 3 for straight, short bend, far short bend and long bend. Then it is 4, 5,
     * 6, 7 for conflict handling.
     */
    private final ConcurrentMap<ConnectionMeshKey, Mesh> connectionMeshes = new ConcurrentHashMap<>(128, 0.5f);
    private final Function<ConnectionMeshKey, Mesh> connectionMeshBuilder;

    private record ConnectionMeshKey(int endpointType, int logicalSlot, int directionId, int renderType) {
    }

    /**
     * The meshes for the center connector. Indexed by: [logicalSlot][bitmask]. The
     * bitmask stores for which direction there is a connection.
     */
    private final ConcurrentMap<CenterMeshKey, Mesh> centerMeshes = new ConcurrentHashMap<>(128, 0.5f);
    private final Function<CenterMeshKey, Mesh> centerMeshBuilder;

    private record CenterMeshKey(int logicalSlot, int bitmask) {
    }

    /**
     * Create a new `PipeMeshCache`, and populate it.
     * 
     * @param innerQuads Whether to add inner quads, e.g. for fluid rendering.
     */
    public PipeMeshCache(Function<Material, TextureAtlasSprite> textureGetter, Material[] spriteIds, boolean innerQuads) {
        // Build the connection cache
        connectionMeshBuilder = key -> {
            int i = key.endpointType;
            int logicalSlot = key.logicalSlot;
            Direction direction = Direction.from3DDataValue(key.directionId);
            int j = key.renderType;

            TextureAtlasSprite sprite = textureGetter.apply(spriteIds[i]);

            MeshBuilder meshBuilder = new MeshBuilderImpl();
            PipeMeshBuilder pmb;
            if (innerQuads) {
                pmb = new PipeMeshBuilder.InnerQuads(meshBuilder.getEmitter(), PipePartBuilder.getSlotPos(logicalSlot), direction, sprite);
            } else {
                pmb = new PipeMeshBuilder(meshBuilder.getEmitter(), PipePartBuilder.getSlotPos(logicalSlot), direction, sprite);
            }
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

            return meshBuilder.build();
        };

        // Build the center cache
        TextureAtlasSprite sprite = textureGetter.apply(spriteIds[0]);
        centerMeshBuilder = key -> {
            int logicalSlot = key.logicalSlot;
            int mask = key.bitmask;

            MeshBuilder meshBuilder = new MeshBuilderImpl();
            for (Direction direction : Direction.values()) {
                PipeMeshBuilder pmb;
                if (innerQuads) {
                    pmb = new PipeMeshBuilder.InnerQuads(meshBuilder.getEmitter(), PipePartBuilder.getSlotPos(logicalSlot), direction, sprite);
                } else {
                    pmb = new PipeMeshBuilder(meshBuilder.getEmitter(), PipePartBuilder.getSlotPos(logicalSlot), direction, sprite);
                }
                pmb.noConnection(mask);
            }

            return meshBuilder.build();
        };
    }

    /**
     * Draw the connections for a logical slot.
     * 
     * @param ctx         Render context.
     * @param logicalSlot The logical slot, so 0 for center, 1 for lower and 2 for
     *                    upper.
     * @param connections For every logical slot, then for every direction, the
     *                    connection type or null for no connection.
     */
    public void draw(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, PipeRenderContext ctx, int logicalSlot,
            PipeEndpointType[][] connections,
            CompoundTag customData) {
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

        // Fluid handling logic
        if (customData.contains("fluid")) {
            FluidVariant fluid = NbtHelper.getFluidCompatible(customData, "fluid");
            TextureAtlasSprite still = FluidVariantRendering.getSprite(fluid);
            int color = FluidVariantRendering.getColor(fluid, view, pos);
            ctx.pushTransform(quad -> {
                if (quad.tag() == 1) {
                    if (still != null) {
                        quad.spriteBake(still, MutableQuadView.BAKE_LOCK_UV);
                        quad.color(color, color, color, color);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            });
        }

        // Render every connection
        for (int i = 0; i < 6; ++i) {
            PipeEndpointType endpointType = connections[logicalSlot][i];
            if (endpointType != null) {
                int renderType = renderTypes[i] - 1;
                if (connectionsInDirection[initialDirections[i].get3DDataValue()] > 1) {
                    renderType += 4; // Conflict handling
                }
                Mesh mesh = connectionMeshes.computeIfAbsent(new ConnectionMeshKey(endpointType.getId(), logicalSlot, i, renderType),
                        connectionMeshBuilder);
                mesh.outputTo(ctx.getEmitter());
            }
        }

        // Render the center connector
        centerMeshes.computeIfAbsent(new CenterMeshKey(logicalSlot, directionsMask), centerMeshBuilder).outputTo(ctx.getEmitter());

        // Fluid handling logic
        if (customData.contains("fluid")) {
            ctx.popTransform();
        }
    }
}
