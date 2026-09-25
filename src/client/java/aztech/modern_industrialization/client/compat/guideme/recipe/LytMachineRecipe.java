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

package aztech.modern_industrialization.client.compat.guideme.recipe;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.client.compat.guideme.recipe.header.LytMachineRecipeHeader;
import aztech.modern_industrialization.client.compat.guideme.recipe.header.LytMachineRecipeHeaderConditionItemImage;
import aztech.modern_industrialization.client.compat.guideme.recipe.header.LytMachineRecipeHeaderEuCostImage;
import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.util.TextHelper;
import guideme.document.DefaultStyles;
import guideme.document.block.AlignItems;
import guideme.document.block.LytParagraph;
import guideme.document.block.LytVBox;

public class LytMachineRecipe extends LytVBox {
    private final MachineCategoryParams params;
    private final MachineRecipe recipe;

    public LytMachineRecipe(MachineCategoryParams params, MachineRecipe recipe) {
        this.params = params;
        this.recipe = recipe;

        var dimensions = params.calculateDimensions();

        var headerRow = new LytMachineRecipeHeader(params, recipe, dimensions.width());
        headerRow.setAlignItems(AlignItems.CENTER);
        headerRow.setGap(2);
        appendToHeader(headerRow);
        append(headerRow);

        append(new LytMachineRecipeSlots(params, dimensions, recipe));
    }

    private void appendToHeader(LytMachineRecipeHeader header) {
        header.append(new LytMachineRecipeHeaderEuCostImage(params.steamMode));

        var euCost = new LytParagraph();
        euCost.setStyle(DefaultStyles.CRAFTING_RECIPE_TYPE);
        euCost.appendText(TextHelper.getEuTextTick(recipe.eu).getString());
        header.append(euCost);

        boolean includeRecipeConditionDisplay = params.includeRecipeConditionDisplay(recipe);
        if (includeRecipeConditionDisplay) {
            var condition = new LytMachineRecipeHeaderConditionItemImage(params.getRecipeConditionDisplayItems(recipe));
            condition.setMarginLeft(5);
            condition.setMarginRight(5);
            header.append(condition);
        }

        var duration = new LytParagraph();
        duration.setStyle(DefaultStyles.CRAFTING_RECIPE_TYPE);
        duration.appendText(MIText.BaseDurationSeconds.text(recipe.duration / 20.0).getString());
        if (!includeRecipeConditionDisplay) {
            duration.setMarginLeft(10);
        }
        header.append(duration);
    }
}
