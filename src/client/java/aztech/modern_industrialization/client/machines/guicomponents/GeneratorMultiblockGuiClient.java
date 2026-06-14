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

package aztech.modern_industrialization.client.machines.guicomponents;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.client.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.client.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.client.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.guicomponents.GeneratorMultiblockGui;
import aztech.modern_industrialization.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Unit;

public class GeneratorMultiblockGuiClient extends GuiComponentClient<Unit, GeneratorMultiblockGui.Data> {
    public GeneratorMultiblockGuiClient(Unit params, GeneratorMultiblockGui.Data data) {
        super(params, data);
    }

    public boolean isShapeValid() {
        return data.isShapeValid();
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    public class Renderer extends CraftingMultiblockGuiClient.BaseScreenRenderer implements ClientComponentRenderer {
        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
            Font font = Minecraft.getInstance().font;

            int deltaY = renderScreenAndStatus(data.isShapeValid(), font, guiGraphics, x, y);

            if (data.isShapeValid()) {
                deltaY += 11;

                guiGraphics.drawString(font, MIText.GeneratorCurrentEu.text(TextHelper.getEuTextTick(data.currentEuGeneration())), x + 10, y + deltaY, 0xFFFFFF,
                        false);
                deltaY += 11;

                guiGraphics.drawString(font, MIText.GeneratorMaxEu.text(TextHelper.getEuTextTick(data.maxEuGeneration())), x + 10, y + deltaY, 0xFFFFFF,
                        false);
                deltaY += 11;
            }
        }
    }
}
