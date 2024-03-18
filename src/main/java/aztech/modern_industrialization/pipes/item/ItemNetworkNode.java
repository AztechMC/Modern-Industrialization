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

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.datamaps.MIDataMaps;
import aztech.modern_industrialization.items.ConfigCardItem;
import aztech.modern_industrialization.pipes.api.IPipeMenuProvider;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.gui.IPipeScreenHandlerHelper;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeNetworks;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.TransferHelper;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import org.jetbrains.annotations.Nullable;

public class ItemNetworkNode extends PipeNetworkNode {
    final List<ItemConnection> connections = new ArrayList<>();
    int inactiveTicks = 0;

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

    private boolean canConnect(Level world, BlockPos pos, Direction direction) {
        BlockPos adjPos = pos.relative(direction);
        return world.getCapability(Capabilities.ItemHandler.BLOCK, adjPos, direction.getOpposite()) != null;
    }

    @Override
    public PipeEndpointType[] getConnections(BlockPos pos) {
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for (Direction direction : network.manager.getNodeLinks(pos)) {
            connections[direction.get3DDataValue()] = PIPE;
        }
        for (ItemConnection connection : this.connections) {
            connections[connection.direction.get3DDataValue()] = connection.type;
        }
        return connections;
    }

    @Override
    public void removeConnection(Level world, BlockPos pos, Direction direction) {
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
    public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
        // Refuse if it already exists
        for (ItemConnection connection : connections) {
            if (connection.direction == direction) {
                return;
            }
        }
        // Otherwise try to connect
        if (canConnect(world, pos, direction)) {
            var conn = new ItemConnection(direction, BLOCK_IN, 0, -10);
            connections.add(conn);
            // Apply memory card in the off-hand.
            var offHandItem = player.getOffhandItem();
            if (MIItem.CONFIG_CARD.is(offHandItem)) {
                conn.applyConfig(pipe, offHandItem.getTagElement(ConfigCardItem.TAG_SAVEDCONFIG), player);
            }
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        for (ItemConnection connection : connections) {
            CompoundTag connectionTag = new CompoundTag();
            connectionTag.putByte("connections", (byte) encodeConnectionType(connection.type));
            connectionTag.putBoolean("whitelist", connection.whitelist);
            connectionTag.putInt("insertPriority", connection.insertPriority);
            connectionTag.putInt("extractPriority", connection.extractPriority);
            for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                connectionTag.put(Integer.toString(i), connection.stacks[i].save(new CompoundTag()));
            }
            connectionTag.put("upgradeStack", connection.upgradeStack.save(new CompoundTag()));
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
                int insertPriority = connectionTag.getInt("insertPriority");
                int extractPriority = connectionTag.getInt("extractPriority");
                ItemConnection connection = new ItemConnection(direction, decodeConnectionType(connectionTag.getByte("connections")),
                        insertPriority, extractPriority);
                connection.whitelist = connectionTag.getBoolean("whitelist");
                for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                    connection.stacks[i] = ItemStack.of(connectionTag.getCompound(Integer.toString(i)));
                    if (!connection.stacks[i].isEmpty()) {
                        connection.stacks[i].setCount(1);
                    }
                }
                connection.refreshStacksCache();
                connection.upgradeStack = ItemStack.of(connectionTag.getCompound("upgradeStack"));
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
    public IPipeMenuProvider getConnectionGui(Direction guiDirection, IPipeScreenHandlerHelper helper) {
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

    @Override
    public boolean customUse(PipeBlockEntity pipe, Player player, InteractionHand hand, @Nullable Direction hitDirection) {
        for (ItemConnection conn : connections) {
            if (conn.direction != hitDirection) {
                continue;
            }

            var stack = player.getItemInHand(hand);
            if (!MIItem.CONFIG_CARD.is(stack)) {
                return false;
            }

            if (player.isShiftKeyDown()) {
                stack.removeTagKey(ConfigCardItem.TAG_CAMOUFLAGE);
                stack.getOrCreateTag().put(ConfigCardItem.TAG_SAVEDCONFIG, conn.getConfig());
                player.displayClientMessage(MIText.ConfigCardSet.text(), true);
            } else if (stack.getTagElement(ConfigCardItem.TAG_SAVEDCONFIG) != null) {
                conn.applyConfig(pipe, stack.getTagElement(ConfigCardItem.TAG_SAVEDCONFIG), player);
                player.displayClientMessage(MIText.ConfigCardApplied.text(), true);
            }
            return true;
        }
        return false;
    }

    class ItemConnection {
        final Direction direction;
        private PipeEndpointType type;
        boolean whitelist = true;
        int insertPriority, extractPriority;
        final ItemStack[] stacks = new ItemStack[ItemPipeInterface.SLOTS];
        final Map<Item, List<ItemStack>> stacksCache = new IdentityHashMap<>();
        private ItemStack upgradeStack = ItemStack.EMPTY;
        BlockCapabilityCache<IItemHandler, @Nullable Direction> cache = null;

        private ItemConnection(Direction direction, PipeEndpointType type, int insertPriority, int extractPriority) {
            this.direction = direction;
            this.type = type;
            this.insertPriority = insertPriority;
            this.extractPriority = extractPriority;
            for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                stacks[i] = ItemStack.EMPTY;
            }
        }

        private void refreshStacksCache() {
            stacksCache.clear();
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    stacksCache.computeIfAbsent(stack.getItem(), k -> new ArrayList<>()).add(stack);
                }
            }
        }

