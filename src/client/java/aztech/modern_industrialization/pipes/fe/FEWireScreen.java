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
package aztech.modern_industrialization.pipes.fe;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.pipes.gui.PipeScreen;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FEWireScreen extends PipeScreen<FEWireScreenHandler> {
    private static final ResourceLocation TEXTURE = MI.id("textures/gui/pipe/fe.png");

    public FEWireScreen(FEWireScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, FEWireScreenHandler.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        addConnectionTypeButton(148, 22, menu.iface);

        addPriorityWidgets(33, 42, menu.iface, 0, () -> {
            List<Component> lines = new ArrayList<>();

            MIText priorityText = MIText.PriorityTransfer;
            lines.add(priorityText.text(menu.iface.getPriority(0)));

            MIText priorityTextHelp = MIText.PriorityTransferWireHelp;
            lines.add(priorityTextHelp.text().setStyle(TextHelper.GRAY_TEXT));

            return lines;
        }, () -> true);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        if (hoveredSlot != null && hoveredSlot instanceof FEWireScreenHandler.UpgradeSlot && !hoveredSlot.hasItem()) {
            List<Component> lines = new ArrayList<>();
            lines.add(MIText.PutBatteryToUpgrade.text());
            if (menu.iface.getConnectionType() == 0) {
                lines.add(MIText.PriorityNotApplicable.text(
                        MIText.PipeConnectionTooltipInsertOnly.text().setStyle(MITooltips.HIGHLIGHT_STYLE),
                        MIText.PipeConnectionIn.text().setStyle(MITooltips.HIGHLIGHT_STYLE))
                        .setStyle(TextHelper.GRAY_TEXT));
            }
            guiGraphics.renderTooltip(font, RenderHelper.splitTooltip(lines), x, y);
        }
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}
