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
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.client.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.client.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.client.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.guicomponents.FuelData;
import aztech.modern_industrialization.machines.guicomponents.SteamBoilerMultiblockGui;
import aztech.modern_industrialization.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

public class SteamBoilerMultiblockGuiClient extends GuiComponentClient<Unit, SteamBoilerMultiblockGui.Data> {
    public SteamBoilerMultiblockGuiClient(Unit params, SteamBoilerMultiblockGui.Data data) {
        super(params, data);
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    private static String formatAmount(long amount) {
        var amountText = TextHelper.getAmount(amount);
        return amountText.digit() + amountText.unit();
    }

    private static String formatAmount(double amount) {
        if (amount == Math.rint(amount)) {
            return formatAmount((long) amount);
        }
        return String.format("%.1f", amount);
    }

    private static Component steamPerUnitText(FuelData fuel, String steamAmount) {
        if (fuel.efficiency().isEmpty()) {
            var text = fuel.isFluid() ? MIText.SteamBoilerSteamPerFluidNoEfficiency : MIText.SteamBoilerSteamPerItemNoEfficiency;
            return text.text(steamAmount);
        }
        var text = fuel.isFluid() ? MIText.SteamBoilerSteamPerFluid : MIText.SteamBoilerSteamPerItem;
        return text.text(steamAmount, MITooltips.MULTIPLIER_PARSER.parse(fuel.efficiency().get()));
    }

    public class Renderer extends CraftingMultiblockGuiClient.BaseScreenRenderer implements ClientComponentRenderer {
        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
            Font font = Minecraft.getInstance().font;

            int deltaY = renderScreenAndStatus(data.isShapeValid(), false, font, guiGraphics, x, y);

            if (data.isShapeValid()) {
                deltaY += 11;

                drawClippedLine(guiGraphics, font, MIText.SteamBoilerSteamProduction.text(formatAmount(data.steamProduction())), x + 10, y + deltaY);
                deltaY += 11;
                drawClippedLine(guiGraphics, font, MIText.SteamBoilerMaxSteamProduction.text(formatAmount(data.maxSteamProduction())), x + 10,
                        y + deltaY);
                deltaY += 11;

                if (data.fuel().isPresent()) {
                    var fuel = data.fuel().get();
                    drawClippedLine(guiGraphics, font, MIText.MachineFuel.text(fuel.name()), x + 10, y + deltaY);
                    deltaY += 11;
                    var steamAmount = formatAmount((double) fuel.euPerUnit() / data.euPerSteamMb());
                    drawClippedLine(guiGraphics, font, steamPerUnitText(fuel, steamAmount), x + 10, y + deltaY);
                } else {
                    drawClippedLine(guiGraphics, font, MIText.MachineNoFuel.text(), x + 10, y + deltaY);
                }
            }
        }
    }
}
