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
package aztech.modern_industrialization.blocks.storage.tank;

import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TankRenderer implements BlockEntityRenderer<BlockEntity> {
    public TankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        TankBlockEntity tank = (TankBlockEntity) entity;
        if (!tank.getResource().isBlank()) {
            if (tank.getAmount() > 0) {
                RenderHelper.drawFluidInTank(entity, matrices, vertexConsumers, tank.getResource(), (float) tank.getAmount() / tank.capacity);
            } else if (tank.isLocked()) {
                RenderHelper.drawFluidInTank(entity, matrices, vertexConsumers, tank.getResource(), 0.01f);
            }

        }

        if (tank.isLocked()) {
            RenderHelper.drawLockedTexture(tank, matrices, vertexConsumers);
        }
    }
}
