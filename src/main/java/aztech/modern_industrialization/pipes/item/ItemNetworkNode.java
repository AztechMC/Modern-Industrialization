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
import aztech.modern_industrialization.util.ItemStackHelper;
import java.util.*;

import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemApi;
import net.fabricmc.fabric.api.transfer.v1.storage.Movement;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

// TODO: item filters
public class ItemNetworkNode extends PipeNetworkNode {
    private final List<ItemConnection> connections = new ArrayList<>();
    private int inactiveTicks = 0;

    @Override
    public void updateConnections(World world, BlockPos pos) {
        // We don't connect by default, so we just have to remove connections that have
        // become unavailable
        for (int i = 0; i < connections.size();) {
            ItemConnection conn = connections.get(i);
            if (canConnect(world, pos, conn.direction)) {
                i++;
            } else {
                conn.dropUpgrades(world, pos);
                connections.remove(i);
            }
        }
    }

    private boolean canConnect(World world, BlockPos pos, Direction direction) {
        Storage<ItemKey> io = ItemApi.SIDED.get(world, pos.offset(direction), direction.getOpposite());
        return io != null && (!io.insertionFunction().isEmpty() || !io.extractionFunction().isEmpty());
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
    public CompoundTag toTag(CompoundTag tag) {
        for (ItemConnection connection : connections) {
            CompoundTag connectionTag = new CompoundTag();
            connectionTag.putByte("connections", (byte) encodeConnectionType(connection.type));
            connectionTag.putBoolean("whitelist", connection.whitelist);
            connectionTag.putInt("priority", connection.priority);
            for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                connectionTag.put(Integer.toString(i), connection.stacks[i].toTag(new CompoundTag()));
            }
            connectionTag.put("upgradeStack", connection.upgradeStack.toTag(new CompoundTag()));
            tag.put(connection.direction.toString(), connectionTag);
        }
        tag.putInt("inactiveTicks", inactiveTicks);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        for (Direction direction : Direction.values()) {
            if (tag.contains(direction.toString())) {
                CompoundTag connectionTag = tag.getCompound(direction.toString());
                ItemConnection connection = new ItemConnection(direction, decodeConnectionType(connectionTag.getByte("connections")),
                        connectionTag.getInt("priority"));
                connection.whitelist = connectionTag.getBoolean("whitelist");
                for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                    connection.stacks[i] = ItemStack.fromTag(connectionTag.getCompound(Integer.toString(i)));
                    connection.stacks[i].setCount(1);
                }
                connection.upgradeStack = ItemStack.fromTag(connectionTag.getCompound("upgradeStack"));
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
    public ExtendedScreenHandlerFactory getConnectionGui(Direction guiDirection, Runnable markDirty, Runnable sync) {
        for (ItemConnection connection : connections) {
            if (connection.direction == guiDirection) {
                return connection.new ScreenHandlerFactory(markDirty, sync, getType().getIdentifier().getPath());
            }
        }
        return null;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (inactiveTicks == 0) {
            List<InsertTarget> reachableInputs = null;
            outer: for (ItemConnection connection : connections) { // TODO: optimize!
                if (connection.canExtract()) {
                    Storage<ItemKey> source = ItemApi.SIDED.get(world, pos.offset(connection.direction), connection.direction.getOpposite());
                    if (source != null && !source.extractionFunction().isEmpty()) {
                        long movesLeft = connection.getMoves();
                        if (reachableInputs == null)
                            reachableInputs = getInputs(world, pos);
                        for (InsertTarget target : reachableInputs) {
                            if (target.connection.canInsert()) {
                                long moved = Movement.move(
                                        source, target.io.insertionFunction(),
                                        k -> connection.canStackMoveThrough(k.toStack()) && target.connection.canStackMoveThrough(k.toStack()),
                                        movesLeft, 1);
                                movesLeft -= moved;
                                if (movesLeft == 0)
                                    continue outer;
                            }
                        }
                    }
                }
            }
            inactiveTicks = 60;
        }
        --inactiveTicks;
    }

    /**
     * Run a bfs to find all connections in which to insert that are loaded and
     * reachable from the given startPos.
     */
    public List<InsertTarget> getInputs(World world, BlockPos startPos) {
        List<InsertTarget> result = new ArrayList<>();

        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(startPos);
        while (!queue.isEmpty()) {
            BlockPos u = queue.remove();
            if (visited.add(u)) {
                PipeNetworkNode maybeUnloaded = network.nodes.get(u);
                if (maybeUnloaded != null) {
                    ItemNetworkNode node = (ItemNetworkNode) maybeUnloaded;
                    for (ItemConnection connection : node.connections) {
                        if (connection.canInsert()) {
                            Storage<ItemKey> target = ItemApi.SIDED.get(world, u.offset(connection.direction), connection.direction.getOpposite());
                            if (target != null && !target.insertionFunction().isEmpty()) {
                                result.add(new InsertTarget(connection, target));
                            }
                        }
                    }
                }
                for (Direction direction : network.manager.getNodeLinks(u)) {
                    queue.add(u.offset(direction));
                }
            }
        }

        // Now we sort by priority, high to low
        result.sort(Comparator.comparing(target -> -target.connection.priority));
        // We randomly shuffle for connections with the same priority
        int prevPriority = Integer.MIN_VALUE;
        int st = 0;
        for (int i = 0; i < result.size() + 1; ++i) {
            int p = i == result.size() ? Integer.MAX_VALUE : result.get(i).connection.priority;
            if (p != prevPriority) {
                Collections.shuffle(result.subList(st, i));
                prevPriority = p;
                st = i;
            }
        }

        return result;
    }

    private static class InsertTarget {
        private final ItemConnection connection;
        private final Storage<ItemKey> io;

        private InsertTarget(ItemConnection connection, Storage<ItemKey> io) {
            this.connection = connection;
            this.io = io;
        }
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

    private static class ItemConnection {
        private final Direction direction;
        private PipeEndpointType type;
        private boolean whitelist = true;
        private int priority;
        private final ItemStack[] stacks = new ItemStack[ItemPipeInterface.SLOTS];
        private ItemStack upgradeStack = ItemStack.EMPTY;

        private ItemConnection(Direction direction, PipeEndpointType type, int priority) {
            this.direction = direction;
            this.type = type;
            this.priority = priority;
            for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                stacks[i] = ItemStack.EMPTY;
            }
        }

        private boolean canInsert() {
            return type == BLOCK_IN || type == BLOCK_IN_OUT;
        }

        private boolean canExtract() {
            return type == BLOCK_OUT || type == BLOCK_IN_OUT;
        }

        private boolean canStackMoveThrough(ItemStack stack) {
            for (ItemStack filterStack : stacks) {
                if (ItemStackHelper.areEqualIgnoreCount(stack, filterStack)) {
                    return whitelist;
                }
            }
            return !whitelist;
        }

        private long getMoves() {
            SpeedUpgrade upgrade = SpeedUpgrade.LOOKUP.get(ItemKey.of(upgradeStack), null);
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
            private final String pipeType;

            private ScreenHandlerFactory(Runnable markDirty, Runnable sync, String pipeType) {
                this.iface = new ItemPipeInterface() {
                    @Override
                    public boolean isWhitelist() {
                        return whitelist;
                    }

                    @Override
                    public void setWhitelist(boolean whitelist) {
                        ItemConnection.this.whitelist = whitelist;
                        markDirty.run();
                    }

                    @Override
                    public ItemStack getStack(int slot) {
                        return stacks[slot];
                    }

                    @Override
                    public void setStack(int slot, ItemStack stack) {
                        stacks[slot] = stack;
                        markDirty.run();
                    }

                    @Override
                    public ItemStack getUpgradeStack() {
                        return upgradeStack;
                    }

                    @Override
                    public void setUpgradeStack(ItemStack stack) {
                        upgradeStack = stack;
                        markDirty.run();
                    }

                    @Override
                    public int getConnectionType() {
                        return encodeConnectionType(type);
                    }

                    @Override
                    public void setConnectionType(int type) {
                        if (0 <= type && type < 3) {
                            ItemConnection.this.type = decodeConnectionType(type);
                            sync.run();
                        }
                    }

                    @Override
                    public int getPriority() {
                        return priority;
                    }

                    @Override
                    public void setPriority(int priority) {
                        ItemConnection.this.priority = priority;
                    }
                };
                this.pipeType = pipeType;
            }

            @Override
            public Text getDisplayName() {
                return new TranslatableText("item.modern_industrialization.pipe_" + pipeType);
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
