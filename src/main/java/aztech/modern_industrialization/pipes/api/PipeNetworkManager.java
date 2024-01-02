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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

public class PipeNetworkManager {
    private static final boolean DEBUG_CHECKS = !FMLEnvironment.production;

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
    public void tickNetworks(ServerLevel world) {
        // Mark ticking chunks
        updateTickingChunks(world);

        // Actual ticking
        for (PipeNetwork network : networks) {
            network.tick(world);
        }

        // Mark pipes in ticking chunks as dirty.
        for (long chunkPos : tickingChunks) {
            int chunkX = ChunkPos.getX(chunkPos);
            int chunkZ = ChunkPos.getZ(chunkPos);
            var chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            if (chunk != null) {
                chunk.setUnsaved(true);
            } else {
                // This is not supposed to happen.
                var sb = new StringBuilder();
                sb.append("MI pipes issue: ticking spanned chunk was not loaded anymore. Please report this.\n");
                sb.append(" - Pipe type: ").append(type.getIdentifier()).append("\n");
                sb.append(" - Chunk: %d,%d\n".formatted(chunkX, chunkZ));
                sb.append(" - Blocks in chunk:\n");
                for (var it = spannedChunks.get(chunkPos).stream().sorted().iterator(); it.hasNext();) {
                    var pos = it.next();
                    sb.append("   - Pos: %d %d %d\n".formatted(pos.getX(), pos.getY(), pos.getZ()));
                    var network = networkByBlock.get(pos);
                    var node = network == null ? "none" : network.getNode(pos) == null ? "not loaded" : "loaded";
                    sb.append("   - Has network (should be true): %s\n".formatted(network != null));
                    sb.append("   - Node status (should be loaded): %s\n".formatted(node));
                }
                throw new UnsupportedOperationException(sb.toString());
            }
        }
    }

    public boolean hasNode(BlockPos pos) {
        return networkByBlock.containsKey(pos);
    }

    private void updateTickingChunks(ServerLevel world) {
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
        BlockPos otherPos = pos.relative(direction);
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
            var nodesCopy = new ArrayList<>(otherNetwork.getRawNodeMap().keySet());
            for (var nodePos : nodesCopy) {
                otherNetwork.removeNode(nodePos);
            }
            otherNetwork.onRemove();
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
        BlockPos otherPos = pos.relative(direction);
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
                    dfs(currentPos.relative(direction));
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
     * Check if a link exists.
     */
    public boolean hasLink(BlockPos pos, Direction direction) {
        var nodeLinks = links.get(pos);
        return nodeLinks != null && nodeLinks.contains(direction);
    }

    /**
     * Check if a link would be possible. A node must exist at pos.
     */
    public boolean canLink(BlockPos pos, Direction direction, boolean forceLink) {
        BlockPos otherPos = pos.relative(direction);
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
        networkByBlock.put(pos.immutable(), network);
        incrementSpanned(pos);
        network.setNode(pos, node);
        links.put(pos.immutable(), new HashSet<>());
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
        network.onRemove();
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
            PipeNetworkData data = MIPipes.INSTANCE.getPipeItem(getType()).defaultData.clone();
            addNode(node, pos, data);
            for (Direction direction : Direction.values()) {
                addLink(pos, direction, false);
            }
        } else {
            node.network = network;
            network.setNode(pos, node);
            network.tickingCacheValid = false;
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
        node.network.tickingCacheValid = false;
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
        spannedChunks.computeIfAbsent(ChunkPos.asLong(pos), p -> new HashSet<>()).add(pos.immutable());
    }

    private void decrementSpanned(BlockPos pos) {
        long chunkPos = ChunkPos.asLong(pos);
        Set<BlockPos> set = spannedChunks.get(chunkPos);
        set.remove(pos);
        if (set.size() == 0) {
            spannedChunks.remove(chunkPos);
        }
    }

    public void fromNbt(CompoundTag tag) {
        // networks
        ListTag networksTag = tag.getList("networks", new CompoundTag().getId());
        for (Tag networkTag : networksTag) {
            PipeNetwork network = type.getNetworkCtor().apply(-1, null);
            network.manager = this;
            network.fromTag((CompoundTag) networkTag);
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

    public CompoundTag toTag(CompoundTag tag) {
        // networks
        List<CompoundTag> networksTags = new ArrayList<>();
        for (PipeNetwork network : networks) {
            networksTags.add(network.toTag(new CompoundTag()));
        }
        ListTag networksTag = new ListTag();
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
        if (!DEBUG_CHECKS) {
            return;
        }

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
