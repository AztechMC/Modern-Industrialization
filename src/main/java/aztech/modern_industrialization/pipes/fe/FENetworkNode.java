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
package aztech.modern_industrialization.pipes.fe;

import static aztech.modern_industrialization.pipes.api.PipeEndpointType.*;

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.api.datamaps.MIDataMaps;
import aztech.modern_industrialization.pipes.api.*;
import aztech.modern_industrialization.pipes.gui.IPipeScreenHandlerHelper;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeNetworks;
import aztech.modern_industrialization.util.IOEnergyStorage;
import com.google.common.collect.Lists;
import java.util.List;

import com.google.common.primitives.Ints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class FENetworkNode extends PipeNetworkNode {
    private List<FEConnection> connections = Lists.newArrayList();

    long energy;

    private boolean canConnect(Level world, BlockPos pos, Direction direction) {
        var storage = world.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(direction), direction.getOpposite());
        return storage != null && (storage.canReceive() || storage.canExtract());
    }

    long gatherTargets(ServerLevel world, BlockPos pos, List<FETarget> targets) {
        long capacity = 0;
        for (FEConnection connection : connections) {
            var storage = world.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(connection.direction),
                    connection.direction.getOpposite());
            if (storage != null) {
                int extractRate = connection.getExtractRate();
                targets.add(new FETarget(connection.priority, new IOEnergyStorage(storage, connection.canInsert(), connection.canExtract()),
                        extractRate));
                capacity += extractRate;
            }
        }
        return capacity;
    }

    private long getCapacity() {
        long capacity = 0;
        for (FEConnection connection : connections) {
            capacity += connection.getExtractRate();
        }
        return capacity;
    }

    @Override
    public PipeEndpointType[] getConnections(BlockPos pos) {
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for (Direction direction : network.manager.getNodeLinks(pos)) {
            connections[direction.get3DDataValue()] = PIPE;
        }
        for (FEConnection connection : this.connections) {
            connections[connection.direction.get3DDataValue()] = connection.type;
        }
        return connections;
    }

    @Override
    public void buildInitialConnections(Level world, BlockPos pos) {
        for (var direction : Direction.values()) {
            if (this.canConnect(world, pos, direction)) {
                connections.add(new FEConnection(direction, BLOCK_IN, 0));
            }
        }
    }

    @Override
    public void updateConnections(Level world, BlockPos pos) {
        // Remove the connection to the outside world if a connection to another pipe is made.
        var levelNetworks = PipeNetworks.get((ServerLevel) world);
        connections.removeIf(connection -> {
            for (var type : PipeNetworkType.getTypes().values()) {
                var manager = levelNetworks.getOptionalManager(type);
                if (manager != null && manager.hasLink(pos, connection.direction)) {
                    connection.dropUpgrades(world, pos);
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void removeConnection(Level world, BlockPos pos, Direction direction) {
        // Cycle if it exists
        for (int i = 0; i < connections.size(); i++) {
            FEConnection conn = connections.get(i);
            if (conn.direction == direction) {
                if (conn.type == BLOCK_IN)
                    conn.type = BLOCK_IN_OUT;
                else if (conn.type == BLOCK_IN_OUT)
                    conn.type = BLOCK_OUT;
                else {
                    conn.dropUpgrades(world, pos);
                    connections.remove(i);
                }
                return;
            }
        }
    }

    @Override
    public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
        // Refuse if it already exists
        for (FEConnection connection : connections) {
            if (connection.direction == direction) {
                return;
            }
        }
        // Otherwise try to connect
        if (canConnect(world, pos, direction)) {
            connections.add(new FEConnection(direction, BLOCK_IN, 0));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("energy", energy);
        for (FEConnection connection : connections) {
            CompoundTag connectionTag = new CompoundTag();
            connectionTag.putByte("connections", (byte) encodeConnectionType(connection.type));
            connectionTag.putInt("priority", connection.priority);
            tag.put(connection.direction.toString(), connectionTag);
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag, HolderLookup.Provider registries) {
        energy = tag.getLong("energy");
        for (Direction direction : Direction.values()) {
            if (tag.contains(direction.toString())) {
                CompoundTag connectionTag = tag.getCompound(direction.toString());
                connections.add(new FEConnection(direction, decodeConnectionType(connectionTag.getByte("connections")),
                        connectionTag.getInt("priority")));
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
        for (FEConnection connection : connections) {
            if (connection.direction == guiDirection) {
                return connection.new ScreenHandlerFactory(helper, getType().getIdentifier());
            }
        }
        return null;
    }

    @Override
    public void appendDroppedStacks(List<ItemStack> droppedStacks) {
        for (FEConnection conn : connections) {
            if (!conn.upgradeStack.isEmpty()) {
                droppedStacks.add(conn.upgradeStack);
                conn.upgradeStack = ItemStack.EMPTY;
            }
        }
    }

    class FEConnection {
        final Direction direction;
        private PipeEndpointType type;
        int priority;
        private ItemStack upgradeStack = ItemStack.EMPTY;
        BlockCapabilityCache<IEnergyStorage, @Nullable Direction> cache = null;

        private FEConnection(Direction direction, PipeEndpointType type, int priority) {
            this.direction = direction;
            this.type = type;
            this.priority = priority;
        }

        boolean canInsert() {
            return type == BLOCK_IN || type == BLOCK_IN_OUT;
        }

        boolean canExtract() {
            return type == BLOCK_OUT || type == BLOCK_IN_OUT;
        }

        int getExtractRate() {
            if (!canExtract()) {
                return 0;
            }
            var upgradeData = upgradeStack.getItemHolder().getData(MIDataMaps.FE_WIRE_UPGRADES);
            long extraTransferRate = upgradeData == null ? 0 : upgradeData.energyTransferBoost();
            return Ints.saturatedCast(((long) MIConfig.getConfig().baseFEWireTransfer) + (extraTransferRate * upgradeStack.getCount()));
        }

        private void dropUpgrades(Level world, BlockPos pos) {
            if (!upgradeStack.isEmpty()) {
                world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), upgradeStack));
                upgradeStack = ItemStack.EMPTY;
            }
        }

        private class ScreenHandlerFactory implements IPipeMenuProvider {
            private final FEWireInterface iface;
            private final ResourceLocation pipeType;

            private ScreenHandlerFactory(IPipeScreenHandlerHelper helper, ResourceLocation pipeType) {
                this.iface = new FEWireInterface() {
                    @Override
                    public ItemStack getUpgradeStack() {
                        return upgradeStack;
                    }

                    @Override
                    public void setUpgradeStack(ItemStack stack) {
                        upgradeStack = stack;
                        helper.callMarkDirty();
                    }

                    @Override
                    public int getConnectionType() {
                        return encodeConnectionType(type);
                    }

                    @Override
                    public void setConnectionType(int type) {
                        if (0 <= type && type < 3) {
                            FEConnection.this.type = decodeConnectionType(type);
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
                        FEConnection.this.priority = priority;
                        helper.callMarkDirty();
                    }

                    @Override
                    public boolean canUse(Player player) {
                        // Check that the BE is within distance
                        if (!helper.isWithinUseDistance(player)) {
                            return false;
                        }
                        // Check that this connection still exists
                        return helper.doesNodeStillExist(FENetworkNode.this) && connections.contains(FEConnection.this);
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
                return new FEWireScreenHandler(syncId, inv, iface);
            }

            @Override
            public void writeAdditionalData(RegistryFriendlyByteBuf buf) {
                iface.toBuf(buf);
            }
        }
    }

    // Used in the Waila plugin
    public InGameInfo collectNetworkInfo() {
        long energy = 0, maxEnergy = 0;
        var feNetwork = (FENetwork) network;
        for (var entry : network.iterateTickingNodes()) {
            var node = (FENetworkNode) entry.getNode();
            energy += node.energy;
            maxEnergy += node.getCapacity();
        }
        return new InGameInfo(energy, maxEnergy, feNetwork.stats.getValue(), feNetwork.capacityStats.getValue());
    }

    public record InGameInfo(long energy, long maxEnergy, long transfer, long maxTransfer) {
    }
}
