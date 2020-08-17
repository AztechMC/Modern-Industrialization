package aztech.modern_industrialization.pipes.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Set;

public abstract class PipeNetworkNode {
    protected PipeNetwork network;
    public abstract void updateConnections(World world, BlockPos pos);
    public abstract Set<Direction> getRenderedConnections(BlockPos pos);

    public abstract CompoundTag toTag(CompoundTag tag);
    public abstract void fromTag(CompoundTag tag);

    public final PipeNetworkType getType() {
        return network.manager.getType();
    }
}
