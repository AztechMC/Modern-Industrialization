package aztech.modern_industrialization.pipes.fluid;

import alexiil.mc.lib.attributes.SearchOption;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.ExactFluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.*;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.pipes.api.PipeConnectionType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.math.RoundingMode;
import java.util.*;

import static alexiil.mc.lib.attributes.Simulation.ACTION;
import static aztech.modern_industrialization.pipes.api.PipeConnectionType.*;

public class FluidNetworkNode extends PipeNetworkNode {
    int amount = 0;
    private List<FluidConnection> connections = new ArrayList<>();

    void interactWithConnections(World world, BlockPos pos) {
        FluidNetworkData data = (FluidNetworkData) network.data;
        FluidNetwork network = (FluidNetwork) this.network;
        if(amount > network.nodeCapacity) {
            ModernIndustrialization.LOGGER.warn("Fluid amount > nodeCapacity, deleting some fluid!");
        }
        for(FluidConnection connection : connections) { // TODO: limit insert and extract rate
            // Insert
            if(amount > 0 && connection.canInsert()) {
                SearchOption option = SearchOptions.inDirection(connection.direction);
                FluidInsertable insertable = FluidAttributes.INSERTABLE.get(world, pos.offset(connection.direction), option);
                FluidVolume leftover = insertable.attemptInsertion(data.fluid.withAmount(FluidAmount.of(amount, 1000)), ACTION);
                amount = leftover.amount().asInt(1000, RoundingMode.FLOOR);
            }
            if(connection.canExtract()) {
                // Extract any
                if(data.fluid.isEmpty()) {
                    SearchOption option = SearchOptions.inDirection(connection.direction);
                    FluidExtractable extractable = FluidAttributes.EXTRACTABLE.get(world, pos.offset(connection.direction), option);
                    FluidVolume extractedVolume = extractable.extract(FluidAmount.of(network.nodeCapacity, 1000));
                    if (extractedVolume.amount().isPositive()) {
                        amount = extractedVolume.amount().asInt(1000, RoundingMode.FLOOR);
                        data.fluid = extractedVolume.getFluidKey();
                        break;
                    }
                }
                // Extract current fluid
                else {
                    SearchOption option = SearchOptions.inDirection(connection.direction);
                    FluidExtractable extractable = FluidAttributes.EXTRACTABLE.get(world, pos.offset(connection.direction), option);
                    FluidVolume extractedVolume = extractable.extract(new ExactFluidFilter(data.fluid), FluidAmount.of(network.nodeCapacity - amount, 1000));
                    amount += extractedVolume.amount().asInt(1000, RoundingMode.FLOOR);
                }
            }
        }
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
        // We don't connect by default, so we just have to remove connections that have become unavailable
        for(int i = 0; i < connections.size();) {
            FluidConnection conn = connections.get(i);
            if(canConnect(world, pos, conn.direction)) {
                    i++;
            } else {
                connections.remove(i);
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

    private boolean canConnect(World world, BlockPos pos, Direction direction) {
        SearchOption option = SearchOptions.inDirection(direction);
        return FluidAttributes.INSERTABLE.getAll(world, pos.offset(direction), option).hasOfferedAny()
                || FluidAttributes.EXTRACTABLE.getAll(world, pos.offset(direction), option).hasOfferedAny();
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
        if (canConnect(world, pos, direction)) {
            connections.add(new FluidConnection(direction, FLUID_IN));
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
                connections.add(new FluidConnection(direction, decodeConnectionType(tag.getByte(direction.toString()))));
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
        private PipeConnectionType type;

        private FluidConnection(Direction direction, PipeConnectionType type) {
            this.direction = direction;
            this.type = type;
        }

        private boolean canInsert() {
            return type == FLUID_IN || type == FLUID_IN_OUT;
        }

        private boolean canExtract() {
            return type == FLUID_OUT || type == FLUID_IN_OUT;
        }
    }
}
