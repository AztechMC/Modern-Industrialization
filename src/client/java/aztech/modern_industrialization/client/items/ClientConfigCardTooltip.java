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

package aztech.modern_industrialization.client.items;

import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.client.util.RenderHelper;
import aztech.modern_industrialization.items.ConfigCardItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public record ClientConfigCardTooltip(ConfigCardItem.TooltipData data) implements ClientTooltipComponent {
    @Override
    public int getHeight(Font font) {
        return 20;
    }

    @Override
    public int getWidth(Font font) {
        return 18 * Math.min(6, data.filter().size());
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        int i = 0;
        for (var stack : data.filter()) {
            RenderHelper.renderAndDecorateItem(graphics, font, stack, x + i * 18, y);
            if (++i >= 5) {
                graphics.text(
                        font, Component.literal("+ ...").withStyle(MITooltips.DEFAULT_STYLE), x + i * 5, y + 5, -1, true);
                break;
            }
        }
    }
}
