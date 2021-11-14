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
package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.api.PipeNetworkManager;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.util.WorldHelper;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class PipeNetworks extends PersistentState {
    private static final String NAME = "modern_industrialization_pipe_networks";
    private final Map<PipeNetworkType, PipeNetworkManager> managers;
    private final Map<Long, List<Runnable>> loadPipesByChunk = new HashMap<>();

    public PipeNetworks(Map<PipeNetworkType, PipeNetworkManager> managers) {
        this.managers = managers;
    }

    public PipeNetworkManager getManager(PipeNetworkType type) {
        return managers.computeIfAbsent(type, PipeNetworkManager::new);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (Map.Entry<PipeNetworkType, PipeNetworkManager> entry : managers.entrySet()) {
            nbt.put(entry.getKey().getIdentifier().toString(), entry.getValue().toTag(new NbtCompound()));
        }
        return nbt;
    }

    public static PipeNetworks readNbt(NbtCompound nbt) {
        Map<PipeNetworkType, PipeNetworkManager> managers = new HashMap<>();
        for (Map.Entry<Identifier, PipeNetworkType> entry : PipeNetworkType.getTypes().entrySet()) {
            PipeNetworkManager manager = new PipeNetworkManager(entry.getValue());
            String tagKey = entry.getKey().toString();
            if (nbt.contains(tagKey)) {
                manager.fromNbt(nbt.getCompound(tagKey));
            }
            managers.put(entry.getValue(), manager);
        }
        return new PipeNetworks(managers);
    }

    public static PipeNetworks get(ServerWorld world) {
        PipeNetworks networks = world.getPersistentStateManager().getOrCreate(PipeNetworks::readNbt, () -> new PipeNetworks(new HashMap<>()), NAME);
        networks.markDirty();
        return networks;
    }

    public static void scheduleLoadPipe(World world, PipeBlockEntity pipe) {
        if (world instanceof ServerWorld sw) {
            if (!sw.getServer().isOnThread()) {
                throw new IllegalStateException("Can only load pipe on server from the server thread.");
            }

            PipeNetworks.get(sw).loadPipesByChunk.computeIfAbsent(ChunkPos.toLong(pipe.getPos()), chunk -> new ArrayList<>()).add(pipe::loadPipes);
        }
    }

    static {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            PipeNetworks networks = PipeNetworks.get(world);

            // Load pipes
            var it = networks.loadPipesByChunk.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, List<Runnable>> chunkEntry = it.next();
                if (WorldHelper.isChunkTicking(world, chunkEntry.getKey())) {
                    chunkEntry.getValue().forEach(Runnable::run);
                    it.remove();
                }
            }

            // Tick networks
            for (PipeNetworkManager manager : networks.managers.values()) {
                manager.tickNetworks(world);
            }
        });
    }
}
