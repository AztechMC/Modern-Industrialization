package aztech.modern_industrialization.pipes.api;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Map;

public abstract class PipeNetworkNode {
    protected PipeNetwork network;
    public abstract void updateConnections(World world, BlockPos pos);

    /**
     * Get connections. Must return a size 6 array containing the 6 connections in the Direction order.
     * Null can be used to render no connection.
     */
    public abstract PipeConnectionType[] getConnections(BlockPos pos);
    public abstract void removeConnection(World world, BlockPos pos, Direction direction);
    public abstract void addConnection(World world, BlockPos pos, Direction direction);

    /**
     * Get the connection screen handler factory, or null if there is not gui for this connection.
     */
    public ExtendedScreenHandlerFactory getConnectionGui(Direction direction, Runnable markDirty, Runnable sync) {
        return null;
    }

    public abstract CompoundTag toTag(CompoundTag tag);
    public abstract void fromTag(CompoundTag tag);

    public final PipeNetworkType getType() {
        return network.manager.getType();
    }
    public final PipeNetworkManager getManager() {
        return network.manager;
    }
    public void tick(World world, BlockPos pos) {
        network.tick(world);
    }
}
