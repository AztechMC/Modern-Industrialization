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
package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.pipes.PipeStatsCollector;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.server.level.ServerLevel;

public class FluidNetwork extends PipeNetwork {
    final int nodeCapacity;
    final PipeStatsCollector stats = new PipeStatsCollector();

    public FluidNetwork(int id, PipeNetworkData data, int nodeCapacity) {
        super(id, data == null ? new FluidNetworkData(FluidVariant.blank()) : data);
        this.nodeCapacity = nodeCapacity;
    }

    @Override
    public void tick(ServerLevel world) {
        // Gather targets and hopefully set fluid
        List<FluidTarget> targets = new ArrayList<>();
        long networkAmount = 0;
        int loadedNodeCount = 0;
        for (var entry : iterateTickingNodes()) {
            FluidNetworkNode fluidNode = (FluidNetworkNode) entry.getNode();
            fluidNode.gatherTargetsAndPickFluid(world, entry.getPos(), targets);
            // Amount goes after the gather...() call because the gather...() call cleans
            // invalid amounts.
            networkAmount += fluidNode.amount;
            loadedNodeCount++;
        }
        long networkCapacity = (long) loadedNodeCount * nodeCapacity;
        FluidVariant fluid = ((FluidNetworkData) data).fluid;

        long extracted = 0, inserted = 0;

        if (!fluid.isBlank()) {
            try (Transaction transaction = Transaction.openOuter()) {
                // Extract from targets into the network
                extracted = transferByPriority(Storage::extract, targets, fluid, networkCapacity - networkAmount, transaction);
                networkAmount += extracted;
                // Insert into the targets from the network
                inserted = transferByPriority(Storage::insert, targets, fluid, networkAmount, transaction);
                networkAmount -= inserted;

                transaction.commit();
            }

            // Split fluid evenly across the nodes
            // Rebalance fluid inside the nodes
            for (var entry : iterateTickingNodes()) {
                FluidNetworkNode fluidNode = (FluidNetworkNode) entry.getNode();
                fluidNode.amount = networkAmount / loadedNodeCount;
                networkAmount -= fluidNode.amount;
                loadedNodeCount--;
            }
        }

        stats.addValue(Math.max(extracted, inserted));

        for (var entry : iterateTickingNodes()) {
            ((FluidNetworkNode) entry.getNode()).afterTick(world, entry.getPos());
        }
    }

    /**
     * Perform a transfer operation for a priority bucket, starting with higher
     * priority targets.
     *
     * @return The amount that was successfully transferred.
     */
    private static long transferByPriority(TransferOperation operation, List<FluidTarget> targets, FluidVariant fluid, long maxAmount,
            TransactionContext transaction) {
        // Sort by decreasing priority
        targets.sort(Comparator.comparingInt(target -> -target.priority));
        // Transfer for each bucket
        long transferredAmount = 0;
        int bucketStart = 0;
        for (int i = 0; i < targets.size(); ++i) {
            if (i == targets.size() - 1 || targets.get(bucketStart).priority != targets.get(i + 1).priority) {
                transferredAmount += transferForBucket(operation, targets.subList(bucketStart, i + 1), fluid, maxAmount - transferredAmount,
                        transaction);
                bucketStart = i + 1;
            }
        }
        return transferredAmount;
    }

    /**
     * Perform a transfer operation for a priority bucket, so {@code bucket} is a
     * sublist of targets with the same priority each.
     * 
     * @return The amount that was successfully transferred.
     */
    private static long transferForBucket(TransferOperation operation, List<FluidTarget> bucket, FluidVariant fluid, long maxAmount,
            TransactionContext transaction) {
        // Shuffle the bucket for better average transfer when simulation returns the
        // same result every time
        Collections.shuffle(bucket);
        // Simulate the transfer for every target
        for (FluidTarget target : bucket) {
            try (Transaction nested = transaction.openNested()) {
                target.simulationResult = operation.transfer(target.storage, fluid, maxAmount, nested);
            }
        }
        // Sort from low result to high result
        bucket.sort(Comparator.comparingLong(target -> target.simulationResult));
        // Actually perform the transfer
        long transferredAmount = 0;
        for (int i = 0; i < bucket.size(); ++i) {
            FluidTarget target = bucket.get(i);
            int remainingTargets = bucket.size() - i;
            long remainingAmount = maxAmount - transferredAmount;
            long targetMaxAmount = remainingAmount / remainingTargets;

            transferredAmount += operation.transfer(target.storage, fluid, targetMaxAmount, transaction);
        }
        return transferredAmount;
    }

    @FunctionalInterface
    private interface TransferOperation {
        long transfer(Storage<FluidVariant> storage, FluidVariant fluid, long maxAmount, TransactionContext transaction);
    }

    @Override
    public PipeNetworkData merge(PipeNetwork other) {
        FluidNetworkData thisData = (FluidNetworkData) data;
        FluidNetworkData otherData = (FluidNetworkData) other.data;
        // If one is empty, it's easy to merge.
        // First check for empty fluid, then also check for empty network the second
        // time
        for (int i = 0; i < 2; ++i) {
            boolean onlyFluid = i == 0;
            if (this.isEmpty(onlyFluid))
                return otherData.clone();
            if (((FluidNetwork) other).isEmpty(onlyFluid))
                return thisData.clone();
        }
        return null;
    }

    private boolean isEmpty(boolean onlyFluid) {
        if (((FluidNetworkData) data).fluid.isBlank())
            return true;
        if (onlyFluid)
            return false;
        for (PipeNetworkNode node : getRawNodeMap().values()) {
            if (node == null || ((FluidNetworkNode) node).amount != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set this network's fluid if this network has an empty fluid.
     */
    protected void setFluid(FluidVariant fluid) {
        if (((FluidNetworkData) data).fluid.isBlank()) {
            ((FluidNetworkData) data).fluid = fluid;
        }
    }

    /**
     * Clear this network of all its fluid if possible.
     */
    protected void clearFluid() {
        // Check that every node is loaded.
        for (PipeNetworkNode node : getRawNodeMap().values()) {
            if (node == null) {
                return;
            }
        }
        // Clear
        for (PipeNetworkNode node : getRawNodeMap().values()) {
            ((FluidNetworkNode) node).amount = 0;
        }
        ((FluidNetworkData) data).fluid = FluidVariant.blank();
    }
}
