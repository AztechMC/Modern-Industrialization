package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import net.minecraft.fluid.Fluids;

public class FluidNetwork extends PipeNetwork {
    public FluidNetwork(int id, PipeNetworkData data) {
        super(id, data == null ? new FluidNetworkData(Fluids.EMPTY, FluidUnit.DROPS_PER_BUCKET) : data);
    }

    public void tick() {
        // Only tick once
        if(ticked) return;
        ticked = true;

        int totalAmount = 0;
        int remainingNodes = 0;
        // Interact with other inventories
        for(PipeNetworkNode node : nodes.values()) {
            if(node != null) {
                FluidNetworkNode fluidNode = (FluidNetworkNode) node;
                fluidNode.interactWithConnections();
                totalAmount += fluidNode.amount;
                remainingNodes++;
            }
        }
        // Rebalance fluid inside the nodes
        for(PipeNetworkNode node : nodes.values()) {
            if(node != null) {
                FluidNetworkNode fluidNode = (FluidNetworkNode) node;
                fluidNode.amount = totalAmount / remainingNodes;
                totalAmount -= fluidNode.amount;
                remainingNodes--;
            }
        }
    }
}
