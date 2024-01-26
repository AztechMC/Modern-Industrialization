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
import aztech.modern_industrialization.util.MISavedData;
import aztech.modern_industrialization.util.WorldHelper;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;

public class PipeNetworks extends MISavedData {
    private static final SavedData.Factory<PipeNetworks> FACTORY = new SavedData.Factory<>(() -> new PipeNetworks(new HashMap<>()),
            PipeNetworks::readNbt);
    private static final String NAME = "modern_industrialization_pipe_networks";

    private final Map<PipeNetworkType, PipeNetworkManager> managers;
    private final Map<Long, List<Runnable>> loadPipesByChunk = new HashMap<>();

    public PipeNetworks(Map<PipeNetworkType, PipeNetworkManager> managers) {
        this.managers = managers;
    }

    public PipeNetworkManager getManager(PipeNetworkType type) {
        return managers.computeIfAbsent(type, PipeNetworkManager::new);
    }

    @Nullable
    public PipeNetworkManager getOptionalManager(PipeNetworkType type) {
        return managers.get(type);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        for (Map.Entry<PipeNetworkType, PipeNetworkManager> entry : managers.entrySet()) {
            nbt.put(entry.getKey().getIdentifier().toString(), entry.getValue().toTag(new CompoundTag()));
        }
        return nbt;
    }

    public static PipeNetworks readNbt(CompoundTag nbt) {
        Map<PipeNetworkType, PipeNetworkManager> managers = new HashMap<>();
        for (Map.Entry<ResourceLocation, PipeNetworkType> entry : PipeNetworkType.getTypes().entrySet()) {
            PipeNetworkManager manager = new PipeNetworkManager(entry.getValue());
            String tagKey = entry.getKey().toString();
            if (nbt.contains(tagKey)) {
                manager.fromNbt(nbt.getCompound(tagKey));
            }
            managers.put(entry.getValue(), manager);
        }
        return new PipeNetworks(managers);
    }

    public static PipeNetworks get(ServerLevel world) {
        PipeNetworks networks = world.getDataStorage().computeIfAbsent(FACTORY, NAME);
        networks.setDirty();
        return networks;
    }

    public static void scheduleLoadPipe(Level world, PipeBlockEntity pipe) {
        if (world instanceof ServerLevel sw) {
            if (!sw.getServer().isSameThread()) {
                throw new IllegalStateException("Can only load pipe on server from the server thread.");
            }

            PipeNetworks.get(sw).loadPipesByChunk.computeIfAbsent(ChunkPos.asLong(pipe.getBlockPos()), chunk -> new ArrayList<>())
                    .add(pipe::loadPipes);
        }
    }

    static {
        NeoForge.EVENT_BUS.addListener(TickEvent.LevelTickEvent.class, event -> {
            if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) {
                return;
            }

            var world = (ServerLevel) event.level;
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
