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
import aztech.modern_industrialization.util.NbtHelper;
import java.util.*;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidApi;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
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
            Storage<Fluid> io = FluidApi.SIDED.get(world, pos.offset(connection.direction), connection.direction.getOpposite());
            if (amount > 0 && connection.canInsert() && io != null && io.supportsInsertion()) {
                try (Transaction tx = Transaction.openOuter()) {
                    amount -= io.insert(data.fluid, amount, tx);
                    tx.commit();
                }
            }
            if (connection.canExtract() && io != null && io.supportsExtraction()) {
                // Find the fluid to extract (by simulating if extraction of said fluid is > 0).
                if (data.fluid == Fluids.EMPTY) {
                    try (Transaction tx = Transaction.openOuter()) {
                        io.forEach(storageView -> {
                            if (data.fluid != Fluids.EMPTY)
                                throw new RuntimeException("Bad implementation!");
                            if (storageView.amount() > 0) {
                                data.fluid = storageView.resource();
                                try (Transaction nested = tx.openNested()) {
                                    if (storageView.extract(data.fluid, Long.MAX_VALUE, nested) > 0) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }, tx);
                    }
                }
                // Extract current fluid
                if (data.fluid != Fluids.EMPTY) {
                    try (Transaction tx = Transaction.openOuter()) {
                        amount += io.extract(data.fluid, network.nodeCapacity - amount, tx);
                        tx.commit();
                    }
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
        Storage<Fluid> io = FluidApi.SIDED.get(world, pos.offset(direction), direction.getOpposite());
        return io != null && (io.supportsInsertion() || io.supportsExtraction());
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
