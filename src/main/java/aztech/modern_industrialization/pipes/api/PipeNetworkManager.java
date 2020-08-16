package aztech.modern_industrialization.pipes.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

public class PipeNetworkManager {
    private Map<BlockPos, PipeNetwork> networkByBlock = new HashMap<>();
    private Set<PipeNetwork> networks = new HashSet<>();
    private int nextNetworkId = 0;
    private PipeNetworkType type;

    public PipeNetworkManager(PipeNetworkType type) {
        this.type = type;
    }

    /**
     * Add a node to the networks.
     */
    public void addNode(PipeNetworkNode node, BlockPos pos, PipeNetworkData data) {
        PipeNetwork nodeNetwork = null;

        // Try to link with existing networks
        for(Direction direction : Direction.values()) {
            PipeNetwork network = networkByBlock.get(pos.offset(direction));
            if(network != null) {
                if(network.data.equals(data)) {
                    if(nodeNetwork == null) {
                        assignNode(network, node, pos);
                        nodeNetwork = network;
                    } else {
                        mergeNetworks(nodeNetwork, network);
                    }
                }
            }
        }

        // If we couldn't link, create a new network instead.
        if(nodeNetwork == null) {
            nodeNetwork = createNetwork(data);
            assignNode(nodeNetwork, node, pos);
        }
    }

    /**
     * Remove a node from the networks.
     */
    public void removeNode(PipeNetworkNode node, BlockPos pos) {
        PipeNetwork network = networkByBlock.get(pos);
        networkByBlock.remove(pos);
        network.nodes.remove(pos);
        node.network = null;
        // If the node was alone, remove the network
        if(network.nodes.isEmpty()) {
            destroyNetwork(network);
        }
        // Otherwise, run a DFS to split the networks correctly
        else {
            Map<BlockPos, PipeNetworkNode> unassignedNodes = network.nodes;
            network.nodes = new HashMap<>();
            // The network we are currently adding nodes to. It is final for use in inner class
            final PipeNetwork[] newNetwork = new PipeNetwork[] { network };

            class Dfs {
                private void dfs(BlockPos currentPos) {
                    PipeNetworkNode node = unassignedNodes.remove(currentPos);
                    // If the node is null, it was already assigned.
                    if(node == null) return;
                    // Otherwise, assign it and move to a neighbor.
                    assignNode(newNetwork[0], node, currentPos);
                    for(Direction direction : Direction.values()) {
                        dfs(currentPos.offset(direction));
                    }
                }
            }

            Dfs dfs = new Dfs();
            while(!unassignedNodes.isEmpty()) {
                // Get any key in the map as a starting pos
                BlockPos startingPos = unassignedNodes.entrySet().iterator().next().getKey();
                // Create new network if needed
                if(newNetwork[0] == null) {
                    newNetwork[0] = createNetwork(network.data.clone());
                }
                // Run dfs
                dfs.dfs(startingPos);
                newNetwork[0] = null;
            }
        }
    }

    /**
     * Should be called when a node is loaded, it will link the node to its network.
     */
    public void nodeLoaded(PipeNetworkNode node, BlockPos pos) {
        PipeNetwork network = networkByBlock.get(pos);
        node.network = network;
        network.nodes.put(pos.toImmutable(), node);
    }

    /**
     * Should be called when a node is unloaded, it will unlink the node from its network.
     */
    public void nodeUnloaded(PipeNetworkNode node, BlockPos pos) {
        node.network.nodes.remove(pos);
    }

    /**
     * Assign a node to a network.
     */
    private void assignNode(PipeNetwork network, PipeNetworkNode node, BlockPos pos) {
        pos = pos.toImmutable();
        PipeNetwork previousNetwork = networkByBlock.put(pos, network);
        network.nodes.put(pos, node);
        node.network = network;
        if(previousNetwork != null && previousNetwork != network) {
            previousNetwork.nodes.remove(pos);
            if(previousNetwork.nodes.size() == 0) {
                destroyNetwork(previousNetwork);
            }
        }
    }

    /**
     * Create a new empty network.
     */
    private PipeNetwork createNetwork(PipeNetworkData data) {
        PipeNetwork network = type.getNetworkCtor().apply(nextNetworkId, data);
        network.type = type;
        nextNetworkId++;
        networks.add(network);
        return network;
    }

    /**
     * Destroy an empty network.
     */
    private void destroyNetwork(PipeNetwork network) {
        networks.remove(network);
    }

    /**
     * Merge child network into parent network.
     */
    private void mergeNetworks(PipeNetwork parent, PipeNetwork child) {
        while(child.nodes.size() > 0) {
            Map.Entry<BlockPos, PipeNetworkNode> entry = child.nodes.entrySet().iterator().next();
            assignNode(parent, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Mark the networks as unticked.
     */
    public void markNetworksAsUnticked() {
        for(PipeNetwork network : networks) {
            network.ticked = false;
        }
    }

    public void fromTag(CompoundTag tag) {
        // networks
        ListTag networksTag = tag.getList("networks", new CompoundTag().getType());
        for(Tag networkTag : networksTag) {
            PipeNetwork network = type.getNetworkCtor().apply(-1, null);
            network.type = type;
            network.fromTag((CompoundTag) networkTag);
            networks.add(network);
        }

        // network_by_block
        Map<Integer, PipeNetwork> networkIds = new HashMap<>();
        for(PipeNetwork network : networks) {
            networkIds.put(network.id, network);
        }
        int[] data = tag.getIntArray("networkByBlock");
        for(int i = 0; i < data.length/4; i++) {
            PipeNetwork network = networkIds.get(data[4*i+3]);
            BlockPos pos = new BlockPos(data[4*i], data[4*i+1], data[4*i+2]);
            networkByBlock.put(pos, network);
            network.nodes.put(pos, null);
        }

        // nextNetworkId
        nextNetworkId = tag.getInt("nextNetworkId");
    }

    public CompoundTag toTag(CompoundTag tag) {
        // networks
        List<CompoundTag> networksTags = new ArrayList<>();
        for(PipeNetwork network : networks) {
            networksTags.add(network.toTag(new CompoundTag()));
        }
        ListTag networksTag = new ListTag();
        networksTag.addAll(networksTags);
        tag.put("networks", networksTag);

        // network_by_block, every entry is identified by four consecutive integers: x, y, z, network id
        int[] networkByBlockData = new int[networkByBlock.size() * 4];
        int i = 0;
        for(Map.Entry<BlockPos, PipeNetwork> entry : networkByBlock.entrySet()) {
            networkByBlockData[i++] = entry.getKey().getX();
            networkByBlockData[i++] = entry.getKey().getY();
            networkByBlockData[i++] = entry.getKey().getZ();
            networkByBlockData[i++] = entry.getValue().id;
        }
        tag.putIntArray("networkByBlock", networkByBlockData);

        // nextNetworkId
        tag.putInt("nextNetworkId", nextNetworkId);
        return tag;
    }
}
