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
package aztech.modern_industrialization.compat.ae2;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.CapabilityP2PTunnelPart;
import aztech.modern_industrialization.api.energy.*;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StoragePreconditions;
import java.util.List;

public class EnergyP2PTunnelPart extends CapabilityP2PTunnelPart<EnergyP2PTunnelPart, MIEnergyStorage> {

    private static final P2PModels MODELS = new P2PModels("part/energy_p2p_tunnel");

    public EnergyP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, EnergyApi.SIDED);

        inputHandler = new InputEnergyStorage();
        outputHandler = new OutputEnergyStorage();
        emptyHandler = EnergyApi.EMPTY;
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private class InputEnergyStorage implements MIEnergyStorage.NoExtract {
        @Override
        public boolean canReceive() {
            for (var output : getOutputs()) {
                try (var capabilityGuard = output.getAdjacentCapability()) {
                    if (capabilityGuard.get().canReceive()) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public long receive(long maxAmount, boolean simulate) {
            StoragePreconditions.notNegative(maxAmount);
            long total = 0;

            final int outputTunnels = getOutputs().size();
            final long amount = maxAmount;

            if (outputTunnels == 0 || amount == 0) {
                return 0;
            }

            final long amountPerOutput = amount / outputTunnels;
            long overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

            for (var target : getOutputs()) {
                try (CapabilityGuard capabilityGuard = target.getAdjacentCapability()) {
                    var output = capabilityGuard.get();
                    final long toSend = amountPerOutput + overflow;

                    final long received = output.receive(toSend, simulate);

                    overflow = toSend - received;
                    total += received;
                }
            }

            if (!simulate) {
                queueTunnelDrain(PowerUnits.FE, total);
            }

            return total;
        }

        @Override
        public long getAmount() {
            long tot = 0;
            for (var output : getOutputs()) {
                try (var capabilityGuard = output.getAdjacentCapability()) {
                    tot += capabilityGuard.get().getAmount();
                }
            }
            return tot;
        }

        @Override
        public long getCapacity() {
            long tot = 0;
            for (var output : getOutputs()) {
                try (var capabilityGuard = output.getAdjacentCapability()) {
                    tot += capabilityGuard.get().getCapacity();
                }
            }
            return tot;
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return cableTier == CableTier.SUPERCONDUCTOR;
        }
    }

    private class OutputEnergyStorage implements MIEnergyStorage.NoInsert {
        @Override
        public boolean canExtract() {
            try (var input = getInputCapability()) {
                return input.get().canExtract();
            }
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            try (var input = getInputCapability()) {
                long extracted = input.get().extract(maxAmount, simulate);
                if (!simulate) {
                    queueTunnelDrain(PowerUnits.FE, extracted);
                }
                return extracted;
            }
        }

        @Override
        public long getAmount() {
            try (var input = getInputCapability()) {
                return input.get().getAmount();
            }
        }

        @Override
        public long getCapacity() {
            try (var input = getInputCapability()) {
                return input.get().getCapacity();
            }
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return cableTier == CableTier.SUPERCONDUCTOR;
        }
    }
}
