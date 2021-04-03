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

import alexiil.mc.lib.attributes.SearchOption;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.FluidTransferable;
import alexiil.mc.lib.attributes.fluid.impl.EmptyFluidTransferable;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.transferapi.FluidTransferHelper;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.*;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// LBA
public class FluidNetworkNode extends PipeNetworkNode {
    long amount = 0;
    private final List<FluidConnection> connections = new ArrayList<>();
    private Fluid cachedFluid = Fluids.EMPTY;
    private boolean needsSync = false;

    /**
     * Add all valid targets to the target list, and pick the fluid for the network
     * if no fluid is set.
     */
    void gatherTargetsAndPickFluid(World world, BlockPos pos, List<FluidTarget> targets) {
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

        for (FluidConnection connection : connections) {
            FluidTransferable transferable = getNeighborTransferable(world, pos, connection);
            if (data.fluid == Fluids.EMPTY) {
                // Try to set fluid, will return EMPTY if none could be found.
                data.fluid = FluidTransferHelper.findExtractableFluid(transferable);
            }
            targets.add(new FluidTarget(connection.priority, transferable));
        }
    }

    FluidTransferable getNeighborTransferable(World world, BlockPos pos, FluidConnection connection) {
        BlockPos neighborPos = pos.offset(connection.direction);
        SearchOption<Object> opt = SearchOptions.inDirection(connection.direction);
        FluidInsertable insertable = connection.canInsert() ? FluidAttributes.INSERTABLE.get(world, neighborPos, opt) : EmptyFluidTransferable.NULL;
        FluidExtractable extractable = connection.canExtract() ? FluidAttributes.EXTRACTABLE.get(world, neighborPos, opt)
                : EmptyFluidTransferable.NULL;
        return FluidTransferable.from(insertable, extractable);
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
        BlockPos adjPos = pos.offset(direction);
        SearchOption<Object> opt = SearchOptions.inDirection(direction);
        return FluidAttributes.INSERTABLE.getFirstOrNull(world, adjPos, opt) != null
                || FluidAttributes.EXTRACTABLE.getFirstOrNull(world, adjPos, opt) != null;
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
            connections.add(new FluidConnection(direction, BLOCK_IN, 0));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putLong("amount_ftl", amount);
        for (FluidConnection connection : connections) {
            CompoundTag connectionTag = new CompoundTag();
            connectionTag.putByte("connections", (byte) encodeConnectionType(connection.type));
            connectionTag.putInt("priority", connection.priority);
            tag.put(connection.direction.toString(), connectionTag);
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
                if (tag.getType(direction.toString()) == NbtType.BYTE) {
                    // Old format (before fluid pipe priorities)
                    connections.add(new FluidConnection(direction, decodeConnectionType(tag.getByte(direction.toString())), 0));
                } else {
                    CompoundTag connectionTag = tag.getCompound(direction.toString());
                    connections.add(new FluidConnection(direction, decodeConnectionType(connectionTag.getByte("connections")),
                            connectionTag.getInt("priority")));
                }
            }
        }
    }

    private PipeEndpointType decodeConnectionType(int i) {
        return i == 0 ? BLOCK_IN : i == 1 ? BLOCK_IN_OUT : BLOCK_OUT;
    }

    private int encodeConnectionType(PipeEndpointType connection) {
        return connection == BLOCK_IN ? 0 : connection == BLOCK_IN_OUT ? 1 : 2;
    }

    @Override
    public ExtendedScreenHandlerFactory getConnectionGui(Direction guiDirection, Runnable markDirty, Runnable sync) {
        for (FluidConnection connection : connections) {
            if (connection.direction == guiDirection) {
                return connection.new ScreenHandlerFactory(markDirty, sync, getType().getIdentifier().getPath());
            }
        }
        return null;
    }

    private class FluidConnection {
        private final Direction direction;
        private PipeEndpointType type;
        private int priority;

        private FluidConnection(Direction direction, PipeEndpointType type, int priority) {
            this.direction = direction;
            this.type = type;
            this.priority = priority;
        }

        private boolean canInsert() {
            return type == BLOCK_IN || type == BLOCK_IN_OUT;
        }

        private boolean canExtract() {
            return type == BLOCK_OUT || type == BLOCK_IN_OUT;
        }

        private class ScreenHandlerFactory implements ExtendedScreenHandlerFactory {
            private final FluidPipeInterface iface;
            private final String pipeType;

            private ScreenHandlerFactory(Runnable markDirty, Runnable sync, String pipeType) {
                this.iface = new FluidPipeInterface() {
                    @Override
                    public Fluid getNetworkFluid() {
                        if (network != null) {
                            return getFluid();
                        } else {
                            return Fluids.EMPTY;
                        }
                    }

                    @Override
                    public void setNetworkFluid(Fluid fluid) {
                        FluidNetwork network = (FluidNetwork) FluidNetworkNode.this.network;
                        if (network != null) {
                            if (fluid == Fluids.EMPTY) {
                                network.clearFluid();
                            } else {
                                network.setFluid(fluid);
                            }
                            markDirty.run();
                        }
                    }

                    @Override
                    public int getConnectionType() {
                        return encodeConnectionType(type);
                    }

                    @Override
                    public void setConnectionType(int type) {
                        if (0 <= type && type < 3) {
                            FluidConnection.this.type = decodeConnectionType(type);
                            markDirty.run();
                            sync.run();
                        }
                    }

                    @Override
                    public int getPriority() {
                        return priority;
                    }

                    @Override
                    public void setPriority(int priority) {
                        FluidConnection.this.priority = priority;
                        sync.run();
                    }
                };
                this.pipeType = pipeType;
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                iface.toBuf(buf);
            }

            @Override
            public Text getDisplayName() {
                return new TranslatableText("item.modern_industrialization.pipe_" + pipeType);
            }

            @Override
            public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new FluidPipeScreenHandler(syncId, inv, iface);
            }
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
