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
import aztech.modern_industrialization.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class CraftingMultiblockGuiClient implements GuiComponentClient {
    public boolean isShapeValid;
    boolean hasActiveRecipe;
    float progress;
    int efficiencyTicks;
    int maxEfficiencyTicks;
    long currentRecipeEu;
    long baseRecipeEu;
    int remainingOverclockTicks;

    public CraftingMultiblockGuiClient(RegistryFriendlyByteBuf buf) {
        readCurrentData(buf);
    }

    @Override
    public void readCurrentData(RegistryFriendlyByteBuf buf) {
        isShapeValid = buf.readBoolean();
        if (isShapeValid) {
            hasActiveRecipe = buf.readBoolean();
            if (hasActiveRecipe) {
                progress = buf.readFloat();
                efficiencyTicks = buf.readInt();
                maxEfficiencyTicks = buf.readInt();
                currentRecipeEu = buf.readLong();
                baseRecipeEu = buf.readLong();
            }
        }
        remainingOverclockTicks = buf.readVarInt();
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    public class Renderer implements ClientComponentRenderer {
        private static final ResourceLocation TEXTURE = MI.id("textures/gui/container/multiblock_info.png");

        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {

            Minecraft minecraftClient = Minecraft.getInstance();
            guiGraphics.blit(TEXTURE, x + CraftingMultiblockGui.X, y + CraftingMultiblockGui.Y, 0, 0,
                    CraftingMultiblockGui.W, CraftingMultiblockGui.H, CraftingMultiblockGui.W, CraftingMultiblockGui.H);
            Font font = minecraftClient.font;

            int deltaY = 23;

            guiGraphics.drawString(font, isShapeValid ? MIText.MultiblockShapeValid.text() : MIText.MultiblockShapeInvalid.text(), x + 10, y + deltaY,
                    isShapeValid ? 0xFFFFFF : 0xFF0000, false);
            deltaY += 11;

            if (isShapeValid) {
                guiGraphics.drawString(font, MIText.MultiblockStatusActive.text(), x + 10, y + deltaY, 0xFFFFFF, false);
                deltaY += 11;

                if (hasActiveRecipe) {
                    guiGraphics.drawString(font, MIText.Progress.text(String.format("%.1f", progress * 100) + " %"), x + 10, y + deltaY, 0xFFFFFF,
                            false);
                    deltaY += 11;

                    if (efficiencyTicks != 0 || maxEfficiencyTicks != 0) {
                        guiGraphics.drawString(font, MIText.EfficiencyTicks.text(efficiencyTicks, maxEfficiencyTicks), x + 10, y + deltaY, 0xFFFFFF,
                                false);
                        deltaY += 11;
                    }

                    guiGraphics.drawString(font, MIText.BaseEuRecipe.text(TextHelper.getEuTextTick(baseRecipeEu)), x + 10, y + deltaY, 0xFFFFFF,
                            false);
                    deltaY += 11;

                    guiGraphics.drawString(font, MIText.CurrentEuRecipe.text(TextHelper.getEuTextTick(currentRecipeEu)), x + 10, y + deltaY, 0xFFFFFF,
                            false);
                    deltaY += 11;
                }
            }

            if (remainingOverclockTicks > 0) {
                guiGraphics.drawString(font, GunpowderOverclockGuiClient.Renderer.formatOverclock(remainingOverclockTicks), x + 10, y + deltaY,
                        0xFFFFFF, false);
            }
        }

    }
}
