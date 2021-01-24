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
package aztech.modern_industrialization.inventory;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.base.FixedDenominatorStorageFunction;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.fluid.Fluid;

/**
 * A fluid insertion function that can also lock slots.
 */
@FunctionalInterface
public interface FluidInsertionFunction extends FixedDenominatorStorageFunction<Fluid> {
    /**
     * @param filter    Return false to skip some ConfigurableFluidStacks.
     * @param lockSlots Whether to lock slots or not.
     */
    long apply(Fluid fluid, long amount, Transaction tx, Predicate<ConfigurableFluidStack> filter, boolean lockSlots);

    @Override
    default long denominator() {
        return 81000;
    }

    @Override
    default long applyFixedDenominator(Fluid fluid, long amount, Transaction tx) {
        return apply(fluid, amount, tx, ConfigurableFluidStack::canPipesInsert, false);
    }
}
