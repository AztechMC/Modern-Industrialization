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
import appeng.parts.PartAdjacentApi;
import appeng.parts.p2p.P2PTunnelPart;
import aztech.modern_industrialization.api.energy.*;
import aztech.modern_industrialization.config.MIServerConfig;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StoragePreconditions;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.DelegatingEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EnergyP2PTunnelPart extends P2PTunnelPart<EnergyP2PTunnelPart> implements IGridTickable {
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
    public void readFromNBT(ValueInput input) {
        super.readFromNBT(input);
        outputBuffer.energy = input.getLongOr("energy", 0);
    }

    @Override
    public void writeToNBT(ValueOutput output) {
        super.writeToNBT(output);
        if (outputBuffer.energy > 0) {
            output.putLong("energy", outputBuffer.energy);
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

            int moved = EnergyHandlerUtil.move(outputBuffer, adjacentEnergy, Integer.MAX_VALUE, null);
            return moved > 0 ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
        } else {
            return TickRateModulation.IDLE;
        }
    }

    // TODO 26.1
//    @PartModels
//    public static List<IPartModel> getModels() {
//        return MODELS.getModels();
//    }
//
//    @Override
//    public IPartModel getStaticModels() {
//        return MODELS.getModel(this.isPowered(), this.isActive());
//    }

    private class InputEnergyStorage implements MIEnergyStorage.NoExtract {
        @Override
        public int insert(int maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);
            int total = 0;

            final int outputTunnels = getOutputs().size();
            final int amount = maxAmount;

            if (outputTunnels == 0 || amount == 0) {
                return 0;
            }

            final int amountPerOutput = amount / outputTunnels;
            int overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

            for (var target : getOutputs()) {
                final int toSend = amountPerOutput + overflow;

                final int received = target.outputBuffer.insert(toSend, transaction);

                overflow = toSend - received;
                total += received;
            }

            deductEnergyCost(total * MIServerConfig.INSTANCE.forgeEnergyPerEu.getAsInt(), PowerUnit.FE, transaction);

            return total;
        }

        @Override
        public long getAmountAsLong() {
            long tot = 0;
            for (var output : getOutputs()) {
                tot += output.outputBuffer.getAmountAsLong();
            }
            return tot;
        }

        @Override
        public long getCapacityAsLong() {
            long tot = 0;
            for (var output : getOutputs()) {
                tot += output.outputBuffer.getCapacityAsLong();
            }
            return tot;
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return cableTier == CableTier.SUPERCONDUCTOR;
        }
    }

    private class EnergyBuffer extends SnapshotJournal<Long> implements EnergyHandler {
        private long energy;

        @Override
        protected Long createSnapshot() {
            return energy;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            energy = snapshot;
        }

        @Override
        protected void onRootCommit(Long originalState) {
            getHost().markForSave();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            int inserted = (int) Math.min(getCapacityAsLong() - energy, amount);
            if (inserted > 0) {
                updateSnapshots(transaction);
                energy += inserted;
                return inserted;
            }
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            // Note that extraction is allowed even if we are inactive since the p2p transfer already happened.
            int extracted = (int) Math.min(energy, amount);
            if (extracted > 0) {
                updateSnapshots(transaction);
                energy -= extracted;
                return extracted;
            }
            return 0;
        }

        @Override
        public long getAmountAsLong() {
            return energy;
        }

        @Override
        public long getCapacityAsLong() {
            return isActive() ? CableTier.SUPERCONDUCTOR.getMaxTransfer() : 0;
        }
    }

    private class OutputEnergyStorage extends DelegatingEnergyHandler implements MIEnergyStorage {
        public OutputEnergyStorage() {
            super(outputBuffer);
        }

        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return cableTier == CableTier.SUPERCONDUCTOR;
        }
    }
}
