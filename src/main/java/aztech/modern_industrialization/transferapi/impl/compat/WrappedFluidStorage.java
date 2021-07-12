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
package aztech.modern_industrialization.transferapi.impl.compat;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public class WrappedFluidStorage implements GroupedFluidInv {
    private final Storage<FluidVariant> fluidStorage;

    public WrappedFluidStorage(Storage<FluidVariant> fluidStorage) {
        this.fluidStorage = fluidStorage;
    }

    @Override
    public FluidVolume attemptInsertion(FluidVolume fluidVolume, Simulation simulation) {
        Fluid fluid = fluidVolume.getRawFluid();
        if (fluid == null || fluid == Fluids.EMPTY)
            return fluidVolume;

        try (Transaction tx = TransferLbaCompat.openPossiblyNestedTransaction()) {
            long amount = fluidVolume.getAmount_F().asLong(81000, RoundingMode.DOWN);
            long inserted = fluidStorage.insert(FluidVariant.of(fluid), amount, tx);

            if (simulation.isAction()) {
                tx.commit();
            }

            return fluidVolume.getFluidKey().withAmount(fluidVolume.getAmount_F().sub(FluidAmount.of(inserted, 81000)));
        }
    }

    @Override
    public FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxFractionAmount, Simulation simulation) {
        long maxAmount = maxFractionAmount.asLong(81000, RoundingMode.DOWN);
        try (Transaction tx = Transaction.openOuter()) {
            // Find a suitable fluid to extract
            FluidVariant extractedFluid = null;
            TransferLbaCompat.OPEN_TRANSACTION.set(tx);
            for (StorageView<FluidVariant> view : fluidStorage.iterable(tx)) {
                FluidVariant fluid = view.getResource();
                if (!view.isResourceBlank() && !fluid.hasNbt() && filter.matches(FluidKeys.get(fluid.getFluid()))) {
                    try (Transaction testTx = tx.openNested()) {
                        if (view.extract(fluid, maxAmount, testTx) > 0) {
                            extractedFluid = fluid;
                            break;
                        }
                    }
                }
            }
            TransferLbaCompat.OPEN_TRANSACTION.remove();
            if (extractedFluid == null)
                return FluidVolumeUtil.EMPTY;
            // Extract it
            long extracted = fluidStorage.extract(extractedFluid, maxAmount, tx);
            if (simulation.isAction()) {
                tx.commit();
            }
            return FluidKeys.get(extractedFluid.getFluid()).withAmount(FluidAmount.of(extracted, 81000));
        }
    }

    @Override
    public String toString() {
        return "WrappedFluidStorage{" + "fluidStorage=" + fluidStorage + '}';
    }

    @Override
    public Set<FluidKey> getStoredFluids() {
        Set<FluidKey> fluidKeys = new HashSet<>();
        try (Transaction tx = TransferLbaCompat.openPossiblyNestedTransaction()) {
            for (StorageView<FluidVariant> view : fluidStorage.iterable(tx)) {
                if (!view.getResource().isBlank() && !view.getResource().hasNbt()) {
                    fluidKeys.add(FluidKeys.get(view.getResource().getFluid()));
                }
            }
        }
        return fluidKeys;
    }

    @Override
    public FluidInvStatistic getStatistics(FluidFilter filter) {
        long amount = 0;
        long capacity = 0;

        try (Transaction tx = TransferLbaCompat.openPossiblyNestedTransaction()) {
            for (StorageView<FluidVariant> view : fluidStorage.iterable(tx)) {
                FluidVariant key = view.getResource();
                if (!key.isBlank() && !key.hasNbt()) {
                    if (filter.matches(FluidKeys.get(key.getFluid()))) {
                        amount += view.getAmount();
                        capacity += view.getCapacity();
                    }
                }
            }
        }

        return new FluidInvStatistic(filter, FluidAmount.of(amount, 81000), FluidAmount.of(capacity - amount, 81000),
                FluidAmount.of(capacity, 81000));
    }
}
