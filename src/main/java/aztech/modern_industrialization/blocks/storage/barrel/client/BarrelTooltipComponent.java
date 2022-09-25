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
package aztech.modern_industrialization.blocks.storage.barrel.client;

import aztech.modern_industrialization.blocks.storage.barrel.BarrelTooltipData;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public record BarrelTooltipComponent(BarrelTooltipData data) implements ClientTooltipComponent {

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return Math.max(textRenderer.width(data.variant().toStack().getHoverName()), 20 + textRenderer.width(getItemNumber()));
    }

    @Override
    public void renderText(Font textRenderer, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource immediate) {

        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false);

        textRenderer.drawInBatch(data.variant().toStack().getHoverName().copy().setStyle(style), x, y, -1, true, matrix4f, immediate, false, 0,
                15728880);

        textRenderer.drawInBatch(getItemNumber(), x + 20, y + 15, -1, true, matrix4f, immediate, false, 0, 15728880);

    }

    public Component getItemNumber() {
        long maxCount = data.variant().getItem().getMaxStackSize();
        long stackCapacity = data.capacity() / maxCount;
        long amount = data.amount();
        long stackNumber = amount / maxCount;
        long rem = amount % maxCount;

        Component itemNumber;

        if (data.creative()) {
            itemNumber = Component.literal("∞ / ∞").setStyle(TextHelper.YELLOW);
        } else {
            if (maxCount == 1 || !Screen.hasShiftDown()) {
                itemNumber = Component.literal(String.format("%d / %d", amount, stackCapacity * maxCount)).setStyle(TextHelper.YELLOW);
            } else {
                if (stackNumber > 0) {
                    if (rem != 0) {
                        itemNumber = Component.literal(String.format("%d × %d + %d / %d × %d", stackNumber, maxCount, rem, stackCapacity, maxCount))
                                .setStyle(TextHelper.YELLOW);

                    } else {
                        itemNumber = Component.literal(String.format("%d × %d / %d × %d", stackNumber, maxCount, stackCapacity, maxCount))
                                .setStyle(TextHelper.YELLOW);

                    }
                } else {
                    itemNumber = Component.literal(String.format("%d / %d × %d", rem, stackCapacity, maxCount)).setStyle(TextHelper.YELLOW);
                }
            }
        }

        return itemNumber;
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, PoseStack matrices, ItemRenderer itemRenderer, int z) {
        itemRenderer.renderGuiItem(data.variant().toStack(), x, y + 10);
    }
}
