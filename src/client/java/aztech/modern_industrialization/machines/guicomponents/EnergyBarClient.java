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
package aztech.modern_industrialization.machines.guicomponents;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import java.util.Collections;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class EnergyBarClient implements GuiComponentClient {
    final EnergyBar.Parameters params;
    long eu, maxEu;

    public EnergyBarClient(RegistryFriendlyByteBuf buf) {
        this.params = new EnergyBar.Parameters(buf.readInt(), buf.readInt());
        readCurrentData(buf);
    }

    @Override
    public void readCurrentData(RegistryFriendlyByteBuf buf) {
        eu = buf.readLong();
        maxEu = buf.readLong();
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    public class Renderer implements ClientComponentRenderer {
        public static final int WIDTH = 13;
        public static final int HEIGHT = 18;

        public static void renderEnergy(GuiGraphics guiGraphics, int px, int py, float fill) {
            guiGraphics.blit(MachineScreen.SLOT_ATLAS, px, py, 230, 0, WIDTH, HEIGHT);
            int fillPixels = (int) (fill * HEIGHT * 0.9 + HEIGHT * 0.1);
            if (fill > 0.95)
                fillPixels = HEIGHT;
            guiGraphics.blit(MachineScreen.SLOT_ATLAS, px, py + HEIGHT - fillPixels, 243, HEIGHT - fillPixels, WIDTH, fillPixels);
        }

        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
            renderEnergy(guiGraphics, x + params.renderX, y + params.renderY, (float) eu / maxEu);
        }

        @Override
        public void renderTooltip(MachineScreen screen, Font font, GuiGraphics guiGraphics, int x, int y, int cursorX, int cursorY) {
            if (RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT, cursorX - x, cursorY - y)) {
                Component tooltip;
                if (Screen.hasShiftDown()) {
                    tooltip = MIText.EuMaxed.text(eu, maxEu, "");
                } else {
                    TextHelper.MaxedAmount maxedAmount = TextHelper.getMaxedAmount(eu, maxEu);
                    tooltip = MIText.EuMaxed.text(maxedAmount.digit(), maxedAmount.maxDigit(), maxedAmount.unit());
                }
                guiGraphics.renderTooltip(font, Collections.singletonList(tooltip), Optional.empty(), cursorX, cursorY);
            }
        }
    }
}
