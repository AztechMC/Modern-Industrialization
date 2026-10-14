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

package aztech.modern_industrialization.client.machines.multiblocks;

import aztech.modern_industrialization.machines.multiblocks.MultiblockMemberTooltipData;
import aztech.modern_industrialization.util.TextHelper;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public record MultiblockMemberTooltip(MultiblockMemberTooltipData data) implements ClientTooltipComponent {
    private List<Component> getLines() {
        return TextHelper.getMultiblockMemberTooltip(data.registries(), data.member(), data.hatchFlags());
    }

    @Override
    public int getHeight() {
        return 18 + Math.max(0, getLines().size() - 1) * (Minecraft.getInstance().font.lineHeight + 1);
    }

    @Override
    public int getWidth(Font font) {
        var lines = getLines();
        int maxWidth = 18;
        for (int index = 0; index < lines.size(); index++) {
            var line = lines.get(index);
            int width = font.width(line) + (index == 0 ? 18 : 0);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    private ItemStack getItemToDisplay() {
        var items = data.member().getItemPreviewState(data.registries()).getItems();
        if (items.length == 0) {
            return ItemStack.EMPTY;
        }
        int itemIndex = (int) ((System.currentTimeMillis() / 1500L) % items.length);
        return items[itemIndex];
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        graphics.renderFakeItem(getItemToDisplay(), x, y);
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        var lines = getLines();
        for (int index = 0; index < lines.size(); index++) {
            var line = lines.get(index);
            int lineX = x + (index == 0 ? 18 : 0);
            int lineY = y + 4 + (index * (font.lineHeight + 1)) + (index > 0 ? 4 : 0);
            font.drawInBatch(line, lineX, lineY, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
    }
}
