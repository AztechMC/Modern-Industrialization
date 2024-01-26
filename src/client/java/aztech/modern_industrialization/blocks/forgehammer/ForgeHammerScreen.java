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
package aztech.modern_industrialization.blocks.forgehammer;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.client.screen.MIHandledScreen;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class ForgeHammerScreen extends MIHandledScreen<ForgeHammerScreenHandler> {

    public static final ResourceLocation FORGE_HAMMER_GUI = MI.id("textures/gui/container/forge_hammer.png");

    private static final int X_OFFSET = 61, Y_OFFSET = 14;

    private final ForgeHammerScreenHandler handler;

    public ForgeHammerScreen(ForgeHammerScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.handler = handler;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i = this.leftPos + X_OFFSET;
        int j = this.topPos + Y_OFFSET + 2;

        int x1 = (int) Math.floor((mouseX - i) / 16d);
        int y1 = (int) Math.floor((mouseY - j) / 18d);

        if (x1 >= 0 && x1 <= 3 && y1 >= 0 && y1 <= 2) {
            int id = x1 + y1 * 4;
            if (id < handler.getAvailableRecipeCount()) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick(handler.containerId, id);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(FORGE_HAMMER_GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        int l = this.leftPos + X_OFFSET;
        int m = this.topPos + Y_OFFSET;
        this.renderRecipeBackground(guiGraphics, mouseX, mouseY, l, m);
        this.renderRecipeIcons(guiGraphics, l, m);
    }

    private void renderRecipeIcons(GuiGraphics guiGraphics, int x, int y) {
        for (int i = 0; i < handler.getAvailableRecipeCount(); ++i) {

            int k = x + i % 4 * 16;
            int l = i / 4;
            int m = y + l * 18 + 2;

            RenderHelper.renderAndDecorateItem(guiGraphics, font, handler.getAvailableRecipes().get(i).value().result(), k, m);
        }

    }

    private void renderRecipeBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        for (int i = 0; i < handler.getAvailableRecipeCount(); ++i) {

            int k = x + i % 4 * 16;
            int l = i / 4;
            int m = y + l * 18 + 2;
            int n = this.imageHeight;

            if (i == handler.getSelectedRecipe()) {
                n += 18;
            } else if (mouseX >= k && mouseY >= m && mouseX < k + 16 && mouseY < m + 18) {
                n += 36;
            }

            guiGraphics.blit(FORGE_HAMMER_GUI, k, m - 1, 0, n, 16, 18);
        }

    }

    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        int x1 = this.leftPos + X_OFFSET;
        int y1 = this.topPos + Y_OFFSET;

        for (int l = 0; l < handler.getAvailableRecipeCount(); ++l) {
            int n = x1 + l % 4 * 16;
            int o = y1 + l / 4 * 18 + 2;
            if (x >= n && x < n + 16 && y >= o && y < o + 18) {
                guiGraphics.renderTooltip(font, handler.getAvailableRecipes().get(l).value().result(), x, y);
            }
        }

    }
}