        private boolean isInCache(ItemStack stack) {
            var list = stacksCache.get(stack.getItem());
            if (list == null) {
                return false;
            }
            for (ItemStack cachedStack : list) {
                if (ItemStack.isSameItemSameTags(cachedStack, stack)) {
                    return true;
                }
            }
            return false;
        }

        boolean canInsert() {
            return type == BLOCK_IN || type == BLOCK_IN_OUT;
        }

        boolean canExtract() {
            return type == BLOCK_OUT || type == BLOCK_IN_OUT;
        }

        boolean canStackMoveThrough(ItemStack stack) {
            return isInCache(stack) == whitelist;
        }

        int getMoves() {
            var upgradeData = upgradeStack.getItemHolder().getData(MIDataMaps.ITEM_PIPE_UPGRADES);
            int extraExtractedItems = upgradeData == null ? 0 : upgradeData.maxExtractedItems();
            return 16 + (extraExtractedItems * upgradeStack.getCount());
        }

        private void dropUpgrades(Level world, BlockPos pos) {
            if (!upgradeStack.isEmpty()) {
                world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), upgradeStack));
                upgradeStack = ItemStack.EMPTY;
            }
        }

        CompoundTag getConfig() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("connectionType", encodeConnectionType(type));
            tag.putBoolean("whitelist", whitelist);
            tag.putInt("insertPriority", insertPriority);
            tag.putInt("extractPriority", extractPriority);
            ListTag filterTag = new ListTag();
            for (ItemStack itemStack : stacks) {
                filterTag.add(itemStack.save(new CompoundTag()));
            }
            tag.put("filter", filterTag);
            tag.put("upgrade", upgradeStack.save(new CompoundTag()));
            return tag;
        }

        void applyConfig(PipeBlockEntity pipe, @Nullable CompoundTag tag, Player player) {
            if (tag == null) {
                return;
            }
            var decodedType = decodeConnectionType(tag.getInt("connectionType"));
            boolean remesh = decodedType != type;
            type = decodedType;
            whitelist = tag.getBoolean("whitelist");
            insertPriority = tag.getInt("insertPriority");
            extractPriority = tag.getInt("extractPriority");
            ListTag filterTag = tag.getList("filter", Tag.TAG_COMPOUND);
            for (int i = 0; i < ItemPipeInterface.SLOTS; i++) {
                stacks[i] = ItemStack.of(filterTag.getCompound(i));
                if (!stacks[i].isEmpty()) {
                    stacks[i].setCount(1);
                }
            }
            refreshStacksCache();

            ItemStack requestedUpgrade = ItemStack.of(tag.getCompound("upgrade"));
            if (player.getAbilities().instabuild) {
                // Creative mode -> apply upgrades immediately
                upgradeStack = requestedUpgrade;
            } else {
                // Otherwise -> try to grab the upgrades from the player's inventory, and deposit the old one.
                ItemVariant requestedVariant = ItemVariant.of(requestedUpgrade);

                if (requestedVariant.matches(upgradeStack)) {
                    int delta = requestedUpgrade.getCount() - upgradeStack.getCount();
                    if (delta > 0) {
                        upgradeStack.grow(fetchItems(player, requestedVariant, delta));
                    } else {
                        player.getInventory().placeItemBackInInventory(upgradeStack.split(-delta));
                    }
                } else {
                    player.getInventory().placeItemBackInInventory(upgradeStack);
                    upgradeStack = requestedVariant.toStack(fetchItems(player, requestedVariant, requestedUpgrade.getCount()));
                }
            }

            pipe.setChanged();
            if (remesh) {
                pipe.sync();
            }
        }

        private int fetchItems(Player player, ItemVariant what, int maxAmount) {
            return TransferHelper.extractMatching(new PlayerInvWrapper(player.getInventory()), what::matches, maxAmount).getCount();
        }

        private class ScreenHandlerFactory implements IPipeMenuProvider {
            private final ItemPipeInterface iface;
            private final ResourceLocation pipeType;

            private ScreenHandlerFactory(IPipeScreenHandlerHelper helper, ResourceLocation pipeType) {
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
                    public int getPriority(int channel) {
                        return channel == 0 ? insertPriority : extractPriority;
                    }

                    @Override
                    public void setPriority(int channel, int priority) {
                        if (channel == 0) {
                            ItemConnection.this.insertPriority = priority;
                        } else {
                            ItemConnection.this.extractPriority = priority;
                        }
                        helper.callMarkDirty();
                    }

                    @Override
                    public boolean canUse(Player player) {
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
            public Component getDisplayName() {
                return Component.translatable("item." + pipeType.getNamespace() + "." + pipeType.getPath());
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new ItemPipeScreenHandler(syncId, inv, iface);
            }

            @Override
            public void writeAdditionalData(FriendlyByteBuf packetByteBuf) {
                iface.toBuf(packetByteBuf);
            }
        }
    }

    // Used in the Waila plugin
    public InGameInfo collectNetworkInfo() {
        var itemNetwork = (ItemNetwork) network;
        return new InGameInfo(itemNetwork.lastMovedItems, itemNetwork.inactiveTicks);
    }

    public record InGameInfo(long movedItems, int pulse) {
    }
}
