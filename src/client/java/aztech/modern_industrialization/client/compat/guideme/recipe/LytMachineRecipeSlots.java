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

import aztech.modern_industrialization.client.compat.guideme.MIGuideMeRenderHelper;
import aztech.modern_industrialization.client.compat.guideme.recipe.fluid.LytMachineFluidSlot;
import aztech.modern_industrialization.client.compat.guideme.recipe.item.LytMachineItemSlot;
import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.block.LytBox;
import guideme.layout.LayoutContext;
import guideme.render.RenderContext;
import java.util.function.Function;
import java.util.function.Supplier;

public class LytMachineRecipeSlots extends LytBox {
    private final MachineCategoryParams params;
    private final MachineRecipe recipe;

    private final int normalizedX;
    private final int normalizedY;
    private final int width;
    private final int height;

    private final LytBlock[] itemInputs;
    private final LytBlock[] fluidInputs;
    private final LytBlock[] itemOutputs;
    private final LytBlock[] fluidOutputs;

    public LytMachineRecipeSlots(MachineCategoryParams params, MachineCategoryParams.Dimensions dimensions, MachineRecipe recipe) {
        this.params = params;
        this.recipe = recipe;

        this.normalizedX = dimensions.minX();
        this.normalizedY = dimensions.minY();
        this.width = dimensions.width() + 2;
        this.height = dimensions.height() + 2;

        this.itemInputs = appendSlots(
                recipe.itemInputs.size(),
                params.itemInputs,
                (index) -> new LytMachineItemSlot(recipe.itemInputs.get(index)),
                () -> new LytMachineItemSlot(true));

        this.fluidInputs = appendSlots(
                recipe.fluidInputs.size(),
                params.fluidInputs,
                (index) -> new LytMachineFluidSlot(recipe.fluidInputs.get(index)),
                () -> new LytMachineFluidSlot(true));

        this.itemOutputs = appendSlots(
                recipe.itemOutputs.size(),
                params.itemOutputs,
                (index) -> new LytMachineItemSlot(recipe.itemOutputs.get(index)),
                () -> new LytMachineItemSlot(false));

        this.fluidOutputs = appendSlots(
                recipe.fluidOutputs.size(),
                params.fluidOutputs,
                (index) -> new LytMachineFluidSlot(recipe.fluidOutputs.get(index)),
                () -> new LytMachineFluidSlot(false));
    }

    private LytBlock[] appendSlots(int recipePartCount, SlotPositions slotPositions, Function<Integer, LytBlock> slotFactory, Supplier<LytBlock> emptySlotSupplier) {
        LytBlock[] items = new LytBlock[slotPositions.size()];
        for (int index = 0; index < slotPositions.size(); index++) {
            if (recipePartCount > index) {
                append(items[index] = slotFactory.apply(index));
            } else {
                append(items[index] = emptySlotSupplier.get());
            }
        }
        return items;
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        computeSlotsLayout(context, x, y, availableWidth, itemInputs, params.itemInputs);
        computeSlotsLayout(context, x, y, availableWidth, fluidInputs, params.fluidInputs);
        computeSlotsLayout(context, x, y, availableWidth, itemOutputs, params.itemOutputs);
        computeSlotsLayout(context, x, y, availableWidth, fluidOutputs, params.fluidOutputs);
        return new LytRect(x, y, width, height);
    }

    private void computeSlotsLayout(LayoutContext context, int x, int y, int availableWidth, LytBlock[] blocks, SlotPositions positions) {
        for (int index = 0; index < blocks.length; index++) {
            blocks[index].layout(context, x + positions.getX(index) - normalizedX, y + positions.getY(index) - normalizedY, availableWidth);
        }
    }

    @Override
    public void render(RenderContext context) {
        MIGuideMeRenderHelper.fillTexturedRect(
                context,
                params.progressBarParams.getTextureId(),
                bounds.x() + params.progressBarParams.renderX() - normalizedX + 1,
                bounds.y() + params.progressBarParams.renderY() - normalizedY,
                params.progressBarParams.width(),
                params.progressBarParams.height(),
                0, 0, params.progressBarParams.width(), params.progressBarParams.height(), params.progressBarParams.width(), params.progressBarParams.textureHeight());

        super.render(context);
    }
}
