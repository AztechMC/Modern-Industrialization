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
package aztech.modern_industrialization.blocks.storage.barrel;

import aztech.modern_industrialization.util.RenderHelper;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class BarrelRenderer implements BlockEntityRenderer<BlockEntity> {

    private final int textColor;

    public static void register(BlockEntityType<BlockEntity> type, int textColor) {
        BlockEntityRendererRegistry.INSTANCE.register(type, (BlockEntityRendererFactory.Context context) -> new BarrelRenderer(textColor));
    }

    private BarrelRenderer(int textColor) {
        this.textColor = textColor;
    }

    @Override
    public void render(BlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        BarrelBlockEntity barrelBlockEntity = (BarrelBlockEntity) entity;

        ItemVariant item = barrelBlockEntity.getResource();
        if (!item.isBlank()) {
            long amount = barrelBlockEntity.getAmount();
            if (amount > 0) {

                ItemStack toRender = new ItemStack(item.getItem(), 1);

                for (int i = 0; i < 4; i++) {

                    // Thanks TechReborn for rendering code

                    matrices.push();
                    matrices.translate(0.5, 0, 0.5);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(i * 90F));
                    matrices.scale(0.5F, 0.5F, 0.5F);
                    matrices.translate(0, 1.3, 1.01);

                    matrices.multiplyPositionMatrix(Matrix4f.scale(1, 1, 0.01f));
                    matrices.peek().getNormalMatrix().multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(45f));

                    MinecraftClient.getInstance().getItemRenderer().renderItem(toRender, ModelTransformation.Mode.GUI, RenderHelper.FULL_LIGHT,
                            OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

                    matrices.pop();

                    matrices.push();
                    TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                    matrices.translate(0.5, 0.5, 0.5);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(i * 90F));
                    matrices.translate(0, 0.2, -0.505);
                    matrices.scale(-0.01f, -0.01F, -0.01f);

                    float xPosition;
                    String count = String.valueOf(amount);
                    xPosition = (float) (-textRenderer.getWidth(count) / 2);
                    textRenderer.draw(count, xPosition, -4f + 40, textColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, false, 0,
                            RenderHelper.FULL_LIGHT);

                    matrices.pop();
                }
            }

        }
    }

}
