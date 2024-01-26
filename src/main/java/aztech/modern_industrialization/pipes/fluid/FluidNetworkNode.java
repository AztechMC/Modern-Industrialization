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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.pipes.api.IPipeMenuProvider;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.gui.IPipeScreenHandlerHelper;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeNetworks;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.IOFluidHandler;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import org.jetbrains.annotations.Nullable;

public class FluidNetworkNode extends PipeNetworkNode {
    long amount = 0;
    private final List<FluidConnection> connections = new ArrayList<>();
    private FluidVariant cachedFluid = FluidVariant.blank();

    /**
     * Add all valid targets to the target list, and pick the fluid for the network
     * if no fluid is set.
     */
    void gatherTargetsAndPickFluid(ServerLevel world, BlockPos pos, List<FluidTarget> targets) {
        FluidNetworkData data = (FluidNetworkData) network.data;
        FluidNetwork network = (FluidNetwork) this.network;

        if (amount > network.nodeCapacity) {
            MI.LOGGER.warn("Fluid amount > nodeCapacity, deleting some fluid!");
            amount = network.nodeCapacity;
        }
        if (amount > 0 && data.fluid.isBlank()) {
            MI.LOGGER.warn("Amount > 0 but fluid is blank, deleting some fluid!");
            amount = 0;
        }

        for (FluidConnection connection : connections) {
            var storage = getNeighborStorage(world, pos, connection);
            if (data.fluid.isBlank() && connection.canExtract()) {
                // Try to set fluid, will return null if none could be found.
                data.fluid = FluidVariant.of(storage.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE));
            }
            targets.add(new FluidTarget(connection.priority, new IOFluidHandler(storage, connection.canInsert(), connection.canExtract())));
        }
    }

    private IFluidHandler getNeighborStorage(ServerLevel world, BlockPos pos, FluidConnection connection) {
        if (connection.cache == null) {
            connection.cache = BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, world, pos.relative(connection.direction),
                    connection.direction.getOpposite());
        }
        var storage = connection.cache.getCapability();
        return Objects.requireNonNullElse(storage, EmptyFluidHandler.INSTANCE);
    }

    @Override
    public void updateConnections(Level world, BlockPos pos) {
        // Remove the connection to the outside world if a connection to another pipe is made.
        var levelNetworks = PipeNetworks.get((ServerLevel) world);
        connections.removeIf(connection -> {
            for (var type : PipeNetworkType.getTypes().values()) {
                var manager = levelNetworks.getOptionalManager(type);
                if (manager != null && manager.hasLink(pos, connection.direction)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public PipeEndpointType[] getConnections(BlockPos pos) {
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for (Direction direction : network.manager.getNodeLinks(pos)) {
            connections[direction.get3DDataValue()] = PipeEndpointType.PIPE;
        }
        for (FluidConnection connection : this.connections) {
            connections[connection.direction.get3DDataValue()] = connection.type;
        }
        return connections;
    }

    private boolean canConnect(Level world, BlockPos pos, Direction direction) {
        return world.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(direction), direction.getOpposite()) != null;
    }

    @Override
    public void removeConnection(Level world, BlockPos pos, Direction direction) {
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
    public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
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
        amount = tag.getLong("amount_ftl");
        for (Direction direction : Direction.values()) {
            if (tag.contains(direction.toString())) {
                if (tag.getTagType(direction.toString()) == Tag.TAG_BYTE) {
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
    public IPipeMenuProvider getConnectionGui(Direction guiDirection, IPipeScreenHandlerHelper helper) {
        for (FluidConnection connection : connections) {
            if (connection.direction == guiDirection) {
                return connection.new ScreenHandlerFactory(helper, getType().getIdentifier());
            }
        }
        return null;
    }

    private class FluidConnection {
        private final Direction direction;
        private PipeEndpointType type;
        private int priority;
        private BlockCapabilityCache<IFluidHandler, @Nullable Direction> cache;

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

        private class ScreenHandlerFactory implements IPipeMenuProvider {
            private final FluidPipeInterface iface;
            private final ResourceLocation pipeType;

            private ScreenHandlerFactory(IPipeScreenHandlerHelper helper, ResourceLocation pipeType) {
                this.iface = new FluidPipeInterface() {
                    @Override
                    public FluidVariant getNetworkFluid() {
                        if (network != null) {
                            return getFluid();
                        } else {
                            return FluidVariant.blank();
                        }
                    }

                    @Override
                    public void setNetworkFluid(FluidVariant fluid) {
                        FluidNetwork network = (FluidNetwork) FluidNetworkNode.this.network;
                        if (network != null && !getNetworkFluid().equals(fluid)) {
                            network.clearFluid();
                            if (!fluid.isBlank()) {
                                network.setFluid(fluid);
                            }
                            helper.callMarkDirty();
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
                            helper.callMarkDirty();
                            helper.callSync();
                        }
                    }

                    @Override
                    public int getPriority(int channel) {
                        return priority;
                    }

                    @Override
                    public void setPriority(int channel, int priority) {
                        FluidConnection.this.priority = priority;
                        helper.callMarkDirty();
                    }

                    @Override
                    public boolean canUse(Player player) {
                        // Check that the BE is within distance
                        if (!helper.isWithinUseDistance(player)) {
                            return false;
                        }
                        // Check that this connection still exists
                        return helper.doesNodeStillExist(FluidNetworkNode.this) && connections.contains(FluidConnection.this);
                    }
                };
                this.pipeType = pipeType;
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("item." + pipeType.getNamespace() + "." + pipeType.getPath());
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new FluidPipeScreenHandler(syncId, inv, iface);
            }

            @Override
            public void writeAdditionalData(FriendlyByteBuf buf) {
                iface.toBuf(buf);
            }
        }
    }

    @Override
    public CompoundTag writeCustomData() {
        CompoundTag tag = new CompoundTag();
        NbtHelper.putFluid(tag, "fluid", ((FluidNetworkData) network.data).fluid);
        return tag;
    }

    public void afterTick(ServerLevel world, BlockPos pos) {
        FluidVariant networkFluid = ((FluidNetworkData) network.data).fluid;
        if (!networkFluid.equals(cachedFluid)) {
            cachedFluid = networkFluid;
            // Equivalent to calling sync()
            world.getChunkSource().blockChanged(pos);
        }
    }

    // Used in the Waila plugin
    private FluidVariant getFluid() {
        return ((FluidNetworkData) network.data).fluid;
    }

    public InGameInfo collectNetworkInfo() {
        long stored = 0, capacity = 0;
        var fluidNetwork = (FluidNetwork) network;
        for (var posNode : network.iterateTickingNodes()) {
            var node = (FluidNetworkNode) posNode.getNode();
            stored += node.amount;
            capacity += fluidNetwork.nodeCapacity;
        }
        return new InGameInfo(getFluid(), stored, capacity, fluidNetwork.stats.getValue(), capacity);
    }

    public record InGameInfo(FluidVariant fluid, long stored, long capacity, long transfer, long maxTransfer) {
    }
}
