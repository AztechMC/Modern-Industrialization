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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.util.RenderHelper;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RecipeEfficiencyBarClient implements GuiComponentClient {
    final RecipeEfficiencyBar.Parameters params;
    boolean hasActiveRecipe;
    int efficiencyTicks;
    int maxEfficiencyTicks;
    long currentRecipeEu;
    long baseRecipeEu;
    long maxRecipeEu;

    public RecipeEfficiencyBarClient(RegistryFriendlyByteBuf buf) {
        this.params = new RecipeEfficiencyBar.Parameters(buf.readInt(), buf.readInt());
        readCurrentData(buf);
    }

    @Override
    public void readCurrentData(RegistryFriendlyByteBuf buf) {
        hasActiveRecipe = buf.readBoolean();
        if (hasActiveRecipe) {
            efficiencyTicks = buf.readInt();
            maxEfficiencyTicks = buf.readInt();
            currentRecipeEu = buf.readLong();
            baseRecipeEu = buf.readLong();
        }
        maxRecipeEu = buf.readLong();
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    private static final ResourceLocation TEXTURE = new MIIdentifier("textures/gui/efficiency_bar.png");
    private static final int WIDTH = 100, HEIGHT = 2;

    public class Renderer implements ClientComponentRenderer {
        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
            guiGraphics.blit(TEXTURE, x + params.renderX - 1, y + params.renderY - 1, 0, 2, WIDTH + 2, HEIGHT + 2, 102, 6);
            if (hasActiveRecipe) {
                int barPixels = (int) ((float) efficiencyTicks / maxEfficiencyTicks * WIDTH);
                guiGraphics.blit(TEXTURE, x + params.renderX, y + params.renderY, 0, 0, barPixels, HEIGHT, 102, 6);
            }
        }

        @Override
        public void renderTooltip(MachineScreen screen, Font font, GuiGraphics guiGraphics, int x, int y, int cursorX, int cursorY) {
            if (RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT, cursorX - x, cursorY - y)) {
                List<Component> tooltip = new ArrayList<>();
                if (hasActiveRecipe) {
                    DecimalFormat factorFormat = new DecimalFormat("#.#");

                    tooltip.add(MIText.EfficiencyTicks.text(efficiencyTicks, maxEfficiencyTicks));
                    tooltip.add(MIText.EfficiencyFactor.text(factorFormat.format((double) currentRecipeEu / baseRecipeEu)));
                    tooltip.add(MIText.EfficiencyEu.text(currentRecipeEu));

                } else {
                    tooltip.add(MIText.EfficiencyDefaultMessage.text());
                }

                tooltip.add(MIText.EfficiencyMaxOverclock.text(maxRecipeEu));

                guiGraphics.renderTooltip(font, tooltip, Optional.empty(), cursorX, cursorY);
            }
        }
    }
}
