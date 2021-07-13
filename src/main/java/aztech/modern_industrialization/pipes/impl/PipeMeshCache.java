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
import aztech.modern_industrialization.util.NbtHelper;
import java.util.function.Function;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

public class PipeMeshCache implements PipeRenderer {
    /**
     * The cached meshes for the connections. Indexed by: [endpoint
     * type][logicalSlot][direction id]["render type" - 1]. "render type" is 0, 1,
     * 2, 3 for straight, short bend, far short bend and long bend. Then it is 4, 5,
     * 6, 7 for conflict handling.
     */
    private final Mesh[][][][] connectionMeshes;
    /**
     * The meshes for the center connector. Indexed by: [logicalSlot][bitmask]. The
     * bitmask stores for which direction there is a connection.
     */
    private final Mesh[][] centerMeshes;
    /**
     * Custom material for the fluids.
     */
    private final RenderMaterial fluidMaterial;

    public PipeMeshCache(Mesh[][][][] connectionMeshes, Mesh[][] centerMeshes, RenderMaterial fluidMaterial) {
        this.connectionMeshes = connectionMeshes;
        this.centerMeshes = centerMeshes;
        this.fluidMaterial = fluidMaterial;
    }

    /**
     * Create a new `PipeMeshCache`, and populate it.
     * 
     * @param innerQuads Whether to add inner quads, e.g. for fluid rendering.
     */
    public PipeMeshCache(Function<SpriteIdentifier, Sprite> textureGetter, SpriteIdentifier[] spriteIds, boolean innerQuads) {
        connectionMeshes = new Mesh[spriteIds.length][3][6][8];
        centerMeshes = new Mesh[3][1 << 6];

        // Build the connection cache
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        for (int i = 0; i < spriteIds.length; ++i) {
            Sprite sprite = textureGetter.apply(spriteIds[i]);
            for (int logicalSlot = 0; logicalSlot < 3; ++logicalSlot) {
                for (Direction direction : Direction.values()) {
                    for (int j = 0; j < 8; ++j) {
                        MeshBuilder meshBuilder = renderer.meshBuilder();
                        PipeMeshBuilder pmb;
                        if (innerQuads) {
                            pmb = new PipeMeshBuilder.InnerQuads(meshBuilder.getEmitter(), PipePartBuilder.getSlotPos(logicalSlot), direction,
                                    sprite);
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
                        connectionMeshes[i][logicalSlot][direction.getId()][j] = meshBuilder.build();
                    }
                }
            }
        }

        // Build the center cache
        Sprite sprite = textureGetter.apply(spriteIds[0]);
        for (int logicalSlot = 0; logicalSlot < 3; ++logicalSlot) {
            for (int mask = 0; mask < (1 << 6); ++mask) {
                MeshBuilder meshBuilder = renderer.meshBuilder();
                for (Direction direction : Direction.values()) {
                    PipeMeshBuilder pmb;
                    if (innerQuads) {
                        pmb = new PipeMeshBuilder.InnerQuads(meshBuilder.getEmitter(), PipePartBuilder.getSlotPos(logicalSlot), direction, sprite);
                    } else {
                        pmb = new PipeMeshBuilder(meshBuilder.getEmitter(), PipePartBuilder.getSlotPos(logicalSlot), direction, sprite);
                    }
                    pmb.noConnection(mask);
                }
                centerMeshes[logicalSlot][mask] = meshBuilder.build();
            }
        }

        fluidMaterial = renderer.materialFinder().emissive(0, true).find();
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
    public void draw(RenderContext ctx, int logicalSlot, PipeEndpointType[][] connections, NbtCompound customData) {
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
            int i = direction.getId();
            renderTypes[i] = PipePartBuilder.getRenderType(logicalSlot, direction, connections);
            if (renderTypes[i] != 0) {
                initialDirections[i] = PipePartBuilder.getInitialDirection(logicalSlot, direction, renderTypes[i]);
                connectionsInDirection[initialDirections[i].getId()]++;
                directionsMask |= 1 << initialDirections[i].getId();
            }
        }

        // Fluid handling logic
        if (customData.contains("fluid")) {
            FluidVariant fluid = NbtHelper.getFluidCompatible(customData, "fluid");
            Sprite still = FluidVariantRendering.getSprite(fluid);
            int color = FluidVariantRendering.getColor(fluid);
            ctx.pushTransform(quad -> {
                if (quad.tag() == 1) {
                    if (still != null) {
                        quad.spriteBake(0, still, MutableQuadView.BAKE_LOCK_UV);
                        quad.spriteColor(0, color, color, color, color);
                        quad.material(fluidMaterial);
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
                if (connectionsInDirection[initialDirections[i].getId()] > 1) {
                    renderType += 4; // Conflict handling
                }
                Mesh mesh = connectionMeshes[endpointType.getId()][logicalSlot][i][renderType];
                ctx.meshConsumer().accept(mesh);
            }
        }

        // Render the center connector
        ctx.meshConsumer().accept(centerMeshes[logicalSlot][directionsMask]);

        // Fluid handling logic
        if (customData.contains("fluid")) {
            ctx.popTransform();
        }
    }

    public Mesh[][][][] getConnectionMeshes() {
        return connectionMeshes;
    }

    public Mesh[][] getCenterMeshes() {
        return centerMeshes;
    }

    public RenderMaterial getFluidMaterial() {
        return fluidMaterial;
    }
}
