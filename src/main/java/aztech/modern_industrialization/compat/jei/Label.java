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
package aztech.modern_industrialization.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class Label implements Widget {
    public final float x;
    public final float y;
    public final Component text;
    private final int width;
    private final Font font;
    @Nullable
    public Component tooltip;
    public int color = -1;
    public boolean shadow = true;
    private LabelAlignment align = LabelAlignment.CENTER;

    public Label(float x, float y, Component text) {
        this.x = x;
        this.y = y;
        this.text = text;
        font = Minecraft.getInstance().font;
        this.width = font.width(text);
    }

    @Override
    public void draw(PoseStack stack) {
        float alignedX = getAlignedX();

        if (shadow) {
            font.drawShadow(stack, text, alignedX, y, color);
        } else {
            font.draw(stack, text, alignedX, y, color);
        }
    }

    public Label alignLeft() {
        align = LabelAlignment.LEFT;
        return this;
    }

    public Label alignRight() {
        align = LabelAlignment.RIGHT;
        return this;
    }

    public Label tooltip(Component text) {
        this.tooltip = text;
        return this;
    }

    public Label noShadow() {
        shadow = false;
        return this;
    }

    @Override
    public boolean hitTest(double x, double y) {
        var alignedX = getAlignedX();
        return x >= alignedX && x < alignedX + width
                && y >= this.y && y < this.y + font.lineHeight;
    }

    @Override
    public List<Component> getTooltipLines() {
        if (tooltip != null) {
            return List.of(tooltip);
        }
        return List.of();
    }

    private float getAlignedX() {
        return switch (align) {
        case LEFT -> x;
        case CENTER -> x - width / 2f;
        case RIGHT -> x - width;
        };
    }

    private enum LabelAlignment {
        LEFT,
        CENTER,
        RIGHT
    }
}
