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
package aztech.modern_industrialization.nuclear;

import aztech.modern_industrialization.items.LockableFluidItem;
import java.util.Random;
import net.minecraft.item.ItemStack;

public class NuclearCoolant extends MINuclearItem {

    private int multiplier;

    public NuclearCoolant(String id, int maxHeat, int multiplier) {
        super(id, -1, maxHeat);
        this.multiplier = multiplier;
        setFluidLockable(true);
    }

    @Override
    public void tick(ItemStack is, NuclearReactorBlockEntity nuclearReactor, double neutronPulse, Random rand) {
        NuclearFluidCoolant coolant = getCoolant(is);
        if (coolant != null) {
            /*
             * FIXME int extractMax = nuclearReactor.getMaxFluidExtraction(coolant.fluid);
             * int insertMax = nuclearReactor.getMaxFluidInsertion(coolant.fluidResult); int
             * s = Math.min(extractMax, insertMax); int u = Math.min(s, 64 * multiplier);
             * int v = Math.min(u, getHeat(is) / coolant.heatConsumed); setHeat(is,
             * getHeat(is) - v * coolant.heatConsumed); int r =
             * nuclearReactor.extractFluidFromInputHatch(FluidKeys.WATER, v); if (r != 0) {
             * throw new IllegalStateException("Remaining extracted fluid : " + r); } r =
             * nuclearReactor.insertFluidInOutputHatch(MIFluids.STEAM.key, v); if (r != 0) {
             * throw new IllegalStateException("Remaining inserted fluid : " + r); }
             */
        }
    }

    public NuclearFluidCoolant getCoolant(ItemStack is) {
        for (NuclearFluidCoolant coolant : NuclearFluidCoolant.values()) {
            if (coolant.fluid == LockableFluidItem.getFluid(is)) {
                return coolant;
            }
        }
        return null;
    }

    @Override
    public double getNeutronPulse(ItemStack is) {
        return 0;
    }

    @Override
    public double getHeatProduction(ItemStack is, double neutronReceived) {
        return 0;
    }

    @Override
    public double getNeutronReflection(ItemStack is, int angle) {
        NuclearFluidCoolant coolant = getCoolant(is);
        if (coolant != null) {
            return coolant.neutronReflection * 0.25;
        }
        return 0;
    }

    @Override
    public double getHeatTransferMax(ItemStack is) {
        NuclearFluidCoolant coolant = getCoolant(is);
        if (coolant != null) {
            return coolant.heatTransfer * multiplier;
        }
        return 0;
    }

    @Override
    public double getHeatTransferNeighbourFraction(ItemStack is) {
        return 0;
    }

    public int getMultiplier() {
        return multiplier;
    }
}
