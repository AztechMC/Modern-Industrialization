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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TemperatureBarClient implements GuiComponentClient {
    public final TemperatureBar.Parameters params;
    public int temperature;

    public TemperatureBarClient(RegistryFriendlyByteBuf buf) {
        this.params = new TemperatureBar.Parameters(buf.readInt(), buf.readInt(), buf.readInt());
        readCurrentData(buf);
    }

    @Override
    public void readCurrentData(RegistryFriendlyByteBuf buf) {
        this.temperature = buf.readInt();
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    public class Renderer implements ClientComponentRenderer {
        private final ResourceLocation TEXTURE = MI.id("textures/gui/efficiency_bar.png");
        private final int WIDTH = 100, HEIGHT = 2;

        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
            // background
            guiGraphics.blit(TEXTURE, x + params.renderX - 1, y + params.renderY - 1, 0, 2,
                    WIDTH + 2, HEIGHT + 2, 102, 6);
            int barPixels = (int) ((float) temperature / params.temperatureMax * WIDTH);
            guiGraphics.blit(TEXTURE, x + params.renderX, y + params.renderY, 0, 0, barPixels,
                    HEIGHT, 102, 6);
            guiGraphics.blit(MachineScreen.SLOT_ATLAS, x + params.renderX - 22, y + params.renderY + HEIGHT / 2 - 10, 144, 0, 20, 20);
        }

        @Override
        public void renderTooltip(MachineScreen screen, Font font, GuiGraphics guiGraphics, int x, int y, int cursorX, int cursorY) {
            if (aztech.modern_industrialization.util.RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT, cursorX - x,
                    cursorY - y)) {
                guiGraphics.renderTooltip(font, MIText.Temperature.text(temperature), cursorX, cursorY);
            }
        }
    }
}
