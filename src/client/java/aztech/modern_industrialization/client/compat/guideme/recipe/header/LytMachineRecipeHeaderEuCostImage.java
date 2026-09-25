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

package aztech.modern_industrialization.client.compat.guideme.recipe.header;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.client.compat.guideme.MIGuideMeRenderHelper;
import aztech.modern_industrialization.client.machines.gui.MachineScreen;
import aztech.modern_industrialization.compat.rei.machines.SteamMode;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.layout.LayoutContext;
import guideme.render.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;

public class LytMachineRecipeHeaderEuCostImage extends LytBlock {
    private final SteamMode steamMode;

    public LytMachineRecipeHeaderEuCostImage(SteamMode steamMode) {
        this.steamMode = steamMode;
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, 9, 9);
    }

    @Override
    protected void onLayoutMoved(int deltaX, int deltaY) {}

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {}

    @Override
    public void render(RenderContext context) {
        var bounds = getBounds();
        switch (steamMode) {
            case BOTH -> MIGuideMeRenderHelper.fillTexturedRect(context, MachineScreen.SLOT_ATLAS, bounds.x(), bounds.y(), 9, 9, 80, 18, 20, 20, 256, 256);
            case STEAM_ONLY -> MIGuideMeRenderHelper.fillTexturedRect(context, MI.id("textures/item/steam_bucket.png"), bounds.x(), bounds.y(), 9, 9, 0, 0, 16, 16, 16, 16);
            case ELECTRIC_ONLY -> MIGuideMeRenderHelper.fillTexturedRect(context, MachineScreen.SLOT_ATLAS, bounds.x() + 1, bounds.y(), 7, 9, 243, 0, 13, 18, 256, 256);
        }
    }
}
