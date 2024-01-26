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

import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.client.MIRenderTypes;
import aztech.modern_industrialization.thirdparty.fabricrendering.QuadBuffer;
import aztech.modern_industrialization.util.GeometryHelper;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.joml.Matrix4f;

public class MachineOverlayClient {
    @SuppressWarnings("ConstantConditions")
    public static void onBlockOutline(RenderHighlightEvent.Block event) {
        var blockHitResult = event.getTarget();

        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = Minecraft.getInstance().level.getBlockState(pos);
        if (state.getBlock() instanceof MachineBlock
                && Minecraft.getInstance().player.getMainHandItem().is(MITags.WRENCHES)) {
            var poseStack = event.getPoseStack();

            poseStack.pushPose();
            Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            double x = pos.getX() - cameraPos.x;
            double y = pos.getY() - cameraPos.y;
            double z = pos.getZ() - cameraPos.z;
            poseStack.translate(x, y, z);

            // Colored face overlay
            Vec3 posInBlock = MachineOverlay.getPosInBlock(blockHitResult);
            Vec3 posOnFace = GeometryHelper.toFaceCoords(posInBlock, blockHitResult.getDirection());

            var emitter = new QuadBuffer();
            VertexConsumer vc = event.getMultiBufferSource().getBuffer(MIRenderTypes.machineOverlay());
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    double minX = MachineOverlay.ZONES[i], maxX = MachineOverlay.ZONES[i + 1];
                    double minY = MachineOverlay.ZONES[j], maxY = MachineOverlay.ZONES[j + 1];
                    boolean insideQuad = minX <= posOnFace.x && posOnFace.x <= maxX && minY <= posOnFace.y && posOnFace.y <= maxY;
                    emitter.emit();
                    emitter.square(blockHitResult.getDirection(), (float) minX, (float) minY, (float) maxX, (float) maxY, -3.5e-4f);
                    float r = 0;
                    float g = insideQuad ? 1 : 0;
                    float b = insideQuad ? 0 : 1;
                    RenderHelper.quadWithAlpha(vc, poseStack.last(), emitter.toBakedQuad(null), r, g, b, 0.3f, 0x7fffffff,
                            -2130706433);
                }
            }
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch(MIRenderTypes.machineOverlay());

            // Extra lines
            VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
            Matrix4f model = poseStack.last().pose();
            Direction face = blockHitResult.getDirection();
            vertex(model, lines, face, MachineOverlay.ZONES[1], MachineOverlay.ZONES[0]);
            vertex(model, lines, face, MachineOverlay.ZONES[1], MachineOverlay.ZONES[3]);
            vertex(model, lines, face, MachineOverlay.ZONES[2], MachineOverlay.ZONES[0]);
            vertex(model, lines, face, MachineOverlay.ZONES[2], MachineOverlay.ZONES[3]);
            vertex(model, lines, face, MachineOverlay.ZONES[0], MachineOverlay.ZONES[1]);
            vertex(model, lines, face, MachineOverlay.ZONES[3], MachineOverlay.ZONES[1]);
            vertex(model, lines, face, MachineOverlay.ZONES[0], MachineOverlay.ZONES[2]);
            vertex(model, lines, face, MachineOverlay.ZONES[3], MachineOverlay.ZONES[2]);

            poseStack.popPose();
        }
    }

    private static void vertex(Matrix4f model, VertexConsumer lines, Direction face, double faceX, double faceY) {
        Vec3 coord = GeometryHelper.toWorldCoords(new Vec3(faceX, faceY, 0), face);
        // assume normal is not useful, it was added in 1.17 but the shader doesn't seem
        // to use it.
        lines.vertex(model, (float) coord.x, (float) coord.y, (float) coord.z).color(0f, 0f, 0f, 0.4f).normal(0, 0, 0).endVertex();
    }
}
