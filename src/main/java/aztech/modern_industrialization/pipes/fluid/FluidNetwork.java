package aztech.modern_industrialization.pipes.fluid;

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class FluidNetwork extends PipeNetwork {
    final int nodeCapacity;

    public FluidNetwork(int id, PipeNetworkData data, int nodeCapacity) {
        super(id, data == null ? new FluidNetworkData(FluidKeys.EMPTY) : data);
        this.nodeCapacity = nodeCapacity;
    }

    @Override
    public void tick(World world) {
        // Only tick once
        if(ticked) return;
        ticked = true;

        int totalAmount = 0;
        int remainingNodes = 0;
        // Interact with other inventories
        for(Map.Entry<BlockPos, PipeNetworkNode> entry : nodes.entrySet()) {
            if(entry.getValue() != null) {
                FluidNetworkNode fluidNode = (FluidNetworkNode) entry.getValue();
                fluidNode.interactWithConnections(world, entry.getKey());
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

    @Override
    public PipeNetworkData merge(PipeNetwork other) {
        FluidNetworkData thisData = (FluidNetworkData) data;
        FluidNetworkData otherData = (FluidNetworkData) other.data;
        // If one is empty, it's easy to merge.
        if(this.isEmpty()) return otherData.clone();
        if(((FluidNetwork) other).isEmpty()) return thisData.clone();
        return null;
    }

    private boolean isEmpty() {
        if(((FluidNetworkData) data).fluid.isEmpty()) return true;
        for(PipeNetworkNode node : nodes.values()) {
            if(node == null || ((FluidNetworkNode) node).amount != 0) {
                return false;
            }
        }
        return true;
    }
}
