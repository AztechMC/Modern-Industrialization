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
package aztech.modern_industrialization.pipes.item;

import static aztech.modern_industrialization.pipes.api.PipeEndpointType.*;

import aztech.modern_industrialization.api.pipes.item.SpeedUpgrade;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.gui.IPipeScreenHandlerHelper;
import aztech.modern_industrialization.pipes.item.ItemNetworkNode.ItemConnection.ScreenHandlerFactory;
import aztech.modern_industrialization.util.MIBlockApiCache;
import java.util.*;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

// LBA
public class ItemNetworkNode extends PipeNetworkNode {
    final List<ItemConnection> connections = new ArrayList<>();
    int inactiveTicks = 0;

    @Override
    public void updateConnections(World world, BlockPos pos) {
        // Remove the connection to the outside world if a connection to another pipe is
        // made.
        connections.removeIf(connection -> network.manager.hasLink(pos, connection.direction));
    }

    private boolean canConnect(World world, BlockPos pos, Direction direction) {
        BlockPos adjPos = pos.offset(direction);
        return ItemStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite()) != null;
    }

    @Override
    public PipeEndpointType[] getConnections(BlockPos pos) {
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for (Direction direction : network.manager.getNodeLinks(pos)) {
            connections[direction.getId()] = PIPE;
        }
        for (ItemConnection connection : this.connections) {
            connections[connection.direction.getId()] = connection.type;
        }
        return connections;
    }

    @Override
    public void removeConnection(World world, BlockPos pos, Direction direction) {
        // Cycle if it exists
        for (int i = 0; i < connections.size(); i++) {
            ItemConnection conn = connections.get(i);
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
    public void addConnection(World world, BlockPos pos, Direction direction) {
        // Refuse if it already exists
        for (ItemConnection connection : connections) {
            if (connection.direction == direction) {
                return;
            }
        }
        // Otherwise try to connect
        if (canConnect(world, pos, direction)) {
            connections.add(new ItemConnection(direction, BLOCK_IN, 0));
        }
    }

    @Override
    public NbtCompound toTag(NbtCompound tag) {
        for (ItemConnection connection : connections) {
            NbtCompound connectionTag = new NbtCompound();
            connectionTag.putByte("connections", (byte) encodeConnectionType(connection.type));
            connectionTag.putBoolean("whitelist", connection.whitelist);
            connectionTag.putInt("priority", connection.priority);
            for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                connectionTag.put(Integer.toString(i), connection.stacks[i].writeNbt(new NbtCompound()));
            }
            connectionTag.put("upgradeStack", connection.upgradeStack.writeNbt(new NbtCompound()));
            tag.put(connection.direction.toString(), connectionTag);
        }
        tag.putInt("inactiveTicks", inactiveTicks);
        return tag;
    }

    @Override
    public void fromTag(NbtCompound tag) {
        for (Direction direction : Direction.values()) {
            if (tag.contains(direction.toString())) {
                NbtCompound connectionTag = tag.getCompound(direction.toString());
                ItemConnection connection = new ItemConnection(direction, decodeConnectionType(connectionTag.getByte("connections")),
                        connectionTag.getInt("priority"));
                connection.whitelist = connectionTag.getBoolean("whitelist");
                for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                    connection.stacks[i] = ItemStack.fromNbt(connectionTag.getCompound(Integer.toString(i)));
                    connection.stacks[i].setCount(1);
                }
                connection.refreshStacksCache();
                connection.upgradeStack = ItemStack.fromNbt(connectionTag.getCompound("upgradeStack"));
                connections.add(connection);
            }
        }
        inactiveTicks = tag.getInt("inactiveTicks");
    }

    private static PipeEndpointType decodeConnectionType(int i) {
        return i == 0 ? BLOCK_IN : i == 1 ? BLOCK_IN_OUT : BLOCK_OUT;
    }

    private static int encodeConnectionType(PipeEndpointType connection) {
        return connection == BLOCK_IN ? 0 : connection == BLOCK_IN_OUT ? 1 : 2;
    }

    @Override
    public ExtendedScreenHandlerFactory getConnectionGui(Direction guiDirection, IPipeScreenHandlerHelper helper) {
        for (ItemConnection connection : connections) {
            if (connection.direction == guiDirection) {
                return connection.new ScreenHandlerFactory(helper, getType().getIdentifier());
            }
        }
        return null;
    }

    @Override
    public void appendDroppedStacks(List<ItemStack> droppedStacks) {
        for (ItemConnection conn : connections) {
            if (!conn.upgradeStack.isEmpty()) {
                droppedStacks.add(conn.upgradeStack);
                conn.upgradeStack = ItemStack.EMPTY;
            }
        }
    }

    class ItemConnection {
        final Direction direction;
        private PipeEndpointType type;
        boolean whitelist = true;
        int priority;
        private final ItemStack[] stacks = new ItemStack[ItemPipeInterface.SLOTS];
        final Set<ItemVariant> stacksCache = new HashSet<>();
        private ItemStack upgradeStack = ItemStack.EMPTY;
        MIBlockApiCache<Storage<ItemVariant>, Direction> cache = null;

        private ItemConnection(Direction direction, PipeEndpointType type, int priority) {
            this.direction = direction;
            this.type = type;
            this.priority = priority;
            for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                stacks[i] = ItemStack.EMPTY;
            }
        }

        private void refreshStacksCache() {
            stacksCache.clear();
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    stacksCache.add(ItemVariant.of(stack));
                }
            }
        }

        boolean canInsert() {
            return type == BLOCK_IN || type == BLOCK_IN_OUT;
        }

        boolean canExtract() {
            return type == BLOCK_OUT || type == BLOCK_IN_OUT;
        }

        boolean canStackMoveThrough(ItemVariant key) {
            return stacksCache.contains(key) == whitelist;
        }

        long getMoves() {
            SpeedUpgrade upgrade = SpeedUpgrade.LOOKUP.find(upgradeStack, null);
            return 16 + (upgrade == null ? 0 : upgrade.value() * upgradeStack.getCount());
        }

        private void dropUpgrades(World world, BlockPos pos) {
            if (!upgradeStack.isEmpty()) {
                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), upgradeStack));
                upgradeStack = ItemStack.EMPTY;
            }
        }

        private class ScreenHandlerFactory implements ExtendedScreenHandlerFactory {
            private final ItemPipeInterface iface;
            private final Identifier pipeType;

            private ScreenHandlerFactory(IPipeScreenHandlerHelper helper, Identifier pipeType) {
                this.iface = new ItemPipeInterface() {
                    @Override
                    public boolean isWhitelist() {
                        return whitelist;
                    }

                    @Override
                    public void setWhitelist(boolean whitelist) {
                        ItemConnection.this.whitelist = whitelist;
                        helper.callMarkDirty();
                    }

                    @Override
                    public ItemStack getStack(int slot) {
                        return stacks[slot];
                    }

                    @Override
                    public void setStack(int slot, ItemStack stack) {
                        stacks[slot] = stack;
                        refreshStacksCache();
                        helper.callMarkDirty();
                    }

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
                            ItemConnection.this.type = decodeConnectionType(type);
                            helper.callMarkDirty();
                            helper.callSync();
                        }
                    }

                    @Override
                    public int getPriority() {
                        return priority;
                    }

                    @Override
                    public void setPriority(int priority) {
                        ItemConnection.this.priority = priority;
                        helper.callMarkDirty();
                    }

                    @Override
                    public boolean canUse(PlayerEntity player) {
                        // Check that the BE is within distance
                        if (!helper.isWithinUseDistance(player)) {
                            return false;
                        }
                        // Check that this connection still exists
                        return helper.doesNodeStillExist(ItemNetworkNode.this) && connections.contains(ItemConnection.this);
                    }
                };
                this.pipeType = pipeType;
            }

            @Override
            public Text getDisplayName() {
                return new TranslatableText("item." + pipeType.getNamespace() + "." + pipeType.getPath());
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ItemPipeScreenHandler(syncId, inv, iface);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
                iface.toBuf(packetByteBuf);
            }
        }
    }
}
