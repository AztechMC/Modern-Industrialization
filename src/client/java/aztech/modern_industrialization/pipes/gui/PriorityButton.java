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

import aztech.modern_industrialization.client.DynamicTooltip;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

class PriorityButton extends Button {
    private final int u;
    private final BooleanSupplier isEnabled;

    public PriorityButton(int x, int y, int width, int u, String message, OnPress onPress, Supplier<List<Component>> tooltipSupplier,
            BooleanSupplier isEnabled) {
        super(x, y, width, 12, Component.literal(message), onPress, Button.DEFAULT_NARRATION);
        this.u = u;
        this.isEnabled = isEnabled;
        setTooltip(new DynamicTooltip(tooltipSupplier));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.active = this.isEnabled.getAsBoolean();

        Minecraft minecraftClient = Minecraft.getInstance();
        Font font = minecraftClient.font;
        int v = this.isHoveredOrFocused() ? 40 + this.height : 40;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        guiGraphics.blit(PipeGuiHelper.BUTTON_TEXTURE, this.getX(), this.getY(), u, v, this.width, this.height);
        int j = this.active ? 16777215 : 10526880;
        guiGraphics.drawCenteredString(font, getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2,
                j | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
