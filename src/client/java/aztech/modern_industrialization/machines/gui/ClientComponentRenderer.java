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

package aztech.modern_industrialization.machines.gui;

import aztech.modern_industrialization.util.Rectangle;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Renderer for the shared data of a client component on the machine screen.
 */
public interface ClientComponentRenderer {
    default void addButtons(ButtonContainer container) {}

    void renderBackground(GuiGraphics guiGraphics, int leftPos, int topPos);

    default void renderTooltip(MachineScreen screen, Font font, GuiGraphics guiGraphics, int leftPos, int topPos, int cursorX, int cursorY) {}

    default void addExtraBoxes(List<Rectangle> rectangles, int leftPos, int topPos) {}

    interface ButtonContainer {
        /**
         * @param u               Texture button u.
         * @param pressAction     A function that is called with the syncId every time
         *                        the button is pressed.
         * @param tooltipSupplier A function that is called every time the tooltip must
         *                        be rendered.
         * @param isPressed       A function that returns true if the button is
         *                        currently pressed.
         * @return
         */
        MachineScreen.MachineButton addButton(int u, Consumer<Integer> pressAction, Supplier<List<Component>> tooltipSupplier,
                Supplier<Boolean> isPressed);

        MachineScreen.MachineButton addButton(int posX, int posY, int width, int height, Consumer<Integer> pressAction,
                Supplier<List<Component>> tooltipSupplier,
                CustomButtonRenderer renderer, Supplier<Boolean> isButtonPresent);

        default MachineScreen.MachineButton addButton(int posX, int posY, int width, int height, Consumer<Integer> pressAction,
                Supplier<List<Component>> tooltipSupplier, CustomButtonRenderer renderer) {
            return addButton(posX, posY, width, height, pressAction, tooltipSupplier, renderer, () -> true);
        }
    }

    @FunctionalInterface
    interface CustomButtonRenderer {
        void renderButton(MachineScreen screen, MachineScreen.MachineButton button, GuiGraphics guiGraphics, int mouseX, int mouseY,
                float partialTicks);
    }
}
