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

package aztech.modern_industrialization.client.compat.guideme.recipe.fluid;

import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.interaction.GuideTooltip;
import guideme.document.interaction.InteractiveElement;
import guideme.layout.LayoutContext;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class LytMachineFluidSlot extends LytBlock implements InteractiveElement {
    private static final int ITEM_SIZE = 16;
    private static final int PADDING = 1;
    public static final int OUTER_SIZE = ITEM_SIZE + 2 * PADDING;
    private static final int CYCLE_TIME = 2000;

    private final List<Fluid> fluids;
    private final long amount;
    private final float probability;
    private final boolean input;

    private LytMachineFluidSlot(List<Fluid> fluids, long amount, float probability, boolean input) {
        this.fluids = fluids;
        this.amount = amount;
        this.probability = probability;
        this.input = input;
    }

    public LytMachineFluidSlot(MachineRecipe.FluidInput input) {
        this(input.getInputFluids(), input.amount(), input.probability(), true);
    }

    private LytMachineFluidSlot(Fluid result, long amount, float probability, boolean input) {
        this.fluids = List.of(result);
        this.amount = amount;
        this.probability = probability;
        this.input = input;
    }

    public LytMachineFluidSlot(MachineRecipe.FluidOutput output) {
        this(output.fluid(), output.amount(), output.probability(), false);
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

        var fluid = getDisplayedFluid();
        if (fluid != null) {
            context.renderFluid(fluid, x + PADDING, y + PADDING, 1, ITEM_SIZE, ITEM_SIZE);
        }
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        var fluid = getDisplayedFluid();
        if (fluid == null) {
            return Optional.empty();
        }
        return Optional.of(new MachineRecipeFluidTooltip(FluidVariant.of(fluid), amount, probability, input));
    }

    @Nullable
    private Fluid getDisplayedFluid() {
        if (fluids.isEmpty()) {
            return null;
        }
        var cycle = System.nanoTime() / TimeUnit.MILLISECONDS.toNanos(CYCLE_TIME);
        return fluids.get((int) (cycle % fluids.size()));
    }
}
