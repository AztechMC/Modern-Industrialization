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

package aztech.modern_industrialization.thirdparty.fabrictransfer.impl.fluid;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluidVariantImpl implements FluidVariant {
    private static final Map<Fluid, FluidVariant> noTagCache = new ConcurrentHashMap<>();

    public static FluidVariant of(Fluid fluid) {
        Objects.requireNonNull(fluid, "Fluid may not be null.");

        return noTagCache.computeIfAbsent(fluid, f -> new FluidVariantImpl(new FluidStack(f, 1)));
    }

    public static FluidVariant of(FluidStack stack) {
        Objects.requireNonNull(stack);

        if (stack.isComponentsPatchEmpty() || stack.isEmpty()) {
            return of(stack.getFluid());
        } else {
            return new FluidVariantImpl(stack);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-transfer-api-v1/fluid");

    private final FluidStack stack;
    private final int hashCode;

    public FluidVariantImpl(FluidStack stack) {
        this.stack = stack.copyWithAmount(1); // defensive copy
        this.hashCode = FluidStack.hashFluidAndComponents(stack);
    }

    @Override
    public Fluid getObject() {
        return this.stack.getFluid();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return this.stack.getComponentsPatch();
    }

    @Override
    public boolean matches(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(this.stack, stack);
    }

    @Override
    public FluidStack toStack(int count) {
        return this.stack.copyWithAmount(count);
    }

    @Override
    public boolean isBlank() {
        return this.stack.isEmpty();
    }

    @Override
    public String toString() {
        return "FluidVariant{stack=" + stack + '}';
    }

    @Override
    public boolean equals(Object o) {
        // succeed fast with == check
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FluidVariantImpl fluidVariant = (FluidVariantImpl) o;
        // fail fast with hash code
        return hashCode == fluidVariant.hashCode && matches(fluidVariant.stack);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
