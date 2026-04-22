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

package aztech.modern_industrialization.client.pipes.gui;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.client.screen.MIContainerScreen;
import aztech.modern_industrialization.network.pipes.IncrementPriorityPacket;
import aztech.modern_industrialization.network.pipes.SetConnectionTypePacket;
import aztech.modern_industrialization.pipes.gui.iface.ConnectionTypeInterface;
import aztech.modern_industrialization.pipes.gui.iface.PriorityInterface;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * A helper for functionality commonly used by pipe screens.
 */
public abstract class PipeScreen<SH extends AbstractContainerMenu> extends MIContainerScreen<SH> {
    @SuppressWarnings("AssignmentToSuperclassField")
    public PipeScreen(SH handler, Inventory inventory, Component title, int imageHeight) {
        super(handler, inventory, title, DEFAULT_IMAGE_WIDTH, imageHeight);

        this.inventoryLabelY = this.imageHeight - 94;
    }

    protected abstract Identifier getBackgroundTexture();

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getBackgroundTexture(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    protected void addPriorityWidgets(int startX, int startY, PriorityInterface priority, int channel, Supplier<List<Component>> tooltip,
            BooleanSupplier isEnabled) {
        addPriorityButton(startX, startY, 20, 12, "--", -10, priority, channel, tooltip, isEnabled);
        addPriorityButton(startX + 22, startY, 12, 0, "-", -1, priority, channel, tooltip, isEnabled);
        addPriorityButton(startX + 62, startY, 12, 0, "+", +1, priority, channel, tooltip, isEnabled);
        addPriorityButton(startX + 76, startY, 20, 12, "++", +10, priority, channel, tooltip, isEnabled);
        addRenderableWidget(
                new PriorityDisplay(startX + 34 + this.leftPos, startY + this.topPos, 28, 12, Component.literal(""), tooltip,
                        () -> priority.getPriority(channel),
                        font, isEnabled));
    }

    private void addPriorityButton(int x, int y, int width, int u, String text, int delta, PriorityInterface priority,
            int channel, Supplier<List<Component>> priorityTooltip, BooleanSupplier isEnabled) {
        addRenderableWidget(new PriorityButton(x + this.leftPos, y + this.topPos, width, u, text, button -> {
            priority.incrementPriority(channel, delta);
            new IncrementPriorityPacket(menu.containerId, channel, delta).sendToServer();
        }, priorityTooltip, isEnabled));
    }

    protected int connectionTypeForward(ConnectionTypeInterface connectionType) {
        return (connectionType.getConnectionType() + 1) % 3;
    }

    protected int connectionTypeBackward(ConnectionTypeInterface connectionType) {
        return (connectionType.getConnectionType() + 2) % 3;
    }

    protected int connectionTypeNext(ConnectionTypeInterface connectionType) {
        if (!Minecraft.getInstance().hasShiftDown()) {
            return connectionTypeForward(connectionType);
        } else {
            return connectionTypeBackward(connectionType);
        }
    }

    protected static Component getConnectionTypeText(int connectionType) {
        return switch (connectionType) {
            case 0 -> MIText.PipeConnectionTooltipInsertOnly.text();
            case 1 -> MIText.PipeConnectionTooltipInsertOrExtract.text();
            case 2 -> MIText.PipeConnectionTooltipExtractOnly.text();
            default -> throw new IllegalArgumentException("Connection Type " + connectionType + " must be either 0, 1, 2");
        };
    }

    protected void addConnectionTypeButton(int x, int y, ConnectionTypeInterface connectionType) {
        addRenderableWidget(new ConnectionTypeButton(x + this.leftPos, y + this.topPos, widget -> {
            int newType = connectionTypeNext(connectionType);
            connectionType.setConnectionType(newType);
            new SetConnectionTypePacket(menu.containerId, newType).sendToServer();
        }, () -> {
            List<Component> lines = new ArrayList<>();
            Component component = getConnectionTypeText(connectionType.getConnectionType());

            lines.add(component);
            lines.add(MIText.PipeConnectionHelp.text().setStyle(TextHelper.GRAY_TEXT));
            return lines;
        }, connectionType));
    }
}
