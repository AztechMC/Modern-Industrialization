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

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;

class PriorityDisplay extends Button {
    private final Supplier<Integer> priorityGetter;
    private final Font textRenderer;

    public PriorityDisplay(int x, int y, int width, int height, Component message, OnTooltip tooltipSupplier, Supplier<Integer> priorityGetter,
            Font textRenderer) {
        super(x, y, width, height, message, button -> {
        }, tooltipSupplier);
        this.priorityGetter = priorityGetter;
        this.textRenderer = textRenderer;
        this.active = false;
    }

    @Override
    public Component getMessage() {
        return new TextComponent(Integer.toString(priorityGetter.get()));
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        FormattedCharSequence orderedText = getMessage().getVisualOrderText();
        textRenderer.draw(matrices, orderedText, (float) (this.x + this.width / 2 - textRenderer.width(orderedText) / 2),
                (float) (this.y + (this.height - 8) / 2), 4210752);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }
}
