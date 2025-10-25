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

package aztech.modern_industrialization.pipes.gui;

import aztech.modern_industrialization.client.DynamicTooltip;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

class PriorityDisplay extends AbstractWidget {
    private final Supplier<Integer> priorityGetter;
    private final Font textRenderer;
    private final BooleanSupplier isEnabled;

    public PriorityDisplay(int x, int y, int width, int height, Component message, Supplier<List<Component>> tooltipSupplier,
            Supplier<Integer> priorityGetter,
            Font textRenderer,
            BooleanSupplier isEnabled) {
        super(x, y, width, height, message);
        setTooltip(new DynamicTooltip(tooltipSupplier));
        this.priorityGetter = priorityGetter;
        this.textRenderer = textRenderer;
        this.isEnabled = isEnabled;
        this.active = false;
    }

    @Override
    public Component getMessage() {
        return Component.literal(Integer.toString(priorityGetter.get()));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        FormattedCharSequence orderedText = getMessage().getVisualOrderText();
        guiGraphics.drawString(textRenderer, orderedText, this.getX() + this.width / 2 - textRenderer.width(orderedText) / 2,
                this.getY() + (this.height - 8) / 2, isEnabled.getAsBoolean() ? 0x404040 : 0x707070, false);
    }
}
