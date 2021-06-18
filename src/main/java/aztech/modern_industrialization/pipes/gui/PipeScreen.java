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

import aztech.modern_industrialization.pipes.gui.iface.ConnectionTypeInterface;
import aztech.modern_industrialization.pipes.gui.iface.PriorityInterface;
import aztech.modern_industrialization.pipes.impl.PipePackets;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

/**
 * A helper for functionality commonly used by pipe screens.
 */
public abstract class PipeScreen<SH extends ScreenHandler> extends HandledScreen<SH> {
    @SuppressWarnings("AssignmentToSuperclassField")
    public PipeScreen(SH handler, PlayerInventory inventory, Text title, int backgroundHeight) {
        super(handler, inventory, title);

        this.backgroundHeight = backgroundHeight;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    protected abstract Identifier getBackgroundTexture();

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        this.client.getTextureManager().bindTexture(getBackgroundTexture());
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        super.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    protected void addPriorityWidgets(int startX, int startY, PriorityInterface priority, String tooltipType) {
        ButtonWidget.TooltipSupplier tooltip = (button, matrices, mouseX, mouseY) -> {
            List<Text> lines = new ArrayList<>();
            lines.add(new TranslatableText("text.modern_industrialization.priority_" + tooltipType, priority.getPriority()));
            lines.add(new TranslatableText("text.modern_industrialization.priority_" + tooltipType + "_help").setStyle(TextHelper.GRAY_TEXT));
            renderTooltip(matrices, lines, mouseX, mouseY);
        };
        addPriorityButton(startX, startY, 20, 12, "--", -10, priority, tooltip);
        addPriorityButton(startX + 22, startY, 12, 0, "-", -1, priority, tooltip);
        addPriorityButton(startX + 62, startY, 12, 0, "+", +1, priority, tooltip);
        addPriorityButton(startX + 76, startY, 20, 12, "++", +10, priority, tooltip);
        addDrawableChild(new PriorityDisplay(startX + 34 + this.x, startY + this.y, 28, 12, new LiteralText(""), tooltip, priority::getPriority,
                textRenderer));
    }

    private void addPriorityButton(int x, int y, int width, int u, String text, int delta, PriorityInterface priority,
            ButtonWidget.TooltipSupplier priorityTooltip) {
        addDrawableChild(new PriorityButton(x + this.x, y + this.y, width, u, text, button -> {
            priority.incrementPriority(delta);
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(handler.syncId);
            buf.writeInt(delta);
            ClientPlayNetworking.send(PipePackets.INCREMENT_PRIORITY, buf);
        }, priorityTooltip));
    }

    protected void addConnectionTypeButton(int x, int y, ConnectionTypeInterface connectionType) {
        addDrawableChild(new ConnectionTypeButton(x + this.x, y + this.y, widget -> {
            int newType = (connectionType.getConnectionType() + 1) % 3;
            connectionType.setConnectionType(newType);
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(handler.syncId);
            buf.writeInt(newType);
            ClientPlayNetworking.send(PipePackets.SET_CONNECTION_TYPE, buf);
        }, (button, matrices, mouseX, mouseY) -> {
            List<Text> lines = new ArrayList<>();
            lines.add(new TranslatableText("text.modern_industrialization.pipe_connection_tooltip_" + connectionType.getConnectionType()));
            lines.add(new TranslatableText("text.modern_industrialization.pipe_connection_help").setStyle(TextHelper.GRAY_TEXT));
            renderTooltip(matrices, lines, mouseX, mouseY);
        }, connectionType));
    }

}
