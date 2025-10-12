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

package aztech.modern_industrialization.pipes.gui;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.client.DynamicTooltip;
import aztech.modern_industrialization.pipes.gui.iface.ConnectionTypeInterface;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

class ConnectionTypeButton extends Button {
    private final ConnectionTypeInterface connectionType;

    public ConnectionTypeButton(int x, int y, OnPress onPress, Supplier<List<Component>> tooltipSupplier, ConnectionTypeInterface connectionType) {
        super(x, y, 20, 20, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.connectionType = connectionType;
        setTooltip(new DynamicTooltip(tooltipSupplier));
    }

    @Override
    public Component getMessage() {
        return switch (connectionType.getConnectionType()) {
        case 0 -> MIText.PipeConnectionIn.text();
        case 1 -> MIText.PipeConnectionIO.text();
        case 2 -> MIText.PipeConnectionOut.text();
        default -> throw new IllegalArgumentException("Connection type must be either 0, 1 or 2");
        };
    }

    // Text is a bit too large, so override to avoid the default 2 pixel margin for the "scrolling" effect
    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
        renderScrollingString(guiGraphics, font, 0, color);
    }
}
