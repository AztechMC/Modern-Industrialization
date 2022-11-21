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
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.FriendlyByteBuf;

public class FluidGUIComponentClient implements GuiComponentClient {

    FluidGUIComponent.Data fluidData;

    public FluidGUIComponentClient(FriendlyByteBuf buf) {
        readCurrentData(buf);
    }

    @Override
    public void readCurrentData(FriendlyByteBuf buf) {
        fluidData = new FluidGUIComponent.Data(FluidVariant.fromPacket(buf), buf.readLong(), buf.readLong());
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    public class Renderer implements ClientComponentRenderer {

        private static final int posX = 70, posY = 12;

        @Override
        public void renderBackground(net.minecraft.client.gui.GuiComponent helper, PoseStack matrices, int x, int y) {
            FluidVariant fluid = fluidData.fluid();
            float fracFull = (float) fluidData.amount() / fluidData.capacity();

            RenderSystem.setShaderTexture(0, MachineScreen.SLOT_ATLAS);
            helper.blit(matrices, x + posX, y + posY, 92, 38, 46, 62);

            if (!fluid.isBlank()) {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 3; j++) {
                        float localFullness = Math.min(Math.max(3 * fracFull - (2 - j), 0), 1);
                        RenderHelper.drawFluidInGui(matrices, fluid, x + posX + 7 + i * 16, y + posY + 7 + j * 16 + (1 - localFullness) * 16, 16,
                                localFullness);
                    }
                }
            }
            RenderSystem.setShaderTexture(0, MachineScreen.SLOT_ATLAS);
            helper.blit(matrices, x + posX + 7, y + posY + 7, 60, 38, 32, 48);

        }

        @Override
        public void renderTooltip(MachineScreen screen, PoseStack matrices, int x, int y, int cursorX, int cursorY) {
            if (RenderHelper.isPointWithinRectangle(posX + 7, posY + 7, 32, 48, cursorX - x, cursorY - y)) {
                screen.renderComponentTooltip(matrices,
                        FluidHelper.getTooltipForFluidStorage(fluidData.fluid(), fluidData.amount(), fluidData.capacity()),
                        cursorX, cursorY);
            }
        }
    }

}
