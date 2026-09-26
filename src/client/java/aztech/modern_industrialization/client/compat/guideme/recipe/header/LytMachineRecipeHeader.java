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

import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import guideme.document.LytRect;
import guideme.document.block.LytAxisBox;
import guideme.document.interaction.GuideTooltip;
import guideme.document.interaction.InteractiveElement;
import guideme.layout.LayoutContext;
import guideme.layout.Layouts;
import java.util.Optional;

public class LytMachineRecipeHeader extends LytAxisBox implements InteractiveElement {
    private final MachineCategoryParams params;
    private final MachineRecipe recipe;

    private final int width;

    public LytMachineRecipeHeader(MachineCategoryParams params, MachineRecipe recipe, int width) {
        this.params = params;
        this.recipe = recipe;
        this.width = width;
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        return Layouts.horizontalLayout(context,
                children,
                x,
                y,
                Math.min(width, availableWidth),
                true,
                0,
                0,
                0,
                0,
                getGap(),
                getAlignItems(),
                false);
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        return Optional.of(new MachineRecipeHeaderTooltip(params, recipe));
    }
}
