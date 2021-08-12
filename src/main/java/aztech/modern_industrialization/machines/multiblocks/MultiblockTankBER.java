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
package aztech.modern_industrialization.machines.multiblocks;

import aztech.modern_industrialization.machines.blockentities.multiblocks.LargeTankMultiblockBlockEntity;
import aztech.modern_industrialization.util.RenderHelper;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class MultiblockTankBER extends MultiblockMachineBER {
    public MultiblockTankBER(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(MultiblockMachineBlockEntity be, float tickDelta, MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay) {
        super.render(be, tickDelta, ms, vcp, light, overlay);
        LargeTankMultiblockBlockEntity tankBlockEntity = (LargeTankMultiblockBlockEntity) be;
        FluidVariant fluid = tankBlockEntity.getFluid();
        if (tankBlockEntity.shapeValid.shapeValid && !fluid.isBlank() && tankBlockEntity.getFullnessFraction() > 0) {

            VertexConsumer vc = vcp.getBuffer(RenderLayer.getTranslucent());

            Sprite sprite = FluidVariantRendering.getSprite(fluid);
            int color = FluidVariantRendering.getColor(fluid);

            float r = ((color >> 16) & 255) / 256f;
            float g = ((color >> 8) & 255) / 256f;
            float b = (color & 255) / 256f;

            double[] cornerPosition = tankBlockEntity.getCornerPosition();

            float maxX = (float) cornerPosition[0];
            float maxY = (float) cornerPosition[1];
            float maxZ = (float) cornerPosition[2];

            float minX = (float) cornerPosition[3];
            float minY = (float) cornerPosition[4];
            float minZ = (float) cornerPosition[5];

            float fullness = (float) tankBlockEntity.getFullnessFraction();

            // Top and bottom positions of the fluid inside the tank
            float topHeight = fullness;
            float bottomHeight = 0;
            // Render gas from top to bottom
            if (FluidVariantRendering.fillsFromTop(fluid)) {
                topHeight = 1;
                bottomHeight = 1 - fullness;
            }

            ms.translate(minX, minY, minZ);
            ms.scale(maxX - minX, maxY - minY, maxZ - minZ);

            Renderer renderer = RendererAccess.INSTANCE.getRenderer();
            for (Direction direction : Direction.values()) {
                QuadEmitter emitter = renderer.meshBuilder().getEmitter();

                if (direction.getAxis().isVertical()) {
                    emitter.square(direction, 0, 0, 1, 1, direction == Direction.UP ? 1 - topHeight : bottomHeight);
                } else {
                    emitter.square(direction, 0, bottomHeight, 1, topHeight, 0);
                }

                emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
                emitter.spriteColor(0, -1, -1, -1, -1);
                vc.quad(ms.peek(), emitter.toBakedQuad(0, sprite, false), r, g, b, RenderHelper.FULL_LIGHT, OverlayTexture.DEFAULT_UV);
            }

        }

    }
}
