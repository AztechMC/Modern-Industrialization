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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBarClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public class SteamDrillTooltipComponent implements ClientTooltipComponent {
    final SteamDrillItem.SteamDrillTooltipData data;

    public SteamDrillTooltipComponent(SteamDrillItem.SteamDrillTooltipData data) {
        this.data = data;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return 40;
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, PoseStack matrices, ItemRenderer itemRenderer, int z) {
        // Slot background
        RenderSystem.setShaderTexture(0, MachineScreen.SLOT_ATLAS);
        GuiComponent.blit(matrices, x, y, 0, 0, 18, 18, 256, 256);
        // Stack itself
        ItemStack stack = data.variant().toStack((int) data.amount());
        itemRenderer.renderAndDecorateItem(stack, x + 1, y + 1);
        itemRenderer.renderGuiItemDecorations(textRenderer, stack, x + 1, y + 1);
        // Burning flame next to the stack
        var progressParams = new ProgressBar.Parameters(0, 0, "furnace", true);
        ProgressBarClient.renderProgress(0, matrices, x + 20, y, progressParams, (float) data.burnTicks() / data.maxBurnTicks());
    }
}
