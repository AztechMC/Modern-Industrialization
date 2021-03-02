package aztech.modern_industrialization.machinesv2.multiblocks.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Set;

public class ChunkEventListeners {
    public static final ChunkPosMultiMap<ChunkEventListener> listeners = new ChunkPosMultiMap<>();

    static {
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            Set<ChunkEventListener> cels = listeners.get(world, chunk.getPos());
            if (cels != null) {
                for (ChunkEventListener cel : cels) {
                    cel.onLoad();
                }
            }
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            Set<ChunkEventListener> cels = listeners.get(world, chunk.getPos());
            if (cels != null) {
                for (ChunkEventListener cel : cels) {
                    cel.onUnload();
                }
            }
        });
    }

    public static void onBlockStateChange(World world, ChunkPos chunkPos, BlockPos pos) {
        Set<ChunkEventListener> cels = listeners.get(world, chunkPos);
        if (cels != null) {
            for (ChunkEventListener cel : cels) {
                cel.onBlockUpdate(pos);
            }
        }
    }
}
