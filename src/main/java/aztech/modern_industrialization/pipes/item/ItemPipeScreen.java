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
package aztech.modern_industrialization.pipes.item;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.gui.PipeGuiHelper;
import aztech.modern_industrialization.pipes.gui.PipeScreen;
import aztech.modern_industrialization.pipes.impl.PipePackets;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

public class ItemPipeScreen extends PipeScreen<ItemPipeScreenHandler> {
    private static final Identifier TEXTURE = new MIIdentifier("textures/gui/pipe/item.png");
    private static final Style SECONDARY_INFO = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);

    public ItemPipeScreen(ItemPipeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, ItemPipeScreenHandler.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        addButton(new WhitelistButton(this.x, this.y, widget -> {
            boolean newWhitelist = !handler.pipeInterface.isWhitelist();
            handler.pipeInterface.setWhitelist(newWhitelist);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(handler.syncId);
            buf.writeBoolean(newWhitelist);
            ClientPlayNetworking.send(PipePackets.SET_ITEM_WHITELIST, buf);
        }, (button, matrices, mouseX, mouseY) -> {
            List<Text> lines = new ArrayList<>();
            if (handler.pipeInterface.isWhitelist()) {
                lines.add(new TranslatableText("text.modern_industrialization.whitelist"));
                lines.add(new TranslatableText("text.modern_industrialization.click_to_toggle_blacklist").setStyle(SECONDARY_INFO));
            } else {
                lines.add(new TranslatableText("text.modern_industrialization.blacklist"));
                lines.add(new TranslatableText("text.modern_industrialization.click_to_toggle_whitelist").setStyle(SECONDARY_INFO));
            }
            renderTooltip(matrices, lines, mouseX, mouseY);
        }));
        addConnectionTypeButton(148, 22, handler.pipeInterface);
        addPriorityWidgets(15, 72, handler.pipeInterface, "insert");
    }

    @Override
    protected Identifier getBackgroundTexture() {
        return TEXTURE;
    }

    private class WhitelistButton extends ButtonWidget {
        public WhitelistButton(int i, int j, ButtonWidget.PressAction onPress, ButtonWidget.TooltipSupplier tooltipSupplier) {
            super(i + 148, j + 44, 20, 20, new LiteralText("test!"), onPress, tooltipSupplier);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getTextureManager().bindTexture(PipeGuiHelper.BUTTON_TEXTURE);
            int u = handler.pipeInterface.isWhitelist() ? 0 : 20;
            int v = this.isHovered() ? 20 : 0;

            RenderSystem.enableDepthTest();
            drawTexture(matrices, this.x, this.y, u, v, this.width, this.height);
            if (this.isHovered()) {
                this.renderToolTip(matrices, mouseX, mouseY);
            }
        }
    }
}
