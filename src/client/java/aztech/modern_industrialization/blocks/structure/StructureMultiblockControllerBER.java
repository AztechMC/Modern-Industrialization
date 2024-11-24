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
import aztech.modern_industrialization.blocks.structure.controller.StructureMultiblockControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class StructureMultiblockControllerBER extends StructureMultiblockBER<StructureMultiblockControllerBlockEntity> {
    @Override
    public void render(StructureMultiblockControllerBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int light,
            int overlay) {
        super.render(be, tickDelta, matrices, vcp, light, overlay);

        if (!be.shouldShowBounds()) {
            return;
        }
        BlockPos pos = be.getBlockPos();
        StructureControllerBounds bounds = be.getBounds();
        AABB box = bounds.aabb();
        if (box != null) {
            matrices.pushPose();
            VertexConsumer buffer = vcp.getBuffer(RenderType.lines());
            LevelRenderer.renderLineBox(matrices, buffer, box, 1, 1, 1, 1);
            matrices.popPose();
        }
    }
}
