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

import aztech.modern_industrialization.machines.MachineScreenHandlers;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

/**
 * Renderer for the shared data of a client component on the machine screen.
 */
public interface ClientComponentRenderer {
    default void addButtons(ButtonContainer container) {
    }

    void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y);

    default void renderTooltip(MachineScreenHandlers.ClientScreen screen, MatrixStack matrices, int x, int y, int cursorX, int cursorY) {
    }

    interface ButtonContainer {
        /**
         * @param u               Texture button u.
         * @param message         No idea.
         * @param pressAction     A function that is called with the syncId every time
         *                        the button is pressed.
         * @param tooltipSupplier A function that is called every time the tooltip must
         *                        be rendered.
         * @param isPressed       A function that returns true if the button is
         *                        currently pressed.
         */
        void addButton(int u, Text message, Consumer<Integer> pressAction, Supplier<List<Text>> tooltipSupplier, Supplier<Boolean> isPressed);

        void addButton(int posX, int posY, int width, int height, Text message, Consumer<Integer> pressAction, Supplier<List<Text>> tooltipSupplier,
                CustomButtonRenderer renderer);

    }

    @FunctionalInterface
    interface CustomButtonRenderer {
        void renderButton(MachineScreenHandlers.ClientScreen.MachineButton button, MatrixStack matrices, int mouseX, int mouseY, float delta);
    }

}
