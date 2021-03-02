package aztech.modern_industrialization.machinesv2.multiblocks.world;

import net.minecraft.util.math.BlockPos;

public interface ChunkEventListener {
    void onBlockUpdate(BlockPos pos);
    void onUnload();
    void onLoad();
}
