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

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.client.screen.MIHandledScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ForgeHammerScreen extends MIHandledScreen<ForgeHammerScreenHandler> {

    public static final Identifier FORGE_HAMMER_GUI = new Identifier(ModernIndustrialization.MOD_ID, "textures/gui/container/forge_hammer.png");

    private static final int X_OFFSET = 61, Y_OFFSET = 14;

    private final ForgeHammerScreenHandler handler;

    public ForgeHammerScreen(ForgeHammerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i = this.x + X_OFFSET;
        int j = this.y + Y_OFFSET + 2;

        int x1 = (int) Math.floor((mouseX - i) / 16d);
        int y1 = (int) Math.floor((mouseY - j) / 18d);

        if (x1 >= 0 && x1 <= 3 && y1 >= 0 && y1 <= 2) {
            int id = x1 + y1 * 4;
            if (id < handler.getAvailableRecipeCount()) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                this.client.interactionManager.clickButton(handler.syncId, id);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, FORGE_HAMMER_GUI);
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        int l = this.x + X_OFFSET;
        int m = this.y + Y_OFFSET;
        this.renderRecipeBackground(matrices, mouseX, mouseY, l, m);
        this.renderRecipeIcons(l, m);
    }

    private void renderRecipeIcons(int x, int y) {
        for (int i = 0; i < handler.getAvailableRecipeCount(); ++i) {

            int k = x + i % 4 * 16;
            int l = i / 4;
            int m = y + l * 18 + 2;

            Item item = handler.getAvailableRecipes().get(i).itemOutputs.get(0).item;
            int amount = handler.getAvailableRecipes().get(i).itemOutputs.get(0).amount;

            ItemStack stack = new ItemStack(item, amount);

            this.client.getItemRenderer().renderInGuiWithOverrides(stack, k, m);
        }

    }

    private void renderRecipeBackground(MatrixStack matrices, int mouseX, int mouseY, int x, int y) {
        for (int i = 0; i < handler.getAvailableRecipeCount(); ++i) {

            int k = x + i % 4 * 16;
            int l = i / 4;
            int m = y + l * 18 + 2;
            int n = this.backgroundHeight;

            if (i == handler.getSelectedRecipe()) {
                n += 18;
            } else if (mouseX >= k && mouseY >= m && mouseX < k + 16 && mouseY < m + 18) {
                n += 36;
            }

            this.drawTexture(matrices, k, m - 1, 0, n, 16, 18);
        }

    }

    protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        super.drawMouseoverTooltip(matrices, x, y);
        int x1 = this.x + X_OFFSET;
        int y1 = this.y + Y_OFFSET;

        for (int l = 0; l < handler.getAvailableRecipeCount(); ++l) {
            int n = x1 + l % 4 * 16;
            int o = y1 + l / 4 * 18 + 2;
            if (x >= n && x < n + 16 && y >= o && y < o + 18) {

                Item item = handler.getAvailableRecipes().get(l).itemOutputs.get(0).item;
                int amount = handler.getAvailableRecipes().get(l).itemOutputs.get(0).amount;
                ItemStack stack = new ItemStack(item, amount);

                this.renderTooltip(matrices, stack, x, y);
            }
        }

    }
}
