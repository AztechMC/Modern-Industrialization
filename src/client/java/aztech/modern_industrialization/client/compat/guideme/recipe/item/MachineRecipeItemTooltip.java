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

package aztech.modern_industrialization.client.compat.guideme.recipe.item;

import aztech.modern_industrialization.util.TextHelper;
import guideme.document.interaction.ItemTooltip;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class MachineRecipeItemTooltip extends ItemTooltip {
    private final float probability;
    private final boolean input;

    public MachineRecipeItemTooltip(ItemStack stack, float probability, boolean input) {
        super(stack);
        this.probability = probability;
        this.input = input;
    }

    @Override
    public List<ClientTooltipComponent> getLines() {
        List<ClientTooltipComponent> lines = new ArrayList<>(super.getLines());
        if (probability != 1) {
            lines.add(ClientTooltipComponent.create(TextHelper.getProbabilityTooltip(probability, input).getVisualOrderText()));
        }
        return lines;
    }
}
