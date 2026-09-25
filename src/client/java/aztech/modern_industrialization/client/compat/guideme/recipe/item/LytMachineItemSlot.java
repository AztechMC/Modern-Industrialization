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

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.interaction.GuideTooltip;
import guideme.document.interaction.InteractiveElement;
import guideme.layout.LayoutContext;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class LytMachineItemSlot extends LytBlock implements InteractiveElement {
    private static final int ITEM_SIZE = 16;
    private static final int PADDING = 1;
    public static final int OUTER_SIZE = ITEM_SIZE + 2 * PADDING;
    private static final int CYCLE_TIME = 2000;

    private final ItemStack[] stacks;
    private final float probability;
    private final boolean input;

    private LytMachineItemSlot(Ingredient ingredient, int amount, float probability, boolean input) {
        this.stacks = ingredient.getItems().clone();
        for (int i = 0; i < this.stacks.length; i++) {
            this.stacks[i] = this.stacks[i].copyWithCount(amount);
        }
        this.probability = probability;
        this.input = input;
    }

    public LytMachineItemSlot(MachineRecipe.ItemInput input) {
        this(input.ingredient(), input.amount(), input.probability(), true);
    }

    private LytMachineItemSlot(ItemVariant result, int amount, float probability, boolean input) {
        this.stacks = new ItemStack[] { result.toStack(amount) };
        this.probability = probability;
        this.input = input;
    }

    public LytMachineItemSlot(MachineRecipe.ItemOutput output) {
        this(output.variant(), output.amount(), output.probability(), false);
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, OUTER_SIZE, OUTER_SIZE);
    }

    @Override
    protected void onLayoutMoved(int deltaX, int deltaY) {}

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {}

    @Override
    public void render(RenderContext context) {
        var x = bounds.x();
        var y = bounds.y();

        context.fillIcon(bounds, GuiAssets.SLOT);

        var stack = getDisplayedStack();
        if (!stack.isEmpty()) {
            context.renderItem(stack, x + PADDING, y + PADDING, 1, ITEM_SIZE, ITEM_SIZE);
        }
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        var stack = getDisplayedStack();
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MachineRecipeItemTooltip(stack, probability, input));
    }

    private ItemStack getDisplayedStack() {
        if (stacks.length == 0) {
            return ItemStack.EMPTY;
        }
        var cycle = System.nanoTime() / TimeUnit.MILLISECONDS.toNanos(CYCLE_TIME);
        return stacks[(int) (cycle % stacks.length)];
    }
}
