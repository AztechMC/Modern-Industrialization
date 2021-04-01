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

import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import java.util.Map;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FluidNetwork extends PipeNetwork {
    final int nodeCapacity;

    public FluidNetwork(int id, PipeNetworkData data, int nodeCapacity) {
        super(id, data == null ? new FluidNetworkData(Fluids.EMPTY) : data);
        this.nodeCapacity = nodeCapacity;
    }

    @Override
    public void tick(World world) {
        // Only tick once
        if (ticked)
            return;
        ticked = true;

        int totalAmount = 0;
        int remainingNodes = 0;
        // Interact with other inventories
        for (Map.Entry<BlockPos, PipeNetworkNode> entry : nodes.entrySet()) {
            if (entry.getValue() != null) {
                FluidNetworkNode fluidNode = (FluidNetworkNode) entry.getValue();
                fluidNode.interactWithConnections(world, entry.getKey());
                totalAmount += fluidNode.amount;
                remainingNodes++;
            }
        }
        // Rebalance fluid inside the nodes
        for (PipeNetworkNode node : nodes.values()) {
            if (node != null) {
                FluidNetworkNode fluidNode = (FluidNetworkNode) node;
                fluidNode.amount = totalAmount / remainingNodes;
                totalAmount -= fluidNode.amount;
                remainingNodes--;
            }
        }
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
        if (((FluidNetworkData) data).fluid == Fluids.EMPTY)
            return true;
        if (onlyFluid)
            return false;
        for (PipeNetworkNode node : nodes.values()) {
            if (node == null || ((FluidNetworkNode) node).amount != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set this network's fluid if this network has an empty fluid.
     */
    protected void setFluid(Fluid fluid) {
        if (((FluidNetworkData) data).fluid == Fluids.EMPTY) {
            ((FluidNetworkData) data).fluid = fluid;
        }
    }

    /**
     * Clear this network of all its fluid if possible.
     */
    protected void clearFluid() {
        // Check that every node is loaded.
        for (PipeNetworkNode node : nodes.values()) {
            if (node == null) {
                return;
            }
        }
        // Clear
        for (PipeNetworkNode node : nodes.values()) {
            ((FluidNetworkNode) node).amount = 0;
        }
        ((FluidNetworkData) data).fluid = Fluids.EMPTY;
    }
}
