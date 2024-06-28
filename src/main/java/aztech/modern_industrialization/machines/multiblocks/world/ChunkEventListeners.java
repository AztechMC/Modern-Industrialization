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
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;

public class ChunkEventListeners {
    public static ChunkPosMultiMap<ChunkEventListener> listeners = new ChunkPosMultiMap<>();

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> serverStopCleanup());

        NeoForge.EVENT_BUS.addListener(ChunkEvent.Load.class, event -> {
            if (event.getLevel().isClientSide()) {
                return;
            }

            ensureServerThread(event.getLevel().getServer());
            Set<ChunkEventListener> cels = listeners.get(event.getLevel(), event.getChunk().getPos());
            if (cels != null) {
                for (ChunkEventListener cel : cels) {
                    cel.onLoad();
                }
            }
        });
        NeoForge.EVENT_BUS.addListener(ChunkEvent.Unload.class, event -> {
            if (event.getLevel().isClientSide()) {
                return;
            }

            ensureServerThread(event.getLevel().getServer());
            Set<ChunkEventListener> cels = listeners.get(event.getLevel(), event.getChunk().getPos());
            if (cels != null) {
                for (ChunkEventListener cel : cels) {
                    cel.onUnload();
                }
            }
        });
        NeoForge.EVENT_BUS.addListener(BlockEvent.NeighborNotifyEvent.class, event -> {
            if (event.getLevel() instanceof Level level) {
                onBlockStateChange(level, new ChunkPos(event.getPos()), event.getPos());
            }
        });
    }

    public static void onBlockStateChange(Level world, ChunkPos chunkPos, BlockPos pos) {
        // We skip block state changes that happen outside of the server thread.
        // Hopefully that won't cause problems.
        if (world instanceof ServerLevel serverLevel && serverLevel.getServer().isSameThread()) {
            Set<ChunkEventListener> cels = listeners.get(world, chunkPos);
            if (cels != null) {
                for (ChunkEventListener cel : cels) {
                    cel.onBlockUpdate(pos);
                }
            }
        }
    }

    private static void ensureServerThread(@Nullable MinecraftServer server) {
        if (server == null) {
            throw new RuntimeException("Null server!");
        }

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
