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

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

/**
 * A pipe network. It is very important that you create a new empty data object
 * if your constructor was passed null.
 */
public abstract class PipeNetwork {
    protected int id;
    public PipeNetworkManager manager;
    public PipeNetworkData data;
    private final Map<BlockPos, PipeNetworkNode> nodes = new HashMap<>();
    private final Map<Long, Map<BlockPos, PipeNetworkNode>> nodesByChunk = new HashMap<>();
    private final List<PosNode> tickingNodesCache = new ArrayList<>();
    boolean tickingCacheValid = false;

    public PipeNetwork(int id, PipeNetworkData data) {
        this.id = id;
        this.data = data;
    }

    public void fromTag(CompoundTag tag, HolderLookup.Provider registries) {
        id = tag.getInt("id");
        data.fromTag(tag.getCompound("data"), registries);
    }

    public CompoundTag toTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("id", id);
        tag.put("data", data.toTag(new CompoundTag(), registries));
        return tag;
    }

    /**
     * <b>Only access nodes that are ticking, for example with {@link #iterateTickingNodes}!</b>
     */
    public void tick(ServerLevel world) {}

    /**
     * Allow merging networks when the player explicitly requests to do so. When
     * this function is called, it must return a new PipeNetworkData without
     * modifying either itself or its parameter.
     *
     * @return null if there can be no merge, or the new pipe network data should
     *         there be a merge.
     */
    public PipeNetworkData merge(PipeNetwork other) {
        return null;
    }

    /**
     * Called when the network is removed from the world.
     * At that point, all the nodes are already gone.
     */
    public void onRemove() {}

    @Nullable
    public PipeNetworkNode getNode(BlockPos pos) {
        return this.nodes.get(pos);
    }

    public void setNode(BlockPos pos, @Nullable PipeNetworkNode node) {
        this.nodes.put(pos.immutable(), node);

        this.nodesByChunk.computeIfAbsent(ChunkPos.asLong(pos), p -> new HashMap<>()).put(pos.immutable(), node);
    }

    public void removeNode(BlockPos pos) {
        this.nodes.remove(pos);

        long chunk = ChunkPos.asLong(pos);
        Map<BlockPos, PipeNetworkNode> map = nodesByChunk.get(chunk);
        map.remove(pos);
        if (map.size() == 0) {
            nodesByChunk.remove(chunk);
        }
    }

    public Map<BlockPos, PipeNetworkNode> getRawNodeMap() {
        return Collections.unmodifiableMap(this.nodes);
    }

    public Collection<PosNode> iterateTickingNodes() {
        if (!tickingCacheValid) {
            tickingNodesCache.clear();
            for (var chunkEntry : this.nodesByChunk.entrySet()) {
                // noinspection deprecation
                if (manager.tickingChunks.contains(chunkEntry.getKey())) {
                    for (var entry : chunkEntry.getValue().entrySet()) {
                        var node = entry.getValue();
                        // no idea how the chunk can be ticking and the node null,
                        // but it happens on the aof5 public server apparently...
                        if (node != null) {
                            tickingNodesCache.add(new PosNode(entry.getKey(), node));
                        }
                    }
                }
            }
            tickingCacheValid = true;
        }
        return tickingNodesCache;
    }

    public static class PosNode {
        private final BlockPos pos;
        private final PipeNetworkNode node;

        public PosNode(BlockPos pos, PipeNetworkNode node) {
            this.pos = pos;
            this.node = node;
        }

        public BlockPos getPos() {
            return pos;
        }

        public PipeNetworkNode getNode() {
            return node;
        }
    }
}
