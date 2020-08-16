package aztech.modern_industrialization.pipes.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class PipeNetworkNode {
    public PipeNetwork network;
    public abstract void updateConnections(World world, BlockPos pos);

    public abstract CompoundTag toTag(CompoundTag tag);
    public abstract void fromTag(CompoundTag tag);

    public final PipeNetworkType getType() {
        return network.type;
    }
}
