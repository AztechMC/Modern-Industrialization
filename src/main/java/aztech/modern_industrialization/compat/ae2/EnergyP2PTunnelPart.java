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
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

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
        public boolean supportsInsertion() {
            for (var output : getOutputs()) {
                try (var capabilityGuard = output.getAdjacentCapability()) {
                    if (capabilityGuard.get().supportsInsertion()) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
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
                    final EnergyStorage output = capabilityGuard.get();
                    final long toSend = amountPerOutput + overflow;

                    final long received = output.insert(toSend, transaction);

                    overflow = toSend - received;
                    total += received;
                }
            }

            queueTunnelDrain(PowerUnits.TR, total, transaction);

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
        public boolean supportsExtraction() {
            try (var input = getInputCapability()) {
                return input.get().supportsExtraction();
            }
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            try (var input = getInputCapability()) {
                long extracted = input.get().extract(maxAmount, transaction);
                queueTunnelDrain(PowerUnits.TR, extracted, transaction);
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
