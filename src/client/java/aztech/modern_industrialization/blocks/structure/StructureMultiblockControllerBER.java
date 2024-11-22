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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

public class StructureMultiblockControllerBER implements BlockEntityRenderer<StructureMultiblockControllerBlockEntity> {
    @Override
    public void render(StructureMultiblockControllerBlockEntity controller, float tickDelta, PoseStack matrices, MultiBufferSource vcs, int light,
            int overlay) {
        if (!controller.shouldShowBounds()) {
            return;
        }
        BlockPos pos = controller.getBlockPos();
        BoundingBox bounds = controller.getBounds();
        if (bounds == null) {
            bounds = new BoundingBox(0, 0, 0, 0, 0, 0);
        }
        matrices.pushPose();
        VertexConsumer buffer = vcs.getBuffer(RenderType.lines());
        AABB box = AABB.of(bounds);
        LevelRenderer.renderLineBox(matrices, buffer, box, 1, 1, 1, 1);
        matrices.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(StructureMultiblockControllerBlockEntity controller) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(StructureMultiblockControllerBlockEntity controller) {
        return AABB.INFINITE;
    }
}
