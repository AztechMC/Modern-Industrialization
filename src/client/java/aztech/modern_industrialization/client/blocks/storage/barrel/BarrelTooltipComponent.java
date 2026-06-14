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

package aztech.modern_industrialization.client.blocks.storage.barrel;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelTooltipData;
import aztech.modern_industrialization.client.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;

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
        Style style = MITooltips.DEFAULT_STYLE;

        textRenderer.drawInBatch(data.variant().toStack().getHoverName().copy().setStyle(style), x, y, -1, true, matrix4f, immediate,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

        textRenderer.drawInBatch(getItemNumber(), x + 20, y + 15, -1, true, matrix4f, immediate, Font.DisplayMode.NORMAL, 0,
                LightTexture.FULL_BRIGHT);
    }

    public Component getItemNumber() {
        long amount = data.amount();
        long capacity = data.capacity();
        Component itemNumber;

        Style style = TextHelper.YELLOW;

        if (data.creative()) {
            itemNumber = Component.literal("∞ / ∞").setStyle(style);
        } else {
            String amountStr;
            String capacityStr;
            Component percent = MITooltips.INVERTED_RATIO_PERCENTAGE_PARSER.parse((double) amount / capacity);

            if (Screen.hasShiftDown()) {
                amountStr = "" + amount;
                capacityStr = "" + capacity;

            } else {
                var amountText = TextHelper.getAmount(amount);
                var capacityText = TextHelper.getAmount(capacity);
                amountStr = amountText.digit() + amountText.unit();
                capacityStr = capacityText.digit() + capacityText.unit();
            }
            itemNumber = MIText.BarrelStorageComponent.text(amountStr, capacityStr, percent).setStyle(style);
        }

        return itemNumber;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        RenderHelper.renderAndDecorateItem(guiGraphics, font, data.variant().toStack(), x, y + 10);
    }
}
