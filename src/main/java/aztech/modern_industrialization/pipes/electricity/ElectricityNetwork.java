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
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.util.Simulation;
import java.util.*;
import net.minecraft.server.world.ServerWorld;

public class ElectricityNetwork extends PipeNetwork {
    final CableTier tier;

    public ElectricityNetwork(int id, PipeNetworkData data, CableTier tier) {
        super(id, data == null ? new ElectricityNetworkData() : data);
        this.tier = tier;
    }

    @Override
    public void tick(ServerWorld world) {
        // Gather targets
        List<EnergyInsertable> insertables = new ArrayList<>();
        List<EnergyExtractable> extractables = new ArrayList<>();
        long networkAmount = 0;
        int loadedNodeCount = 0;
        for (var entry : iterateTickingNodes()) {
            ElectricityNetworkNode node = (ElectricityNetworkNode) entry.getNode();
            node.appendAttributes(world, entry.getPos(), insertables, extractables);
            networkAmount += node.eu;
            loadedNodeCount++;
        }

        // Filter targets
        insertables.removeIf(insertable -> !insertable.canInsert(tier));
        extractables.removeIf(extractable -> !extractable.canExtract(tier));

        // Do the transfer
        long networkCapacity = loadedNodeCount * tier.getMaxTransfer();
        long extractMaxAmount = Math.min(tier.getMaxTransfer(), networkCapacity - networkAmount);
        networkAmount += transferForTargets(EnergyExtractable::extractEnergy, extractables, extractMaxAmount);
        long insertMaxAmount = Math.min(tier.getMaxTransfer(), networkAmount);
        networkAmount -= transferForTargets(EnergyInsertable::insertEnergy, insertables, insertMaxAmount);

        // Split energy evenly across the nodes
        for (var entry : iterateTickingNodes()) {
            ElectricityNetworkNode electricityNode = (ElectricityNetworkNode) entry.getNode();
            electricityNode.eu = networkAmount / loadedNodeCount;
            networkAmount -= electricityNode.eu;
            --loadedNodeCount;
        }
    }

    /**
     * Perform a transfer operation across a list of targets. Will not mutate the
     * list. Does not check for the network's max transfer rate specifically.
     */
    private static <T> long transferForTargets(TransferOperation<T> operation, List<T> targets, long maxAmount) {
        // Build target list
        List<EnergyTarget<T>> sortableTargets = new ArrayList<>(targets.size());
        for (T target : targets) {
            sortableTargets.add(new EnergyTarget<>(target));
        }
        // Shuffle for better transfer on average
        Collections.shuffle(sortableTargets);
        // Simulate the transfer for every target
        for (EnergyTarget<T> target : sortableTargets) {
            target.simulationResult = operation.transfer(target.target, maxAmount, Simulation.SIMULATE);
        }
        // Sort from low to high result
        sortableTargets.sort(Comparator.comparing(t -> t.simulationResult));
        // Actually perform the transfer
        long transferredAmount = 0;
        for (int i = 0; i < sortableTargets.size(); ++i) {
            EnergyTarget<T> target = sortableTargets.get(i);
            int remainingTargets = sortableTargets.size() - i;
            long remainingAmount = maxAmount - transferredAmount;
            long targetMaxAmount = remainingAmount / remainingTargets;

            transferredAmount += operation.transfer(target.target, targetMaxAmount, Simulation.ACT);
        }
        return transferredAmount;
    }

    @FunctionalInterface
    private interface TransferOperation<T> {
        long transfer(T transferable, long maxAmount, Simulation simulation);
    }

    private static class EnergyTarget<T> {
        final T target;
        long simulationResult;

        EnergyTarget(T target) {
            this.target = target;
        }
    }
}
