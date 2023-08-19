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

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

public class BarrelRenderer implements BlockEntityRenderer<BarrelBlockEntity> {

    public static void register(BlockEntityType<? extends BarrelBlockEntity> type, int itemNameColor) {
        BlockEntityRenderers.register(type, context -> new BarrelRenderer(itemNameColor));
    }

    private final int itemNameColor;

    public BarrelRenderer(int itemNameColor) {
        this.itemNameColor = itemNameColor;
    }

    @Override
    public void render(@NotNull BarrelBlockEntity entity, float tickDelta, @NotNull PoseStack matrices, @NotNull MultiBufferSource vertexConsumers,
            int light, int overlay) {

        if (entity.isLocked()) {
            RenderHelper.drawLockedTexture(entity, matrices, vertexConsumers, itemNameColor);
        }

        if (!MIConfig.getConfig().enableBarrelContentRendering) {
            return;
        }

        var state = entity.getBlockState();
        var pos = entity.getBlockPos();

        ItemVariant item = entity.getResource();

        if (!item.isBlank()) {

            String amount;
            if (entity.behaviour.isCreative()) {
                amount = "∞";
            } else {
                amount = String.valueOf(entity.getAmount());
            }

            ItemStack toRender = item.toStack();

            for (int i = 0; i < 4; i++) {
                var direction = Direction.from2DDataValue(i);
                // Note: level can be null from builtin item renderer
                if (entity.getLevel() != null
                        && !Block.shouldRenderFace(state, entity.getLevel(), pos, direction, pos.relative(direction))) {
                    continue;
                }

                String itemName = toRender.getHoverName().getString();
                matrices.pushPose();
                Font textRenderer = Minecraft.getInstance().font;
                matrices.translate(0.5, 1.14, 0.5);
                matrices.mulPose(Axis.YP.rotationDegrees((2 - i) * 90F));
                matrices.translate(0, 0.15, -0.505);
                matrices.scale(-0.01f, -0.01F, -0.01f);

                // Adjust width
                final int maxWidth = 100;
                if (textRenderer.width(itemName) > maxWidth) {
                    itemName = textRenderer.plainSubstrByWidth(itemName, maxWidth - textRenderer.width("...")) + "...";
                }

                float xPosition = (float) (-textRenderer.width(itemName) / 2);
                textRenderer.drawInBatch(itemName, xPosition, -4f + 40, itemNameColor, false, matrices.last().pose(), vertexConsumers,
                        Font.DisplayMode.NORMAL, 0, RenderHelper.FULL_LIGHT);

                matrices.popPose();

                // Thanks TechReborn for rendering code

                matrices.pushPose();
                matrices.translate(0.5, 0, 0.5);
                matrices.mulPose(Axis.YP.rotationDegrees(-i * 90F));
                matrices.scale(0.5F, 0.5F, 0.5F);
                matrices.translate(0, 1.125, 1.01);

                matrices.last().pose().scale(1, 1, 0.01f);
                matrices.last().normal().rotate(Mth.HALF_PI / 2, -1, 0, 0);

                Minecraft.getInstance().getItemRenderer().renderStatic(toRender, ItemDisplayContext.GUI, RenderHelper.FULL_LIGHT,
                        OverlayTexture.NO_OVERLAY, matrices, vertexConsumers, entity.getLevel(), 0);

                matrices.popPose();

                matrices.pushPose();
                matrices.translate(0.5, 0.5, 0.5);
                matrices.mulPose(Axis.YP.rotationDegrees((2 - i) * 90F));
                matrices.translate(0, 0.0875, -0.505);
                matrices.scale(-0.01f, -0.01F, -0.01f);

                xPosition = (float) (-textRenderer.width(amount) / 2);
                textRenderer.drawInBatch(amount, xPosition, -4f + 40, 0x000000, false, matrices.last().pose(), vertexConsumers,
                        Font.DisplayMode.NORMAL, 0, RenderHelper.FULL_LIGHT);

                matrices.popPose();
            }

        }
    }

}
