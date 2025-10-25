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
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkExtensionTank;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.ResourceAmount;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleVariantStorage;
import com.google.common.base.Preconditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidStorageComponent implements IComponent, FluidAccess {
    private final Runnable changeCallback;
    private long capacity;

    SingleVariantStorage<FluidVariant> singleStorageVariant = new SingleVariantStorage<>() {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return capacity;
        }

        @Override
        protected void onRootCommit(ResourceAmount<FluidVariant> originalState) {
            if (!originalState.resource().equals(variant) || originalState.amount() != amount) {
                changeCallback.run();
            }
        }
    };
    private final IFluidHandler fluidHandler = new FluidNetworkExtensionTank(singleStorageVariant);

    public FluidStorageComponent(Runnable changeCallback) {
        this.changeCallback = changeCallback;
    }

    public SingleVariantStorage<FluidVariant> getFluidStorage() {
        return singleStorageVariant;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        Preconditions.checkArgument(capacity >= 0, "Fluid Capacity must be > 0");
        this.capacity = capacity;
        singleStorageVariant.amount = Math.min(singleStorageVariant.amount, capacity);
    }

    public FluidVariant getFluid() {
        return singleStorageVariant.variant;
    }

    @Override
    public long getAmount() {
        return singleStorageVariant.amount;
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("fluid", singleStorageVariant.variant.toNbt(registries));
        tag.putLong("amount", singleStorageVariant.amount);
        tag.putLong("capacity", capacity);
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
        singleStorageVariant.variant = FluidVariant.fromNbt(tag.getCompound("fluid"), registries);
        singleStorageVariant.amount = tag.getLong("amount");
        capacity = tag.getLong("capacity");
    }

    @Override
    public FluidVariant getVariant() {
        return getFluid();
    }
}
