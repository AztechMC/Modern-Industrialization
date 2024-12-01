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
package aztech.modern_industrialization.blocks.structure;

import aztech.modern_industrialization.blocks.structure.controller.StructureControllerBounds;
import aztech.modern_industrialization.blocks.structure.controller.StructureControllerMode;
import aztech.modern_industrialization.blocks.structure.controller.StructureMultiblockControllerBlockEntity;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class StructureMultiblockControllerBER implements BlockEntityRenderer<StructureMultiblockControllerBlockEntity> {
    private static List<BlockPos> MISCONFIGURED_BLOCKS = new ArrayList<>();

    public static void setMisconfigured(List<BlockPos> positions) {
        MISCONFIGURED_BLOCKS = new ArrayList<>(positions);
    }

    public static void forgetMisconfigured(List<BlockPos> positions) {
        MISCONFIGURED_BLOCKS.removeAll(positions);
    }

    @Override
    public void render(StructureMultiblockControllerBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int light,
            int overlay) {
        if (be.getMode() == StructureControllerMode.SAVE) {
            BlockPos pos = be.getBlockPos();
            StructureControllerBounds bounds = be.getBounds();

            AABB box = bounds.aabb();
            if (box != null) {
                if ((System.currentTimeMillis() / 500L) % 2 == 0) {
                    AABB worldBox = box.move(pos);
                    for (BlockPos misconfiguredPos : MISCONFIGURED_BLOCKS) {
                        if (worldBox.contains(misconfiguredPos.getX(), misconfiguredPos.getY(), misconfiguredPos.getZ())) {
                            matrices.pushPose();
                            matrices.translate(misconfiguredPos.getX() - pos.getX(), misconfiguredPos.getY() - pos.getY(),
                                    misconfiguredPos.getZ() - pos.getZ());
                            matrices.translate(-0.005f, -0.005f, -0.005f);
                            matrices.scale(1.01f, 1.01f, 1.01f);
                            RenderHelper.drawOverlay(matrices, vcp, 1.0f, 111f / 256f, 111f / 256f, RenderHelper.FULL_LIGHT, overlay, false);
                            matrices.popPose();
                        }
                    }
                }

                if (be.shouldShowBounds()) {
                    matrices.pushPose();
                    VertexConsumer buffer = vcp.getBuffer(RenderType.lines());
                    LevelRenderer.renderLineBox(matrices, buffer, box, 1, 1, 1, 1);
                    matrices.popPose();
                }
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(StructureMultiblockControllerBlockEntity be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(StructureMultiblockControllerBlockEntity be) {
        return AABB.INFINITE;
    }
}
