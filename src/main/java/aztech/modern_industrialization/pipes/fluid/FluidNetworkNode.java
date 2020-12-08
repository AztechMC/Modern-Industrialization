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
package aztech.modern_industrialization.pipes.fluid;

import static aztech.modern_industrialization.pipes.api.PipeEndpointType.*;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import java.util.*;

import aztech.modern_industrialization.util.NbtHelper;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.fluid.FluidApi;
import dev.technici4n.fasttransferlib.api.fluid.FluidExtractable;
import dev.technici4n.fasttransferlib.api.fluid.FluidInsertable;
import dev.technici4n.fasttransferlib.api.fluid.FluidView;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FluidNetworkNode extends PipeNetworkNode {
    long amount = 0;
    private final List<FluidConnection> connections = new ArrayList<>();
    private Fluid cachedFluid = Fluids.EMPTY;
    private boolean needsSync = false;

    void interactWithConnections(World world, BlockPos pos) {
        FluidNetworkData data = (FluidNetworkData) network.data;
        FluidNetwork network = (FluidNetwork) this.network;
        if (amount > network.nodeCapacity) {
            ModernIndustrialization.LOGGER.warn("Fluid amount > nodeCapacity, deleting some fluid!");
            amount = network.nodeCapacity;
        }
        if (amount > 0 && data.fluid == Fluids.EMPTY) {
            ModernIndustrialization.LOGGER.warn("Amount > 0 but fluid is empty, deleting some fluid!");
            amount = 0;
        }
        for (FluidConnection connection : connections) { // TODO: limit insert and extract rate
            // Insert
            FluidView view = FluidApi.SIDED_VIEW.get(world, pos.offset(connection.direction), connection.direction.getOpposite());
            if (amount > 0 && connection.canInsert() && view instanceof FluidInsertable) {
                FluidInsertable insertable = (FluidInsertable) view;
                amount = insertable.insert(data.fluid, amount, Simulation.ACT);
            }
            if (connection.canExtract() && view instanceof FluidExtractable) {
                FluidExtractable extractable = (FluidExtractable) view;
                // Extract any
                if (data.fluid == Fluids.EMPTY) {
                    for (int i = 0; i < extractable.getFluidSlotCount(); ++i) {
                        Fluid fluid = extractable.getFluid(i);
                        amount = extractable.extract(i, fluid, network.nodeCapacity, Simulation.ACT);

                        if (amount > 0) {
                            data.fluid = fluid;
                            break;
                        }
                    }
                }
                // Extract current fluid
                else {
                    amount += extractable.extract(data.fluid, network.nodeCapacity - amount, Simulation.ACT);
                }
            }
        }
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
        // We don't connect by default, so we just have to remove connections that have
        // become unavailable
        for (int i = 0; i < connections.size();) {
            FluidConnection conn = connections.get(i);
            if (canConnect(world, pos, conn.direction)) {
                i++;
            } else {
                connections.remove(i);
            }
        }
    }

    @Override
    public PipeEndpointType[] getConnections(BlockPos pos) {
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for (Direction direction : network.manager.getNodeLinks(pos)) {
            connections[direction.getId()] = PipeEndpointType.PIPE;
        }
        for (FluidConnection connection : this.connections) {
            connections[connection.direction.getId()] = connection.type;
        }
        return connections;
    }

    private boolean canConnect(World world, BlockPos pos, Direction direction) {
        FluidView view = FluidApi.SIDED_VIEW.get(world, pos.offset(direction), direction.getOpposite());
        return view instanceof FluidInsertable || view instanceof FluidExtractable;
    }

    @Override
    public void removeConnection(World world, BlockPos pos, Direction direction) {
        // Cycle if it exists
        for (int i = 0; i < connections.size(); i++) {
            FluidConnection conn = connections.get(i);
            if (conn.direction == direction) {
                if (conn.type == BLOCK_IN)
                    conn.type = BLOCK_IN_OUT;
                else if (conn.type == BLOCK_IN_OUT)
                    conn.type = BLOCK_OUT;
                else
                    connections.remove(i);
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
            connections.add(new FluidConnection(direction, BLOCK_IN));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putLong("amount_ftl", amount);
        for (FluidConnection connection : connections) {
            tag.putByte(connection.direction.toString(), (byte) encodeConnectionType(connection.type));
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (tag.contains("amount")) {
            amount = tag.getInt("amount") * 81;
        } else {
            amount = tag.getLong("amount_ftl");
        }
        for (Direction direction : Direction.values()) {
            if (tag.contains(direction.toString())) {
                connections.add(new FluidConnection(direction, decodeConnectionType(tag.getByte(direction.toString()))));
            }
        }
    }

    private PipeEndpointType decodeConnectionType(int i) {
        return i == 0 ? BLOCK_IN : i == 1 ? BLOCK_IN_OUT : BLOCK_OUT;
    }

    private int encodeConnectionType(PipeEndpointType connection) {
        return connection == BLOCK_IN ? 0 : connection == BLOCK_IN_OUT ? 1 : 2;
    }

    private static class FluidConnection {
        private final Direction direction;
        private PipeEndpointType type;

        private FluidConnection(Direction direction, PipeEndpointType type) {
            this.direction = direction;
            this.type = type;
        }

        private boolean canInsert() {
            return type == BLOCK_IN || type == BLOCK_IN_OUT;
        }

        private boolean canExtract() {
            return type == BLOCK_OUT || type == BLOCK_IN_OUT;
        }
    }

    @Override
    public CompoundTag writeCustomData() {
        CompoundTag tag = new CompoundTag();
        NbtHelper.putFluid(tag, "fluid", ((FluidNetworkData) network.data).fluid);
        return tag;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        super.tick(world, pos);

        Fluid networkFluid = ((FluidNetworkData) network.data).fluid;
        if (networkFluid != cachedFluid) {
            cachedFluid = networkFluid;
            needsSync = true;
        }
    }

    @Override
    public boolean shouldSync() {
        boolean sync = needsSync;
        needsSync = false;
        return sync;
    }

    // Used in the Waila plugin
    public long getAmount() {
        return amount;
    }

    public int getCapacity() {
        return ((FluidNetwork) network).nodeCapacity;
    }

    public Fluid getFluid() {
        return ((FluidNetworkData) network.data).fluid;
    }
}
