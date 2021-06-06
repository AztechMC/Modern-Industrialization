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
import alexiil.mc.lib.attributes.fluid.FixedFluidInv;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import java.util.ArrayList;
import java.util.List;

/**
 * Hacky wrapper, only to get AE2 compat.
 */
public class FixedGroupedFluidInv implements FixedFluidInv {
    private final GroupedFluidInv groupedInv;
    private final List<FluidKey> fluids;

    public FixedGroupedFluidInv(GroupedFluidInv groupedInv) {
        this.groupedInv = groupedInv;
        this.fluids = new ArrayList<>(groupedInv.getStoredFluids());
    }

    @Override
    public int getTankCount() {
        return Math.max(1, fluids.size());
    }

    @Override
    public FluidVolume getInvFluid(int tank) {
        if (tank >= getTankCount()) {
            return FluidVolumeUtil.EMPTY;
        } else {
            FluidKey key = fluids.get(tank);
            return key.withAmount(groupedInv.getAmount_F(key));
        }
    }

    @Override
    public boolean isFluidValidForTank(int tank, FluidKey fluid) {
        return true;
    }

    @Override
    public boolean setInvFluid(int tank, FluidVolume to, Simulation simulation) {
        return false;
    }

    @Override
    public GroupedFluidInv getGroupedInv() {
        return groupedInv;
    }
}
