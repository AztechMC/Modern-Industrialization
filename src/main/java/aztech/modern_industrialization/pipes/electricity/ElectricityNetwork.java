package aztech.modern_industrialization.pipes.electricity;

import aztech.modern_industrialization.api.CableTier;
import aztech.modern_industrialization.api.EnergyExtractable;
import aztech.modern_industrialization.api.EnergyInsertable;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElectricityNetwork extends PipeNetwork {
    final CableTier tier;

    public ElectricityNetwork(int id, PipeNetworkData data, CableTier tier) {
        super(id, data == null ? new ElectricityNetworkData() : data);
        this.tier = tier;
    }

    @Override
    public void tick(World world) {
        // Only tick once
        if(ticked) return;
        ticked = true;

        List<EnergyInsertable> insertables = new ArrayList<>();
        List<EnergyExtractable> extractables = new ArrayList<>();
        long networkAmount = 0;
        long remainingInsert = 0;
        int loadedNodes = 0;
        for(Map.Entry<BlockPos, PipeNetworkNode> entry : nodes.entrySet()) {
            if(entry.getValue() != null) {
                ElectricityNetworkNode node = (ElectricityNetworkNode) entry.getValue();
                node.appendAttributes(world, entry.getKey(), insertables, extractables);
                networkAmount += node.eu;
                remainingInsert += tier.getMaxInsert() - node.eu;
                loadedNodes++;
            }
        }
        remainingInsert = Math.min(remainingInsert, tier.getMaxInsert());

        for(EnergyExtractable extractable : extractables) {
            long ext = extractable.extractEnergy(remainingInsert);
            remainingInsert -= ext;
            networkAmount += ext;
        }

        for(EnergyInsertable insertable : insertables) {
            if(insertable.canInsert(tier)) {
                networkAmount = insertable.insertEnergy(networkAmount);
            }
        }

        for(PipeNetworkNode node : nodes.values()) {
            if(node != null) {
                ElectricityNetworkNode electricityNode = (ElectricityNetworkNode) node;
                electricityNode.eu = networkAmount / loadedNodes;
                networkAmount -= electricityNode.eu;
                --loadedNodes;
            }
        }
    }
}
