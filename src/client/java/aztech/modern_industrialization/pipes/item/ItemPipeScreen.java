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
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.client.DynamicTooltip;
import aztech.modern_industrialization.network.pipes.SetItemWhitelistPacket;
import aztech.modern_industrialization.pipes.gui.PipeGuiHelper;
import aztech.modern_industrialization.pipes.gui.PipeScreen;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
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
            new SetItemWhitelistPacket(menu.containerId, newWhitelist).sendToServer();
        }, () -> {
            List<Component> lines = new ArrayList<>();
            if (menu.pipeInterface.isWhitelist()) {
                lines.add(MIText.Whitelist.text());
                lines.add(MIText.ClickToToggleBlacklist.text().setStyle(SECONDARY_INFO));
            } else {
                lines.add(MIText.Blacklist.text());
                lines.add(MIText.ClickToToggleWhitelist.text().setStyle(SECONDARY_INFO));
            }
            return lines;
        }));
        addConnectionTypeButton(148, 22, menu.pipeInterface);
        addPriorityWidgets(35, 72, menu.pipeInterface, "insert", 0);
        addPriorityWidgets(35, 86, menu.pipeInterface, "extract", 1);
    }

    /**
     * @reason Override the title to add a warning when the slot is empty
     */
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component title = this.title;
        if (menu.pipeInterface.isWhitelist() && menu.pipeInterface.isFilterEmpty()) {
            title = title.copy().append(Component.literal(" "))
                    .append(MIText.EmptyWhitelistWarning.text().setStyle(TextHelper.WARNING_TEXT));
        }
        guiGraphics.drawString(font, title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        if (this.hoveredSlot != null && this.hoveredSlot instanceof ItemPipeScreenHandler.UpgradeSlot && !this.hoveredSlot.hasItem()) {
            guiGraphics.renderTooltip(font, new MITooltips.Line(MIText.PutMotorToUpgrade).build(), x, y);
        }

    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }

    private class WhitelistButton extends Button {
        public WhitelistButton(int i, int j, OnPress onPress, Supplier<List<Component>> tooltipSupplier) {
            super(i + 148, j + 44, 20, 20, Component.literal("test!"), onPress, Button.DEFAULT_NARRATION);
            setTooltip(new DynamicTooltip(tooltipSupplier));
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            int u = menu.pipeInterface.isWhitelist() ? 0 : 20;
            int v = this.isHoveredOrFocused() ? 20 : 0;

            RenderSystem.enableDepthTest();
            guiGraphics.blit(PipeGuiHelper.BUTTON_TEXTURE, this.getX(), this.getY(), u, v, this.width, this.height);
        }
    }
}
