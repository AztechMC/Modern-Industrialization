/*
 * MIT License
 *
 * Copyright (c) 2023 Justin Hu
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
package aztech.modern_industrialization.blocks.toolstation;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.client.screen.MIHandledScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ToolStationScreen extends MIHandledScreen<ToolStationScreenHandler> {
    public static final ResourceLocation TOOL_STATION_GUI = new ResourceLocation(ModernIndustrialization.MOD_ID,
            "textures/gui/container/tool_station.png");

    private static final int X_OFFSET = 61, Y_OFFSET = 14;

    private final ToolStationScreenHandler handler;

    public ToolStationScreen(ToolStationScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.handler = handler;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        this.renderBackground(guiGraphics);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TOOL_STATION_GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        for (int idx = handler.getNumAddonSlots(); idx < 5; ++idx) {
            guiGraphics.blit(TOOL_STATION_GUI, this.leftPos + 43 + idx * 18, this.topPos + 41, 0, 166, 18, 18);
        }
        if (!handler.isComponentsEnabled()) {
            guiGraphics.blit(TOOL_STATION_GUI, this.leftPos + 43, this.topPos + 23, 0, 166, 18 * 3, 18);
        }
    }
}
