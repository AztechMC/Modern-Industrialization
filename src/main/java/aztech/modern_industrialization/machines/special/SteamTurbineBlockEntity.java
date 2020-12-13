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
package aztech.modern_industrialization.machines.special;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;

public final class SteamTurbineBlockEntity extends MachineBlockEntity {
    private final EnergyExtractable extractable;
    private final CableTier tier;

    public SteamTurbineBlockEntity(MachineFactory factory, CableTier tier) {
        super(factory);

        inventory.fluidStacks.set(0, ConfigurableFluidStack.lockedInputSlot(factory.getInputBucketCapacity() * 81000, MIFluids.STEAM));
        this.tier = tier;
        extractable = buildExtractable(tier);
    }

    @Override
    protected long getMaxStoredEu() {
        return tier.getMaxInsert() * 10;
    }

    @Override
    public void tick() {
        if (world.isClient)
            return;

        boolean wasActive = isActive;

        int transformed = (int) Math.min(Math.min(inventory.fluidStacks.get(0).getAmount() / 81, getMaxStoredEu() - storedEu), tier.getEu());
        if (transformed > 0) {
            inventory.fluidStacks.get(0).decrement(transformed * 81);
            storedEu += transformed;
            isActive = true;
        } else {
            isActive = false;
        }

        autoExtractEnergy(outputDirection, tier);

        if (wasActive != isActive) {
            sync();
        }
        markDirty();
    }

    @Override
    public void registerAdditionalApis() {
        EnergyApi.MOVEABLE.registerForBlockEntities((blockEntity, direction) -> {
            SteamTurbineBlockEntity be = ((SteamTurbineBlockEntity) blockEntity);
            return direction == be.outputDirection ? be.extractable : null;
        }, getType());
    }
}
