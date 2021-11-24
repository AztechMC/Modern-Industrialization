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
package aztech.modern_industrialization.pipes.api;

import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.WorldHelper;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.*;
import java.util.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class PipeNetworkManager {
    private final Map<BlockPos, PipeNetwork> networkByBlock = new HashMap<>();
    private final Map<BlockPos, Set<Direction>> links = new HashMap<>();
    private final Set<PipeNetwork> networks = new HashSet<>();
    private int nextNetworkId = 0;
    private final PipeNetworkType type;

    private final Map<Long, Set<BlockPos>> spannedChunks = new HashMap<>();
    protected LongSet tickingChunks = new LongOpenHashSet();
    protected LongSet lastTickingChunks = new LongOpenHashSet();

    public PipeNetworkManager(PipeNetworkType type) {
        this.type = type;
    }

    /**
     * Tick networks
     */
    public void tickNetworks(ServerWorld world) {
        // Mark ticking chunks
        updateTickingChunks(world);

        // Actual ticking
        for (PipeNetwork network : networks) {
            network.tick(world);
        }

        // Mark spanned chunks as dirty.
        for (long chunk : spannedChunks.keySet()) {
            int chunkX = ChunkPos.getPackedX(chunk);
            int chunkZ = ChunkPos.getPackedZ(chunk);
            if (world.isChunkLoaded(chunkX, chunkZ)) {
                world.getChunk(chunkX, chunkZ).markDirty();
            } else {
                throw new UnsupportedOperationException("Internal MI pipe bug: spanned chunk was not loaded anymore.");
            }
        }
    }

    private void updateTickingChunks(ServerWorld world) {
        var tmp = tickingChunks;
        tickingChunks = lastTickingChunks;
        lastTickingChunks = tmp;
        Preconditions.checkState(tickingChunks.isEmpty(), "Internal pipe network error.");

        for (Map.Entry<Long, Set<BlockPos>> entry : spannedChunks.entrySet()) {
            long chunk = entry.getKey();
            if (WorldHelper.isChunkTicking(world, chunk)) {
                tickingChunks.add(chunk);

                if (!lastTickingChunks.remove(chunk)) {
                    // New ticking chunk
                    notifyTickingChanged(entry.getValue());
                }
            }
        }
        // Chunk that isn't ticking anymore
        for (Long notTickingChunk : lastTickingChunks) {
            notifyTickingChanged(spannedChunks.get(notTickingChunk));
        }
        lastTickingChunks.clear();
    }

    private void notifyTickingChanged(@Nullable Set<BlockPos> positionsInChunk) {
        if (positionsInChunk != null) {
            for (BlockPos pos : positionsInChunk) {
                PipeNetwork network = networkByBlock.get(pos);
                network.tickingCacheValid = false;
            }
        }
    }

    /**
     * Add a network link and merge networks if necessary. Both the node at pos and
     * the node at pos + direction must exist in the network.
     */
    public void addLink(BlockPos pos, Direction direction, boolean force) {
        if (hasLink(pos, direction))
            return;
        if (!canLink(pos, direction, force))
            return;

        // Add links
        BlockPos otherPos = pos.offset(direction);
        links.get(pos).add(direction);
        links.get(otherPos).add(direction.getOpposite());

        // If the networks are different, we merge all nodes into `network`. We don't
        // change other links.
        PipeNetwork network = networkByBlock.get(pos);
        PipeNetwork otherNetwork = networkByBlock.get(otherPos);
        if (network != otherNetwork) {
            if (!network.data.equals(otherNetwork.data)) {
                network.data = network.merge(otherNetwork);
            }
            for (Map.Entry<BlockPos, PipeNetworkNode> entry : otherNetwork.getRawNodeMap().entrySet()) {
                PipeNetworkNode node = entry.getValue();
                BlockPos nodePos = entry.getKey();
                if (node != null) {
                    node.network = network;
                }
                networkByBlock.put(nodePos, network);
                network.setNode(nodePos, node);
            }
            networks.remove(otherNetwork);
        }
        network.tickingCacheValid = false;
        checkStateCoherence();
    }

    /**
     * Remove a network link and split networks if necessary. Both the node at pos
     * and the node at pos + direction must exist in the network.
     */
    public void removeLink(BlockPos pos, Direction direction) {
        if (!hasLink(pos, direction))
            return;

        // Remove links
        BlockPos otherPos = pos.offset(direction);
        links.get(pos).remove(direction);
        links.get(otherPos).remove(direction.getOpposite());

        // Run a DFS to mark all disconnected nodes.
        PipeNetwork network = networkByBlock.get(pos);
        Map<BlockPos, PipeNetworkNode> unvisitedNodes = new HashMap<>(network.getRawNodeMap());
        network.tickingCacheValid = false;

        class Dfs {
            private void dfs(BlockPos currentPos) {
                // warning: don't try to use the return value of Map#remove, because it might be
                // null if the node is not loaded.
                if (!unvisitedNodes.containsKey(currentPos)) {
                    return;
                }
                unvisitedNodes.remove(currentPos);
                for (Direction direction : links.get(currentPos)) {
                    dfs(currentPos.offset(direction));
                }
            }
        }

        // Try to put all nodes in the current network
        Dfs dfs = new Dfs();
        dfs.dfs(pos);

        // If it was not possible, create a new network and transfer all unvisitedNodes
        // to it.
        if (unvisitedNodes.size() > 0) {
            PipeNetwork newNetwork = createNetwork(network.data.clone());
            for (Map.Entry<BlockPos, PipeNetworkNode> entry : unvisitedNodes.entrySet()) {
                PipeNetworkNode node = entry.getValue();
                BlockPos nodePos = entry.getKey();
                if (node != null) {
                    node.network = newNetwork;
                }
                networkByBlock.put(nodePos, newNetwork);
                newNetwork.setNode(nodePos, node);
                network.removeNode(nodePos);
            }
        }
        checkStateCoherence();
    }

    /**
     * Check if a link exists. A node must exist at pos.
     */
    public boolean hasLink(BlockPos pos, Direction direction) {
        return links.get(pos).contains(direction);
    }

    /**
     * Check if a link would be possible. A node must exist at pos.
     */
    public boolean canLink(BlockPos pos, Direction direction, boolean forceLink) {
        BlockPos otherPos = pos.offset(direction);
        PipeNetwork network = networkByBlock.get(pos);
        PipeNetwork otherNetwork = networkByBlock.get(otherPos);
        return otherNetwork != null && (network.data.equals(otherNetwork.data) || forceLink && network.merge(otherNetwork) != null);
    }

    /**
     * Add a node and create a new network for it.
     */
    public void addNode(PipeNetworkNode node, BlockPos pos, PipeNetworkData data) {
        if (networkByBlock.containsKey(pos))
            throw new IllegalArgumentException("Cannot add a node that is already in the network.");

        PipeNetwork network = createNetwork(data.clone());
        if (node != null) {
            node.network = network;
        }
        networkByBlock.put(pos.toImmutable(), network);
        incrementSpanned(pos);
        network.setNode(pos, node);
        links.put(pos.toImmutable(), new HashSet<>());
        checkStateCoherence();
    }

    /**
     * Remove a node and its network. Will remove all remaining links.
     */
    public void removeNode(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            removeLink(pos, direction);
        }

        PipeNetwork network = networkByBlock.remove(pos);
        decrementSpanned(pos);
        networks.remove(network);
        links.remove(pos);
        checkStateCoherence();
    }

    /**
     * Should be called when a node is loaded, it will link the node to its network.
     */
    public void nodeLoaded(PipeNetworkNode node, BlockPos pos) {
        PipeNetwork network = networkByBlock.get(pos);
        if (network == null) {
            // The network is null! That probably means that the node doesn't exist, e.g.
            // because a pipe was moved with Carrier.
            // If that happens, we just create the node here. Hopefully it goes well.
            // TODO: refactor this in an api
            PipeNetworkData data = MIPipes.INSTANCE.getPipeItem(getType()).defaultData;
            addNode(node, pos, data);
            for (Direction direction : Direction.values()) {
                addLink(pos, direction, false);
            }
        } else {
            node.network = network;
            network.setNode(pos, node);
        }
        incrementSpanned(pos);
        checkStateCoherence();
    }

    /**
     * Should be called when a node is unloaded, it will unlink the node from its
     * network.
     */
    public void nodeUnloaded(PipeNetworkNode node, BlockPos pos) {
        node.network.setNode(pos, null);
        decrementSpanned(pos);
        checkStateCoherence();
    }

    /**
     * Create a new empty network.
     */
    private PipeNetwork createNetwork(PipeNetworkData data) {
        PipeNetwork network = type.getNetworkCtor().apply(nextNetworkId, data);
        network.manager = this;
        nextNetworkId++;
        networks.add(network);
        checkStateCoherence();
        return network;
    }

    private void incrementSpanned(BlockPos pos) {
        spannedChunks.computeIfAbsent(ChunkPos.toLong(pos), p -> new HashSet<>()).add(pos.toImmutable());
    }

    private void decrementSpanned(BlockPos pos) {
        long chunkPos = ChunkPos.toLong(pos);
        Set<BlockPos> set = spannedChunks.get(chunkPos);
        set.remove(pos);
        if (set.size() == 0) {
            spannedChunks.remove(chunkPos);
        }
    }

    public void fromNbt(NbtCompound tag) {
        // networks
        NbtList networksTag = tag.getList("networks", new NbtCompound().getType());
        for (NbtElement networkTag : networksTag) {
            PipeNetwork network = type.getNetworkCtor().apply(-1, null);
            network.manager = this;
            network.fromTag((NbtCompound) networkTag);
            networks.add(network);
        }

        // networkByBlock and links
        Map<Integer, PipeNetwork> networkIds = new HashMap<>();
        for (PipeNetwork network : networks) {
            networkIds.put(network.id, network);
        }
        int[] data = tag.getIntArray("networkByBlock");
        for (int i = 0; i < data.length / 5; i++) {
            PipeNetwork network = networkIds.get(data[5 * i + 3]);
            BlockPos pos = new BlockPos(data[5 * i], data[5 * i + 1], data[5 * i + 2]);
            networkByBlock.put(pos, network);
            network.setNode(pos, null);
            links.put(pos, new HashSet<>(Arrays.asList(NbtHelper.decodeDirections((byte) data[5 * i + 4]))));
        }

        // nextNetworkId
        nextNetworkId = tag.getInt("nextNetworkId");
        checkStateCoherence();
    }

    public NbtCompound toTag(NbtCompound tag) {
        // networks
        List<NbtCompound> networksTags = new ArrayList<>();
        for (PipeNetwork network : networks) {
            networksTags.add(network.toTag(new NbtCompound()));
        }
        NbtList networksTag = new NbtList();
        networksTag.addAll(networksTags);
        tag.put("networks", networksTag);

        // networkByBlock and links, every entry is identified by five consecutive
        // integers: x, y, z, network id, encoded links
        int[] networkByBlockData = new int[networkByBlock.size() * 5];
        int i = 0;
        for (Map.Entry<BlockPos, PipeNetwork> entry : networkByBlock.entrySet()) {
            networkByBlockData[i++] = entry.getKey().getX();
            networkByBlockData[i++] = entry.getKey().getY();
            networkByBlockData[i++] = entry.getKey().getZ();
            networkByBlockData[i++] = entry.getValue().id;
            networkByBlockData[i++] = NbtHelper.encodeDirections(links.get(entry.getKey()));
        }
        tag.putIntArray("networkByBlock", networkByBlockData);

        // nextNetworkId
        tag.putInt("nextNetworkId", nextNetworkId);
        checkStateCoherence();
        return tag;
    }

    public PipeNetworkType getType() {
        return type;
    }

    public Set<Direction> getNodeLinks(BlockPos pos) {
        return new HashSet<>(links.get(pos));
    }

    /**
     * Check all internal state coherence for debugging purposes.
     */
    public void checkStateCoherence() {
        customAssert(networkByBlock.keySet().equals(links.keySet()));
        for (Map.Entry<BlockPos, PipeNetwork> entry : networkByBlock.entrySet()) {
            customAssert(networks.contains(entry.getValue()));
            PipeNetworkNode node = entry.getValue().getNode(entry.getKey());
            customAssert(node == null || node.network == entry.getValue());
        }
        for (Map.Entry<BlockPos, Set<Direction>> entry : links.entrySet()) {
            customAssert(entry.getValue() != null);
        }
        for (PipeNetwork network : networks) {
            for (Map.Entry<BlockPos, PipeNetworkNode> entry : network.getRawNodeMap().entrySet()) {
                customAssert(entry.getValue() == null || entry.getValue().network == network);
                customAssert(networkByBlock.get(entry.getKey()) == network);
            }
        }
    }

    private void customAssert(boolean predicate) {
        if (!predicate)
            throw new NullPointerException("Predicate was false");
    }
}
