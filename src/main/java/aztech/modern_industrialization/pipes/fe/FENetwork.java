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
package aztech.modern_industrialization.pipes.fe;

import aztech.modern_industrialization.pipes.PipeStatsCollector;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class FENetwork extends PipeNetwork {
    final PipeStatsCollector stats = new PipeStatsCollector();
    final PipeStatsCollector capacityStats = new PipeStatsCollector();

    public FENetwork(int id, PipeNetworkData data) {
        super(id, data == null ? new FENetworkData() : data);
    }

    @Override
    public void tick(ServerLevel world) {
        // Gather targets
        List<FETarget> targets = new ArrayList<>();
        long networkAmount = 0;
        long networkCapacity = 0;
        int loadedNodeCount = 0;
        for (var entry : iterateTickingNodes()) {
            FENetworkNode node = (FENetworkNode) entry.getNode();
            long nodeCapacity = node.gatherTargets(world, entry.getPos(), targets);
            networkAmount += node.energy;
            networkCapacity += nodeCapacity;
            loadedNodeCount++;
        }

        // Sort by descending priority
        targets.sort(Comparator.comparingInt(target -> -target.priority));
        // Extract from targets into the network
        int extracted = transferByPriority(IEnergyStorage::extractEnergy, targets, Ints.saturatedCast(networkCapacity - networkAmount));
        networkAmount += extracted;
        // Insert into the targets from the network
        int inserted = transferByPriority(IEnergyStorage::receiveEnergy, targets, Ints.saturatedCast(networkAmount));
        networkAmount -= inserted;

        for (var entry : iterateTickingNodes()) {
            FENetworkNode node = (FENetworkNode) entry.getNode();
            node.energy = networkAmount / loadedNodeCount;
            networkAmount -= node.energy;
            loadedNodeCount--;
        }

        stats.addValue(Math.max(extracted, inserted));
        capacityStats.addValue(networkCapacity);
    }

    private static int transferByPriority(TransferOperation operation, List<FETarget> targets, int maxAmount) {
        // Transfer for each section
        int transferredAmount = 0;
        int sectionStart = 0;
        for (int i = 0; i < targets.size(); i++) {
            if (i == targets.size() - 1 || targets.get(sectionStart).priority != targets.get(i + 1).priority) {
                transferredAmount += transferForSection(operation, targets.subList(sectionStart, i + 1), maxAmount - transferredAmount);
                sectionStart = i + 1;
            }
        }
        return transferredAmount;
    }

    private static int transferForSection(TransferOperation operation, List<FETarget> targets, int maxAmount) {
        // Shuffle the targets for better average transfer when simulation returns the
        // same result every time
        Collections.shuffle(targets);
        // Simulate the transfer for every target
        for (FETarget target : targets) {
            target.simulationResult = operation.transfer(target.storage, maxAmount, true);
        }
        // Sort from low result to high result
        targets.sort(Comparator.comparingLong(target -> target.simulationResult));
        // Perform the transfer
        int transferredAmount = 0;
        for (int i = 0; i < targets.size(); i++) {
            var target = targets.get(i);
            int remainingTargts = targets.size() - i;
            int remainingAmount = maxAmount - transferredAmount;
            int targetMaxAmount = remainingAmount / remainingTargts;
            transferredAmount += operation.transfer(target.storage, targetMaxAmount, false);
        }
        return transferredAmount;
    }

    @FunctionalInterface
    private interface TransferOperation {
        int transfer(IEnergyStorage handler, int maxAmount, boolean simulate);
    }
}
