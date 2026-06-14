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

import appeng.api.config.PowerUnit;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartAdjacentApi;
import appeng.parts.p2p.P2PTunnelPart;
import aztech.modern_industrialization.api.energy.*;
import aztech.modern_industrialization.config.MIServerConfig;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StoragePreconditions;
import dev.technici4n.grandpower.api.DelegatingEnergyStorage;
import dev.technici4n.grandpower.api.EnergyStorageUtil;
import dev.technici4n.grandpower.api.ILongEnergyStorage;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class EnergyP2PTunnelPart extends P2PTunnelPart<EnergyP2PTunnelPart> implements IGridTickable {
    private static final P2PModels MODELS = new P2PModels("part/energy_p2p_tunnel");

    private final PartAdjacentApi<MIEnergyStorage> adjacentCapability;
    private final MIEnergyStorage inputStorage = new InputEnergyStorage();
    private final EnergyBuffer outputBuffer = new EnergyBuffer();
    private final MIEnergyStorage exposedOutput = new OutputEnergyStorage();

    public EnergyP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);

        getMainNode().addService(IGridTickable.class, this);

        this.adjacentCapability = new PartAdjacentApi<>(this, EnergyApi.SIDED);
    }

    public MIEnergyStorage getExposedApi() {
        if (isOutput()) {
            return exposedOutput;
        } else {
            return inputStorage;
        }
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        outputBuffer.energy = data.getLong("energy");
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        if (outputBuffer.energy > 0) {
            data.putLong("energy", outputBuffer.energy);
        }
    }

    @Override
    public void onTunnelNetworkChange() {
        // This might be invoked while the network is being unloaded,
        // however the capability system should handle this fine.
        // (Not OK for block updates though, thankfully we don't need them anymore!)
        getBlockEntity().invalidateCapabilities();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (isOutput()) {
            // Try to push energy to the adjacent block
            var adjacentEnergy = adjacentCapability.find();
            if (adjacentEnergy == null || !adjacentEnergy.canConnect(CableTier.SUPERCONDUCTOR)) {
                adjacentEnergy = EnergyApi.EMPTY;
            }

            long moved = EnergyStorageUtil.move(outputBuffer, adjacentEnergy, Long.MAX_VALUE);
            return moved > 0 ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
        } else {
            return TickRateModulation.IDLE;
        }
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
            return true;
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
                final long toSend = amountPerOutput + overflow;

                final long received = target.outputBuffer.receive(toSend, simulate);

                overflow = toSend - received;
                total += received;
            }

            if (!simulate) {
                queueTunnelDrain(PowerUnit.FE, total * MIServerConfig.INSTANCE.forgeEnergyPerEu.getAsInt());
            }

            return total;
        }

        @Override
        public long getAmount() {
            long tot = 0;
            for (var output : getOutputs()) {
                tot += output.outputBuffer.getAmount();
            }
            return tot;
        }

        @Override
        public long getCapacity() {
            long tot = 0;
            for (var output : getOutputs()) {
                tot += output.outputBuffer.getCapacity();
            }
            return tot;
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return cableTier == CableTier.SUPERCONDUCTOR;
        }
    }

    private class EnergyBuffer implements ILongEnergyStorage {
        private long energy;

        @Override
        public long receive(long maxReceive, boolean simulate) {
            long inserted = Math.min(getCapacity() - energy, maxReceive);
            if (inserted > 0) {
                if (!simulate) {
                    energy += inserted;
                    getHost().markForSave();
                }
                return inserted;
            }
            return 0;
        }

        @Override
        public long extract(long maxExtract, boolean simulate) {
            // Note that extraction is allowed even if we are inactive since the p2p transfer already happened.
            long extracted = Math.min(energy, maxExtract);
            if (extracted > 0) {
                if (!simulate) {
                    energy -= extracted;
                    getHost().markForSave();
                }
                return extracted;
            }
            return 0;
        }

        @Override
        public long getAmount() {
            return energy;
        }

        @Override
        public long getCapacity() {
            return isActive() ? CableTier.SUPERCONDUCTOR.getMaxTransfer() : 0;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    private class OutputEnergyStorage extends DelegatingEnergyStorage implements MIEnergyStorage {
        public OutputEnergyStorage() {
            super(outputBuffer);
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        @Override
        public long receive(long maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return cableTier == CableTier.SUPERCONDUCTOR;
        }
    }
}
