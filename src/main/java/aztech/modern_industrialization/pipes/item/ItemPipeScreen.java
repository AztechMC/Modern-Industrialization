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
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.pipes.gui.PipeGuiHelper;
import aztech.modern_industrialization.pipes.gui.PipeScreen;
import aztech.modern_industrialization.pipes.impl.PipePackets;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemPipeScreen extends PipeScreen<ItemPipeScreenHandler> {
    private static final ResourceLocation TEXTURE = new MIIdentifier("textures/gui/pipe/item.png");
    private static final Style SECONDARY_INFO = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);

    public ItemPipeScreen(ItemPipeScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, ItemPipeScreenHandler.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new WhitelistButton(this.leftPos, this.topPos, widget -> {
            boolean newWhitelist = !menu.pipeInterface.isWhitelist();
            menu.pipeInterface.setWhitelist(newWhitelist);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(menu.containerId);
            buf.writeBoolean(newWhitelist);
            ClientPlayNetworking.send(PipePackets.SET_ITEM_WHITELIST, buf);
        }, (button, matrices, mouseX, mouseY) -> {
            List<Component> lines = new ArrayList<>();
            if (menu.pipeInterface.isWhitelist()) {
                lines.add(MIText.Whitelist.text());
                lines.add(MIText.ClickToToggleBlacklist.text().setStyle(SECONDARY_INFO));
            } else {
                lines.add(MIText.Blacklist.text());
                lines.add(MIText.ClickToToggleWhitelist.text().setStyle(SECONDARY_INFO));
            }
            renderComponentTooltip(matrices, lines, mouseX, mouseY);
        }));
        addConnectionTypeButton(148, 22, menu.pipeInterface);
        addPriorityWidgets(35, 72, menu.pipeInterface, "insert", 0);
        addPriorityWidgets(35, 86, menu.pipeInterface, "extract", 1);
    }

    /**
     * @reason Override the title to add a warning when the slot is empty
     */
    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        Component title = this.title;
        if (menu.pipeInterface.isWhitelist() && menu.pipeInterface.isFilterEmpty()) {
            title = title.copy().append(new TextComponent(" "))
                    .append(MIText.EmptyWhitelistWarning.text().setStyle(TextHelper.WARNING_TEXT));
        }
        this.font.draw(matrices, title, (float) this.titleLabelX, (float) this.titleLabelY, 0x404040);
        this.font.draw(matrices, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 0x404040);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }

    private class WhitelistButton extends Button {
        public WhitelistButton(int i, int j, OnPress onPress, OnTooltip tooltipSupplier) {
            super(i + 148, j + 44, 20, 20, new TextComponent("test!"), onPress, tooltipSupplier);
        }

        @Override
        public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, PipeGuiHelper.BUTTON_TEXTURE);
            int u = menu.pipeInterface.isWhitelist() ? 0 : 20;
            int v = this.isHoveredOrFocused() ? 20 : 0;

            RenderSystem.enableDepthTest();
            blit(matrices, this.x, this.y, u, v, this.width, this.height);
            if (this.isHoveredOrFocused()) {
                this.renderToolTip(matrices, mouseX, mouseY);
            }
        }
    }
}
