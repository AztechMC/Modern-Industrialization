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
package aztech.modern_industrialization.machines.multiblocks.world;

import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ChunkEventListeners {
    public static ChunkPosMultiMap<ChunkEventListener> listeners = new ChunkPosMultiMap<>();
    private static MinecraftServer server = null;

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> server = minecraftServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(minecraftServer -> server = null);
        ServerLifecycleEvents.SERVER_STOPPED.register(minecraftServer -> serverStopCleanup());

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            ensureServerThread();
            Set<ChunkEventListener> cels = listeners.get(world, chunk.getPos());
            if (cels != null) {
                for (ChunkEventListener cel : cels) {
                    cel.onLoad();
                }
            }
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            ensureServerThread();
            Set<ChunkEventListener> cels = listeners.get(world, chunk.getPos());
            if (cels != null) {
                for (ChunkEventListener cel : cels) {
                    cel.onUnload();
                }
            }
        });
    }

    public static void onBlockStateChange(Level world, ChunkPos chunkPos, BlockPos pos) {
        // We skip block state changes that happen outside of the server thread.
        // Hopefully that won't cause problems.
        if (server.isSameThread()) {
            Set<ChunkEventListener> cels = listeners.get(world, chunkPos);
            if (cels != null) {
                for (ChunkEventListener cel : cels) {
                    cel.onBlockUpdate(pos);
                }
            }
        }
    }

    private static void ensureServerThread() {
        if (!server.isSameThread()) {
            throw new RuntimeException("Thread is not server thread!");
        }
    }

    private static void serverStopCleanup() {
        if (listeners.size() != 0) {
            listeners = new ChunkPosMultiMap<>();
        }
    }
}
