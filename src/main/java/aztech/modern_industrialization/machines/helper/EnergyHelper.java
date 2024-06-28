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
package aztech.modern_industrialization.machines.helper;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import dev.technici4n.grandpower.api.EnergyStorageUtil;
import net.minecraft.core.Direction;

public class EnergyHelper {

    public static void autoOutput(MachineBlockEntity machine, OrientationComponent orientation, CableTier output, MIEnergyStorage energySource) {
        autoOutput(machine, orientation.outputDirection, output, energySource);
    }

    public static void autoOutput(MachineBlockEntity machine, Direction side, CableTier output, MIEnergyStorage energySource) {
        var storage = machine.getLevel().getCapability(EnergyApi.SIDED, machine.getBlockPos().relative(side), side.getOpposite());
        if (storage != null && storage.canConnect(output)) {
            if (EnergyStorageUtil.move(energySource, storage, Long.MAX_VALUE) > 0) {
                machine.setChanged();
            }
        }
    }
}
