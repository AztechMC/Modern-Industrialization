package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.fluid.FluidInventory;
import aztech.modern_industrialization.pipes.api.PipeConnectionType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

import static aztech.modern_industrialization.pipes.api.PipeConnectionType.*;

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
                    connections.set(i, new FluidConnection(conn.direction, (FluidInventory) entity, conn.type));
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

    @Override
    public PipeConnectionType[] getConnections(BlockPos pos) {
        PipeConnectionType[] connections = new PipeConnectionType[6];
        for(Direction direction : network.manager.getNodeLinks(pos)) {
            connections[direction.getId()] = FLUID;
        }
        for(FluidConnection connection : this.connections) {
            connections[connection.direction.getId()] = connection.type;
        }
        return connections;
    }

    private boolean canConnect(BlockEntity entity, Direction direction) {
        return entity instanceof FluidInventory && ((FluidInventory) entity).canFluidContainerConnect(direction.getOpposite());
    }

    @Override
    public void removeConnection(World world, BlockPos pos, Direction direction) {
        // Cycle if it exists
        for(int i = 0; i < connections.size(); i++) {
            FluidConnection conn = connections.get(i);
            if(conn.direction == direction) {
                if(conn.type == FLUID_IN) conn.type = FLUID_IN_OUT;
                else if(conn.type == FLUID_IN_OUT) conn.type = FLUID_OUT;
                else connections.remove(i);
                return;
            }
        }
    }

    @Override
    public void addConnection(World world, BlockPos pos, Direction direction) {
        // Refuse if it already exists
        for (FluidConnection connection : connections) {
            if (connection.direction == direction) {
                return;
            }
        }
        // Otherwise try to connect
        BlockPos adjPos = pos.offset(direction);
        BlockEntity entity = world.getBlockEntity(adjPos);
        if (canConnect(entity, direction)) {
            connections.add(new FluidConnection(direction, (FluidInventory) entity, FLUID_IN));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("amount", amount);
        for(FluidConnection connection : connections) {
            tag.putByte(connection.direction.toString(), (byte)encodeConnectionType(connection.type));
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        amount = tag.getInt("amount");
        for(Direction direction : Direction.values()) {
            if(tag.contains(direction.toString())) {
                connections.add(new FluidConnection(direction, null, decodeConnectionType(tag.getByte(direction.toString()))));
            }
        }
    }

    private PipeConnectionType decodeConnectionType(int i) {
        return i == 0 ? FLUID_IN : i == 1 ? FLUID_IN_OUT : FLUID_OUT;
    }

    private int encodeConnectionType(PipeConnectionType connection) {
        return connection == FLUID_IN ? 0 : connection == FLUID_IN_OUT ? 1 : 2;
    }

    private static class FluidConnection {
        private final Direction direction;
        private final FluidInventory fluidInventory;
        private PipeConnectionType type;

        private FluidConnection(Direction direction, FluidInventory fluidInventory, PipeConnectionType type) {
            this.direction = direction;
            this.fluidInventory = fluidInventory;
            this.type = type;
        }

        private boolean canInsert(Fluid fluid) {
            return type == FLUID_IN || type == FLUID_IN_OUT;
        }

        private boolean canExtract(Fluid fluid) {
            return (type == FLUID_OUT || type == FLUID_IN_OUT) && fluidInventory.providesFluidExtractionForce(direction.getOpposite(), fluid);
        }
    }
}
