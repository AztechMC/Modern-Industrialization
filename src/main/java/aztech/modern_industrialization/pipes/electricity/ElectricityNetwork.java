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
package aztech.modern_industrialization.pipes.electricity;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.pipes.PipeStatsCollector;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import java.util.*;
import net.minecraft.server.level.ServerLevel;

public class ElectricityNetwork extends PipeNetwork {
    private static final List<MIEnergyStorage> STORAGES_CACHE = new ArrayList<>();

    final CableTier tier;
    final PipeStatsCollector stats = new PipeStatsCollector();

    public ElectricityNetwork(int id, PipeNetworkData data, CableTier tier) {
        super(id, data == null ? new ElectricityNetworkData() : data);
        this.tier = tier;
    }

    @Override
    public void tick(ServerLevel world) {
        // Gather targets
        List<MIEnergyStorage> storages = STORAGES_CACHE;
        long networkAmount = 0;
        int loadedNodeCount = 0;
        for (var entry : iterateTickingNodes()) {
            ElectricityNetworkNode node = (ElectricityNetworkNode) entry.getNode();
            node.appendAttributes(world, entry.getPos(), tier, storages);
            networkAmount += node.eu;
            loadedNodeCount++;
        }

        // Filter targets
        storages.removeIf(s -> !s.canConnect(tier));

        // Do the transfer
        long networkCapacity = loadedNodeCount * tier.getMaxTransfer();
        long extractMaxAmount = Math.min(tier.getMaxTransfer(), networkCapacity - networkAmount);
        long extracted = transferForTargets(MIEnergyStorage::extract, storages, extractMaxAmount);
        networkAmount += extracted;

        long insertMaxAmount = Math.min(tier.getMaxTransfer(), networkAmount);
        long inserted = transferForTargets(MIEnergyStorage::receive, storages, insertMaxAmount);
        networkAmount -= inserted;

        stats.addValue(Math.max(extracted, inserted));

        // Split energy evenly across the nodes
        for (var entry : iterateTickingNodes()) {
            ElectricityNetworkNode electricityNode = (ElectricityNetworkNode) entry.getNode();
            electricityNode.eu = networkAmount / loadedNodeCount;
            networkAmount -= electricityNode.eu;
            --loadedNodeCount;
        }

        // Very important to clear the static caches
        storages.clear();
    }

    /**
     * Perform a transfer operation across a list of targets. Will not mutate the
     * list. Does not check for the network's max transfer rate specifically.
     */
    private static long transferForTargets(TransferOperation operation, List<MIEnergyStorage> targets, long maxAmount) {
        // Build target list
        List<EnergyTarget> sortableTargets = new ArrayList<>(targets.size());
        for (var target : targets) {
            sortableTargets.add(new EnergyTarget(target));
        }
        // Shuffle for better transfer on average
        Collections.shuffle(sortableTargets);
        // Simulate the transfer for every target
        for (EnergyTarget target : sortableTargets) {
            target.simulationResult = operation.transfer(target.target, maxAmount, true);
        }
        // Sort from low to high result
        sortableTargets.sort(Comparator.comparingLong(t -> t.simulationResult));
        // Actually perform the transfer
        long transferredAmount = 0;
        for (int i = 0; i < sortableTargets.size(); ++i) {
            EnergyTarget target = sortableTargets.get(i);
            int remainingTargets = sortableTargets.size() - i;
            long remainingAmount = maxAmount - transferredAmount;
            long targetMaxAmount = remainingAmount / remainingTargets;

            transferredAmount += operation.transfer(target.target, targetMaxAmount, false);
        }
        return transferredAmount;
    }

    @FunctionalInterface
    private interface TransferOperation {
        long transfer(MIEnergyStorage transferable, long maxAmount, boolean simulate);
    }

    private static class EnergyTarget {
        final MIEnergyStorage target;
        long simulationResult;

        EnergyTarget(MIEnergyStorage target) {
            this.target = target;
        }
    }
}
