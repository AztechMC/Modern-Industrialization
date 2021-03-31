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

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

/**
 * A helper for functionality commonly used by pipe screens.
 */
public abstract class AbstractPipeScreen<SH extends ScreenHandler> extends HandledScreen<SH> {
    public AbstractPipeScreen(SH handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    protected void addPriorityWidgets(int startX, int startY, Supplier<Integer> priorityGetter, Consumer<Integer> incrementPriority,
            ButtonWidget.TooltipSupplier priorityTooltip) {
        addPriorityButton(startX, startY, 20, 12, "--", -10, incrementPriority, priorityTooltip);
        addPriorityButton(startX + 22, startY, 12, 0, "-", -1, incrementPriority, priorityTooltip);
        addPriorityButton(startX + 62, startY, 12, 0, "+", +1, incrementPriority, priorityTooltip);
        addPriorityButton(startX + 76, startY, 20, 12, "++", +10, incrementPriority, priorityTooltip);
        addButton(new PriorityDisplay(startX + 34 + this.x, startY + this.y, 28, 12, new LiteralText(""), priorityTooltip, priorityGetter,
                textRenderer));
    }

    private void addPriorityButton(int x, int y, int width, int u, String text, int delta, Consumer<Integer> incrementPriority,
            ButtonWidget.TooltipSupplier priorityTooltip) {
        addButton(new PriorityButton(x + this.x, y + this.y, width, u, text, button -> incrementPriority.accept(delta), priorityTooltip));
    }
}
