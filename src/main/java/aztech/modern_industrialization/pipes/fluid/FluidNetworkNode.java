package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.fluid.FluidInventory;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FluidNetworkNode extends PipeNetworkNode {
    int amount = 0;
    private List<FluidConnection> connections = new ArrayList<>();

    void interactWithConnections() {
        FluidNetworkData data = (FluidNetworkData) network.data;
        for(FluidConnection connection : connections) { // TODO: limit insert and extract rate
            // Insert
            if(amount > 0 && connection.canInsert(data.fluid)) {
                int inserted = connection.fluidInventory.insert(connection.direction, data.fluid, amount, false);
                amount -= inserted;
            }
            // Extract any
            if(data.fluid == Fluids.EMPTY) {
                for(Fluid fluid : connection.fluidInventory.getExtractableFluids(connection.direction)) {
                    if(connection.canExtract(fluid)) {
                        int extracted = connection.fluidInventory.extract(connection.direction, fluid, data.nodeCapacity, false);
                        if (extracted > 0) {
                            amount = extracted;
                            data.fluid = fluid;
                            break;
                        }
                    }
                }
            }
            // Extract current fluid
            else {
                if(connection.canExtract(data.fluid)) {
                    int extracted = connection.fluidInventory.extract(connection.direction, data.fluid, data.nodeCapacity - amount, false);
                    amount += extracted;
                }
            }
        }
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
        // We don't connect by default, so we just have to remove connections that have become unavailable
        for(int i = 0; i < connections.size();) {
            FluidConnection conn = connections.get(i);
            BlockPos adjPos = pos.offset(conn.direction);
            BlockEntity entity = world.getBlockEntity(adjPos);
            if(conn.fluidInventory == null) {
                // The node was just loaded, it doesn't have the fluid inventory yet, so we accept any connection.
                if(canConnect(entity, conn.direction)) {
                    connections.set(i, new FluidConnection(conn.direction, (FluidInventory) entity));
                    i++;
                } else {
                    connections.remove(i);
                }
            } else {
                // The connected inventory must be the same and it must still accept connections, otherwise we disconnect
                if(entity == conn.fluidInventory && conn.fluidInventory.canFluidContainerConnect(conn.direction.getOpposite())) {
                    i++;
                } else {
                    connections.remove(i);
                }

            }
        }
    }

    public Set<Direction> getRenderedConnections(BlockPos pos) {
        Set<Direction> links = network.manager.getNodeLinks(pos);
        for(FluidConnection connection : connections) {
            links.add(connection.direction);
        }
        return links;
    }

    private boolean canConnect(BlockEntity entity, Direction direction) {
        return entity instanceof FluidInventory && ((FluidInventory) entity).canFluidContainerConnect(direction.getOpposite());
    }

    @Override
    public boolean removeConnection(World world, BlockPos pos, Direction direction) {
        // Remove if it exists
        for(int i = 0; i < connections.size(); i++) {
            if(connections.get(i).direction == direction) {
                connections.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addConnection(World world, BlockPos pos, Direction direction) {
        // Refuse if it already exists
        for(int i = 0; i < connections.size(); i++) {
            if(connections.get(i).direction == direction) {
                connections.remove(i);
                return false;
            }
        }
        // Otherwise try to connect
        BlockPos adjPos = pos.offset(direction);
        BlockEntity entity = world.getBlockEntity(adjPos);
        if (canConnect(entity, direction)) {
            connections.add(new FluidConnection(direction, (FluidInventory) entity));
            return true;
        }
        return false;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("amount", amount);
        tag.putByte("connections", NbtHelper.encodeDirections(connections.stream().map(c -> c.direction).collect(Collectors.toList())));
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        amount = tag.getInt("amount");
        Direction[] directions = NbtHelper.decodeDirections(tag.getByte("connections"));
        connections.clear();
        for(int i = 0; i < directions.length; i++) {
            connections.add(new FluidConnection(directions[i], null));
        }
    }

    private static class FluidConnection {
        private final Direction direction;
        private final FluidInventory fluidInventory;

        private FluidConnection(Direction direction, FluidInventory fluidInventory) {
            this.direction = direction;
            this.fluidInventory = fluidInventory;
        }

        private boolean canInsert(Fluid fluid) {
            return true;
        }

        private boolean canExtract(Fluid fluid) {
            return fluidInventory.providesFluidExtractionForce(direction.getOpposite(), fluid);
        }
    }
}
