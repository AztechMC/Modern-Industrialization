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

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

class PriorityButton extends ButtonWidget {
    private final int u;

    public PriorityButton(int x, int y, int width, int u, String message, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, 12, new LiteralText(message), onPress, tooltipSupplier);
        this.u = u;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        minecraftClient.getTextureManager().bindTexture(PipeGuiHelper.BUTTON_TEXTURE);
        int v = this.isHovered() ? 40 + this.height : 40;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        drawTexture(matrices, this.x, this.y, u, v, this.width, this.height);
        int j = this.active ? 16777215 : 10526880;
        drawCenteredText(matrices, textRenderer, getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2,
                j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        if (this.isHovered()) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }
}
