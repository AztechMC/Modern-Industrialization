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
package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.util.GeometryHelper;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class MachineOverlay {
    private static final double SIDE = 0.25;
    public static final double[] ZONES = new double[] { 0, SIDE, 1 - SIDE, 1 };

    public static final List<VoxelShape> OVERLAY_SHAPES = new ArrayList<>();
    public static final List<List<Direction>> TOUCHING_DIRECTIONS = new ArrayList<>();

    static {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                for (int k = 0; k < 3; ++k) {
                    List<Direction> directions = new ArrayList<>();
                    OVERLAY_SHAPES.add(VoxelShapes.cuboid(ZONES[i], ZONES[j], ZONES[k], ZONES[i + 1], ZONES[j + 1], ZONES[k + 1]));
                    if (i == 0)
                        directions.add(Direction.WEST);
                    if (i == 2)
                        directions.add(Direction.EAST);
                    if (j == 0)
                        directions.add(Direction.DOWN);
                    if (j == 2)
                        directions.add(Direction.UP);
                    if (k == 0)
                        directions.add(Direction.NORTH);
                    if (k == 2)
                        directions.add(Direction.SOUTH);
                    TOUCHING_DIRECTIONS.add(directions);
                }
            }
        }
    }

    public static Vec3d getPosInBlock(BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        return blockHitResult.getPos().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static int findHitIndex(Vec3d posInBlock) {
        for (int i = 0; i < MachineOverlay.OVERLAY_SHAPES.size(); i++) {
            Box box = MachineOverlay.OVERLAY_SHAPES.get(i).getBoundingBox();
            // move slightly towards box center
            Vec3d dir = box.getCenter().subtract(posInBlock).normalize().multiply(1e-4);
            if (box.contains(posInBlock.add(dir))) {
                return i;
            }
        }
        throw new UnsupportedOperationException("Hit shape could not be found :(");
    }

    public static Direction findHitSide(Vec3d posInBlock, Direction hitFace) {
        int i = findHitIndex(posInBlock);
        List<Direction> shapeDirections = MachineOverlay.TOUCHING_DIRECTIONS.get(i);
        if (shapeDirections.size() == 1) {
            return hitFace; // center
        } else if (shapeDirections.size() == 3) {
            return hitFace.getOpposite(); // corner
        } else {
            // one of the sides. There are 2 directions, so it must be the other one
            for (Direction direction : shapeDirections) {
                if (direction != hitFace) {
                    return direction;
                }
            }
        }
        throw new RuntimeException("Unreachable!");
    }

    public static Direction findHitSide(BlockHitResult bhr) {
        return findHitSide(getPosInBlock(bhr), bhr.getSide());
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean onBlockOutline(WorldRenderContext wrc, WorldRenderContext.BlockOutlineContext boc) {
        HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;

        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = wrc.world().getBlockState(pos);
            if (state.getBlock() instanceof MachineBlock
                    && MinecraftClient.getInstance().player.getMainHandStack().isIn(ModernIndustrialization.WRENCHES)) {
                wrc.matrixStack().push();
                Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
                double x = pos.getX() - cameraPos.x;
                double y = pos.getY() - cameraPos.y;
                double z = pos.getZ() - cameraPos.z;
                wrc.matrixStack().translate(x, y, z);

                // Colored face overlay
                Vec3d posInBlock = getPosInBlock(blockHitResult);
                Vec3d posOnFace = GeometryHelper.toFaceCoords(posInBlock, blockHitResult.getSide());

                MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
                QuadEmitter emitter;
                VertexConsumer vc = wrc.consumers().getBuffer(RenderLayer.getTranslucent());
                for (int i = 0; i < 3; ++i) {
                    for (int j = 0; j < 3; ++j) {
                        double minX = ZONES[i], maxX = ZONES[i + 1];
                        double minY = ZONES[j], maxY = ZONES[j + 1];
                        boolean insideQuad = minX <= posOnFace.x && posOnFace.x <= maxX && minY <= posOnFace.y && posOnFace.y <= maxY;
                        emitter = meshBuilder.getEmitter();
                        emitter.square(blockHitResult.getSide(), (float) minX, (float) minY, (float) maxX, (float) maxY, -0.0001f);
                        float r = 0;
                        float g = insideQuad ? 1 : 0;
                        float b = insideQuad ? 0 : 1;
                        RenderHelper.quadWithAlpha(vc, wrc.matrixStack().peek(), emitter.toBakedQuad(0, null, false), r, g, b, 0.5f, 0x7fffffff,
                                -2130706433);
                    }
                }

                // Extra lines
                VertexConsumer lines = wrc.consumers().getBuffer(RenderLayer.getLines());
                Matrix4f model = wrc.matrixStack().peek().getPositionMatrix();
                Direction face = blockHitResult.getSide();
                vertex(model, lines, face, ZONES[1], ZONES[0]);
                vertex(model, lines, face, ZONES[1], ZONES[3]);
                vertex(model, lines, face, ZONES[2], ZONES[0]);
                vertex(model, lines, face, ZONES[2], ZONES[3]);
                vertex(model, lines, face, ZONES[0], ZONES[1]);
                vertex(model, lines, face, ZONES[3], ZONES[1]);
                vertex(model, lines, face, ZONES[0], ZONES[2]);
                vertex(model, lines, face, ZONES[3], ZONES[2]);

                wrc.matrixStack().pop();
            }
        }
        return true;
    }

    private static void vertex(Matrix4f model, VertexConsumer lines, Direction face, double faceX, double faceY) {
        Vec3d coord = GeometryHelper.toWorldCoords(new Vec3d(faceX, faceY, 0), face);
        // assume normal is not useful, it was added in 1.17 but the shader doesn't seem
        // to use it.
        lines.vertex(model, (float) coord.x, (float) coord.y, (float) coord.z).color(0f, 0f, 0f, 0.4f).normal(0, 0, 0).next();
    }
}
