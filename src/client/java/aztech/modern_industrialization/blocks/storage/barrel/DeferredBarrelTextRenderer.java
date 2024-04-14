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
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public class DeferredBarrelTextRenderer {
    private record Entry(BlockPos pos, int sideMask, int itemNameColor) {
    }

    private static final List<Entry> barrelsToRender = new ArrayList<>();
    private static final MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(new BufferBuilder(128));

    public static void init() {
        NeoForge.EVENT_BUS.addListener(DeferredBarrelTextRenderer::render);
    }

    public static void enqueueBarrelForRendering(BlockPos pos, int sideMask, int itemNameColor) {
        barrelsToRender.add(new Entry(pos.immutable(), sideMask, itemNameColor));
    }

    private static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        for (var entry : barrelsToRender) {
            var pos = entry.pos();
            int sideMask = entry.sideMask();

            if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity entity)) {
                continue;
            }

            String amount;
            if (entity.behaviour.isCreative()) {
                amount = "∞";
            } else {
                amount = String.valueOf(entity.getAmount());
            }
            ItemStack toRender = entity.getResource().toStack();
            var matrices = event.getPoseStack();

            for (int i = 0; i < 4; ++i) {
                if ((sideMask & (1 << i)) == 0) {
                    continue;
                }

                Font textRenderer = Minecraft.getInstance().font;
                String itemName = toRender.getHoverName().getString();
                matrices.pushPose();
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
                textRenderer.drawInBatch(itemName, xPosition, -4f + 40, entry.itemNameColor(), false, matrices.last().pose(), immediate,
                        Font.DisplayMode.NORMAL, 0, RenderHelper.FULL_LIGHT);

                matrices.popPose();

                matrices.pushPose();
                matrices.translate(0.5, 0.5, 0.5);
                matrices.mulPose(Axis.YP.rotationDegrees((2 - i) * 90F));
                matrices.translate(0, 0.0875, -0.505);
                matrices.scale(-0.01f, -0.01F, -0.01f);

                xPosition = (float) (-textRenderer.width(amount) / 2);
                textRenderer.drawInBatch(amount, xPosition, -4f + 40, 0x000000, false, matrices.last().pose(), immediate,
                        Font.DisplayMode.NORMAL, 0, RenderHelper.FULL_LIGHT);

                matrices.popPose();
            }
        }

        barrelsToRender.clear();
        immediate.endBatch();
    }
}
