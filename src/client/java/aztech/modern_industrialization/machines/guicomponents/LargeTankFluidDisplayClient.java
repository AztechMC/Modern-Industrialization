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

import aztech.modern_industrialization.machines.blockentities.multiblocks.LargeTankMultiblockBlockEntity;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;

public class LargeTankFluidDisplayClient implements GuiComponentClient {

    LargeTankFluidDisplay.Data fluidData;

    public LargeTankFluidDisplayClient(FriendlyByteBuf buf) {
        readCurrentData(buf);
    }

    @Override
    public void readCurrentData(FriendlyByteBuf buf) {
        fluidData = new LargeTankFluidDisplay.Data(FluidVariant.fromPacket(buf), buf.readLong(), buf.readLong());
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new ClientComponentRenderer() {
            private static final int posX = 70, posY = 12;

            @Override
            public void renderBackground(GuiGraphics guiGraphics, int leftPos, int topPos) {
                FluidVariant fluid = fluidData.fluid();
                float fracFull = (float) fluidData.amount() / fluidData.capacity();

                guiGraphics.blit(MachineScreen.SLOT_ATLAS, leftPos + posX, topPos + posY, 92, 38, 46, 62);

                RenderSystem.disableBlend();
                if (!fluid.isBlank()) {
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 3; j++) {
                            float localFullness = Math.min(Math.max(3 * fracFull - (2 - j), 0), 1);
                            RenderHelper.drawFluidInGui(guiGraphics, fluid, leftPos + posX + 7 + i * 16,
                                    topPos + posY + 7 + j * 16 + (1 - localFullness) * 16, 16, localFullness);
                        }
                    }
                }
                RenderSystem.enableBlend();

                guiGraphics.blit(MachineScreen.SLOT_ATLAS, leftPos + posX + 7, topPos + posY + 7, 60, 38, 32, 48);

                // A bit hacky: draw the capacity corresponding to the shape in the shape selection GUI if it's open. ;)
                var shapeSelection = machineScreen.getMenu().getComponent(ShapeSelectionClient.class);
                var renderer = Objects.requireNonNull(shapeSelection).getRenderer();
                if (renderer.isPanelOpen) {
                    var shapePanelBox = renderer.getBox(leftPos, topPos);
                    int[] selectedShape = shapeSelection.currentData;
                    long capacity = LargeTankMultiblockBlockEntity.getCapacityFromComponents(
                            selectedShape[0], selectedShape[1], selectedShape[2]);
                    var capacityText = FluidHelper.getFluidAmountLarge(capacity);

                    guiGraphics.drawString(Minecraft.getInstance().font, capacityText, shapePanelBox.x() + 14, shapePanelBox.y() + 14, 0x404040,
                            false);
                }
            }

            @Override
            public void renderTooltip(MachineScreen screen, Font font, GuiGraphics guiGraphics, int x, int y, int cursorX, int cursorY) {
                if (RenderHelper.isPointWithinRectangle(posX + 7, posY + 7, 32, 48, cursorX - x, cursorY - y)) {
                    guiGraphics.renderTooltip(font,
                            FluidHelper.getTooltipForFluidStorage(fluidData.fluid(), fluidData.amount(), fluidData.capacity()),
                            Optional.empty(),
                            cursorX, cursorY);
                }
            }
        };
    }

}
