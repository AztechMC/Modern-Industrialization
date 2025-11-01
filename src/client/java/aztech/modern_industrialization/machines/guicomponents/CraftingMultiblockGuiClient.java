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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

public class CraftingMultiblockGuiClient extends GuiComponentClient<Unit, CraftingMultiblockGui.Data> {
    public CraftingMultiblockGuiClient(Unit params, CraftingMultiblockGui.Data data) {
        super(params, data);
    }

    public boolean isShapeValid() {
        return data.isShapeValid();
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    public static class BaseScreenRenderer {
        private static final ResourceLocation TEXTURE = MI.id("textures/gui/container/multiblock_info.png");

        /**
         * Returns {@code deltaY}.
         */
        public int renderScreenAndStatus(boolean shapeValid, Font font, GuiGraphics guiGraphics, int x, int y) {
            guiGraphics.blit(TEXTURE, x + CraftingMultiblockGui.X, y + CraftingMultiblockGui.Y, 0, 0,
                    CraftingMultiblockGui.W, CraftingMultiblockGui.H, CraftingMultiblockGui.W, CraftingMultiblockGui.H);

            int deltaY = 23;

            guiGraphics.drawString(font,
                    shapeValid ? MIText.MultiblockShapeValid.text() : MIText.MultiblockShapeInvalid.text(),
                    x + 10, y + deltaY,
                    shapeValid ? 0xFFFFFF : 0xFF0000, false);
            deltaY += 11;

            if (shapeValid) {
                guiGraphics.drawString(font, MIText.MultiblockStatusActive.text(), x + 10, y + deltaY, 0xFFFFFF, false);
                deltaY += 11;
            }

            return deltaY;
        }
    }

    public class Renderer extends BaseScreenRenderer implements ClientComponentRenderer {
        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
            Font font = Minecraft.getInstance().font;

            int deltaY = renderScreenAndStatus(data.isShapeValid(), font, guiGraphics, x, y);

            if (data.isShapeValid()) {
                if (data.activeRecipe().isPresent()) {
                    var recipe = data.activeRecipe().get();

                    guiGraphics.drawString(font, MIText.Progress.text(String.format("%.1f", recipe.progress() * 100) + " %"), x + 10, y + deltaY, 0xFFFFFF,
                            false);
                    deltaY += 11;

                    if (recipe.efficiencyTicks() != 0 || recipe.maxEfficiencyTicks() != 0) {
                        guiGraphics.drawString(font, MIText.EfficiencyTicks.text(recipe.efficiencyTicks(), recipe.maxEfficiencyTicks()), x + 10, y + deltaY, 0xFFFFFF,
                                false);
                        deltaY += 11;
                    }

                    guiGraphics.drawString(font, MIText.BaseEuRecipe.text(TextHelper.getEuTextTick(recipe.baseRecipeEu())), x + 10, y + deltaY, 0xFFFFFF,
                            false);
                    deltaY += 11;

                    guiGraphics.drawString(font, MIText.CurrentEuRecipe.text(TextHelper.getEuTextTick(recipe.currentRecipeEu())), x + 10, y + deltaY, 0xFFFFFF,
                            false);
                    deltaY += 11;
                }
            }

            if (data.remainingOverclockTicks() > 0) {
                guiGraphics.drawString(font, GunpowderOverclockGuiClient.Renderer.formatOverclock(data.remainingOverclockTicks()), x + 10, y + deltaY,
                        0xFFFFFF, false);
            }
        }
    }
}
