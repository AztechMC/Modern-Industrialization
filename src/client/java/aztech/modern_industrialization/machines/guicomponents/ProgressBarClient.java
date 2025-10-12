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

import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ProgressBarClient implements GuiComponentClient {
    public final ProgressBar.Parameters params;
    public float progress;

    public ProgressBarClient(RegistryFriendlyByteBuf buf) {
        this.params = new ProgressBar.Parameters(buf.readInt(), buf.readInt(), buf.readUtf(), buf.readBoolean());
        readCurrentData(buf);
    }

    public static void renderProgress(GuiGraphics guiGraphics, int x, int y, ProgressBar.Parameters params, float progress) {
        // background
        guiGraphics.blit(params.getTextureId(), x + params.renderX, y + params.renderY, 0, 0, 20, 20, 20, 40);
        // foreground
        int foregroundPixels = (int) (progress * 20);
        if (foregroundPixels > 0) {
            if (!params.isVertical) {
                guiGraphics.blit(params.getTextureId(), x + params.renderX, y + params.renderY, 0, 20, foregroundPixels, 20,
                        20, 40);
            } else {
                guiGraphics.blit(params.getTextureId(), x + params.renderX, y + params.renderY + 20 - foregroundPixels, 0,
                        40 - foregroundPixels, 20, foregroundPixels, 20, 40);
            }
        }
    }

    @Override
    public void readCurrentData(RegistryFriendlyByteBuf buf) {
        this.progress = buf.readFloat();
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    public class Renderer implements ClientComponentRenderer {
        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
            renderProgress(guiGraphics, x, y, params, progress);
        }
    }
}
