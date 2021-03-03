package aztech.modern_industrialization.machinesv2.multiblocks.world;

import aztech.modern_industrialization.ModernIndustrialization;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Set;

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

    public static void onBlockStateChange(World world, ChunkPos chunkPos, BlockPos pos) {
        // We skip block state changes that happen outside of the server thread.
        // Hopefully that won't cause problems.
        if (server.isOnThread()) {
            Set<ChunkEventListener> cels = listeners.get(world, chunkPos);
            if (cels != null) {
                for (ChunkEventListener cel : cels) {
                    cel.onBlockUpdate(pos);
                }
            }
        }
    }

    private static void ensureServerThread() {
        if (!server.isOnThread()) {
            throw new RuntimeException("Thread is not server thread!");
        }
    }

    private static void serverStopCleanup() {
        if (listeners.size() != 0) {
            ModernIndustrialization.LOGGER.warn("ChunkEventListeners#listeners is not empty at server stop! Active worlds: " + listeners.size());
            listeners = new ChunkPosMultiMap<>();
        }
    }
}
