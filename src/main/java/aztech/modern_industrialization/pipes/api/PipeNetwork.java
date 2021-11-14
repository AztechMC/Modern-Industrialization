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
import java.util.function.BiConsumer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

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

    public void fromTag(NbtCompound tag) {
        id = tag.getInt("id");
        data.fromTag(tag.getCompound("data"));
    }

    public NbtCompound toTag(NbtCompound tag) {
        tag.putInt("id", id);
        tag.put("data", data.toTag(new NbtCompound()));
        return tag;
    }

    public void tick(ServerWorld world) {
    }

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

    @Nullable
    public PipeNetworkNode getNode(BlockPos pos) {
        return this.nodes.get(pos);
    }

    public void setNode(BlockPos pos, @Nullable PipeNetworkNode node) {
        this.nodes.put(pos.toImmutable(), node);

        this.nodesByChunk.computeIfAbsent(ChunkPos.toLong(pos), p -> new HashMap<>()).put(pos.toImmutable(), node);
    }

    public void removeNode(BlockPos pos) {
        this.nodes.remove(pos);

        long chunk = ChunkPos.toLong(pos);
        Map<BlockPos, PipeNetworkNode> map = nodesByChunk.get(chunk);
        map.remove(pos);
        if (map.size() == 0) {
            nodesByChunk.remove(chunk);
        }
    }

    public Map<BlockPos, PipeNetworkNode> getRawNodeMap() {
        return Collections.unmodifiableMap(this.nodes);
    }

    protected Collection<PosNode> iterateTickingNodes() {
        if (!tickingCacheValid) {
            tickingNodesCache.clear();
            for (var chunkEntry : this.nodesByChunk.entrySet()) {
                if (manager.tickingChunks.contains(chunkEntry.getKey())) {
                    for (var entry : chunkEntry.getValue().entrySet()) {
                        tickingNodesCache.add(new PosNode(entry.getKey(), entry.getValue()));
                    }
                }
            }
            tickingCacheValid = true;
        }
        return tickingNodesCache;
    }

    protected void forEachTickingNode(BiConsumer<BlockPos, PipeNetworkNode> consumer) {
        for (var chunkEntry : this.nodesByChunk.entrySet()) {
            if (manager.tickingChunks.contains(chunkEntry.getKey())) {
                chunkEntry.getValue().forEach(consumer);
            }
        }
    }

    protected static class PosNode {
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
