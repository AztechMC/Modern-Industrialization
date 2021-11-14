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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Matrix4f;

public record BarrelTooltipComponent(BarrelTooltipData data) implements TooltipComponent {

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return Math.max(textRenderer.getWidth(data.variant().toStack().getName()), 20 + textRenderer.getWidth(getItemNumber()));
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate) {

        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false);

        textRenderer.draw(data.variant().toStack().getName().shallowCopy().setStyle(style), x, y, -1, true, matrix4f, immediate, false, 0, 15728880);

        textRenderer.draw(getItemNumber(), x + 20, y + 15, -1, true, matrix4f, immediate, false, 0, 15728880);

    }

    public Text getItemNumber() {
        long maxCount = data.variant().getItem().getMaxCount();
        long stackCapacity = data.capacity() / maxCount;
        long amount = data.amount();
        long stackNumber = amount / maxCount;
        long rem = amount % maxCount;

        Text itemNumber;

        if (maxCount == 1 || Screen.hasShiftDown()) {
            itemNumber = new LiteralText(String.format("%d / %d", amount, stackCapacity * maxCount)).setStyle(TextHelper.YELLOW);
        } else {
            if (stackNumber > 0) {
                if (rem != 0) {
                    itemNumber = (new LiteralText(String.format("%d × %d + %d / %d × %d", stackNumber, maxCount, rem, stackCapacity, maxCount))
                            .setStyle(TextHelper.YELLOW));

                } else {
                    itemNumber = new LiteralText(String.format("%d × %d / %d × %d", stackNumber, maxCount, stackCapacity, maxCount))
                            .setStyle(TextHelper.YELLOW);

                }
            } else {
                itemNumber = new LiteralText(String.format("%d / %d × %d", rem, stackCapacity, maxCount)).setStyle(TextHelper.YELLOW);
            }
        }

        return itemNumber;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        itemRenderer.renderGuiItemIcon(data.variant().toStack(), x, y + 10);
    }
}
