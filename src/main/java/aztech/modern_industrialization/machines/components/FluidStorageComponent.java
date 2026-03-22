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

package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.api.machine.component.FluidAccess;
import aztech.modern_industrialization.machines.MachineComponent;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkExtensionTank;
import com.google.common.base.Preconditions;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

public class FluidStorageComponent implements MachineComponent, FluidAccess {
    private final Runnable changeCallback;
    private int capacity;

    private final FluidStacksResourceHandler fluidHandler = new FluidStacksResourceHandler(1, 0) {
        // Capacity is dynamic, don't pass it through the constructor but rather override the method
        @Override
        protected int getCapacity(int index, FluidResource resource) {
            return capacity;
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            changeCallback.run();
        }
    };
    private final ResourceHandler<FluidResource> exposedFluidHandler = new FluidNetworkExtensionTank(fluidHandler);

    public FluidStorageComponent(Runnable changeCallback) {
        this.changeCallback = changeCallback;
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return fluidHandler;
    }

    public ResourceHandler<FluidResource> getExposedFluidHandler() {
        return exposedFluidHandler;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        Preconditions.checkArgument(capacity >= 0, "Fluid Capacity must be > 0");
        this.capacity = capacity;
        this.fluidHandler.set(0, getFluid(), Math.min(getAmount(), capacity));
    }

    public FluidResource getFluid() {
        return fluidHandler.getResource(0);
    }

    @Override
    public int getAmount() {
        return fluidHandler.getAmountAsInt(0);
    }

    @Override
    public void writeNbt(ValueOutput output) {
        fluidHandler.serialize(output);
        output.putInt("capacity", capacity);
    }

    @Override
    public void readNbt(ValueInput input, boolean isUpgradingMachine) {
        fluidHandler.deserialize(input);
        capacity = input.getIntOr("capacity", 0);
    }

    @Override
    public FluidResource getVariant() {
        return getFluid();
    }
}
