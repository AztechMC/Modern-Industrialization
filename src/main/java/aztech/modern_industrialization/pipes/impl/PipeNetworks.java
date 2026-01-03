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

import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import aztech.modern_industrialization.pipes.api.PipeNetworkManager;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.util.WorldHelper;
import java.util.*;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PipeNetworks extends SavedData {
    private static final Codec<PipeNetworks> CODEC = Codec.dispatchedMap(PipeNetworkType.CODEC, PipeNetworkManager::codec)
            .xmap(map -> new PipeNetworks(new HashMap<>(map)), n -> n.managers);
    private static final SavedDataType<PipeNetworks> TYPE = new SavedDataType<>(
            "modern_industrialization_pipe_networks",
            () -> new PipeNetworks(new HashMap<>()),
            CODEC);

    private final Map<PipeNetworkType, PipeNetworkManager> managers;
    private final Map<Long, List<Runnable>> loadPipesByChunk = new HashMap<>();

    public PipeNetworks(Map<PipeNetworkType, PipeNetworkManager> managers) {
        this.managers = managers;
        for (PipeNetworkType type : PipeNetworkType.getTypes().values()) {
            if (!managers.containsKey(type)) {
                managers.put(type, new PipeNetworkManager(type));
            }
        }
    }

    public PipeNetworkManager getManager(PipeNetworkType type) {
        return managers.computeIfAbsent(type, PipeNetworkManager::new);
    }

    @Nullable
    public PipeNetworkManager getOptionalManager(PipeNetworkType type) {
        return managers.get(type);
    }

    public static PipeNetworks get(ServerLevel world) {
        PipeNetworks networks = world.getDataStorage().computeIfAbsent(TYPE);
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
        NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, event -> {
            if (!(event.getLevel() instanceof ServerLevel world)) {
                return;
            }

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
