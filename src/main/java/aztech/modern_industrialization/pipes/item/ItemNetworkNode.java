package aztech.modern_industrialization.pipes.item;

import static aztech.modern_industrialization.pipes.api.PipeConnectionType.*;

import alexiil.mc.lib.attributes.SearchOption;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.item.*;
import aztech.modern_industrialization.pipes.api.PipeConnectionType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.util.ItemStackHelper;
import java.util.*;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
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
    private List<ItemConnection> connections = new ArrayList<>();
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
                connections.remove(i);
            }
        }
    }

    private boolean canConnect(World world, BlockPos pos, Direction direction) {
        SearchOption option = SearchOptions.inDirection(direction);
        return ItemAttributes.INSERTABLE.getAll(world, pos.offset(direction), option).hasOfferedAny()
                || ItemAttributes.EXTRACTABLE.getAll(world, pos.offset(direction), option).hasOfferedAny();
    }

    @Override
    public PipeConnectionType[] getConnections(BlockPos pos) {
        PipeConnectionType[] connections = new PipeConnectionType[6];
        for (Direction direction : network.manager.getNodeLinks(pos)) {
            connections[direction.getId()] = ITEM;
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
                if (conn.type == ITEM_IN)
                    conn.type = ITEM_IN_OUT;
                else if (conn.type == ITEM_IN_OUT)
                    conn.type = ITEM_OUT;
                else
                    connections.remove(i);
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
            connections.add(new ItemConnection(direction, ITEM_IN, 0));
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
                connections.add(connection);
            }
        }
        inactiveTicks = tag.getInt("inactiveTicks");
    }

    private static PipeConnectionType decodeConnectionType(int i) {
        return i == 0 ? ITEM_IN : i == 1 ? ITEM_IN_OUT : ITEM_OUT;
    }

    private static int encodeConnectionType(PipeConnectionType connection) {
        return connection == ITEM_IN ? 0 : connection == ITEM_IN_OUT ? 1 : 2;
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
                    int movesLeft = 16;
                    if (reachableInputs == null)
                        reachableInputs = getInputs(world, pos);
                    ItemExtractable extractable = ItemAttributes.EXTRACTABLE.get(world, pos.offset(connection.direction),
                            SearchOptions.inDirection(connection.direction));
                    for (InsertTarget target : reachableInputs) {
                        if (target.connection.canInsert()) {
                            int moved = ItemInvUtil.moveMultiple(extractable, target.insertable,
                                    s -> connection.canStackMoveThrough(s) && target.connection.canStackMoveThrough(s), movesLeft,
                                    movesLeft).itemsMoved;
                            movesLeft -= moved;
                            if (movesLeft == 0)
                                continue outer;
                        }
                    }
                }
            }
            inactiveTicks = 100;
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
                            SearchOption option = SearchOptions.inDirection(connection.direction);
                            result.add(new InsertTarget(connection, ItemAttributes.INSERTABLE.get(world, u.offset(connection.direction), option)));
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
        private final ItemInsertable insertable;

        private InsertTarget(ItemConnection connection, ItemInsertable insertable) {
            this.connection = connection;
            this.insertable = insertable;
        }
    }

    private static class ItemConnection {
        private final Direction direction;
        private PipeConnectionType type;
        private boolean whitelist = true;
        private int priority;
        private final ItemStack[] stacks = new ItemStack[ItemPipeInterface.SLOTS];

        private ItemConnection(Direction direction, PipeConnectionType type, int priority) {
            this.direction = direction;
            this.type = type;
            this.priority = priority;
            for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                stacks[i] = ItemStack.EMPTY;
            }
        }

        private boolean canInsert() {
            return type == ITEM_IN || type == ITEM_IN_OUT;
        }

        private boolean canExtract() {
            return type == ITEM_OUT || type == ITEM_IN_OUT;
        }

        private boolean canStackMoveThrough(ItemStack stack) {
            for (ItemStack filterStack : stacks) {
                if (ItemStackHelper.areEqualIgnoreCount(stack, filterStack)) {
                    return whitelist;
                }
            }
            return !whitelist;
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
