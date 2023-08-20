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

import aztech.modern_industrialization.api.pipe.item.SpeedUpgrade;
import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.gui.PipeScreenHandler;
import aztech.modern_industrialization.pipes.impl.PipePackets;
import aztech.modern_industrialization.util.ItemStackHelper;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.UnsupportedOperationInventory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ItemPipeScreenHandler extends PipeScreenHandler {
    public static final int HEIGHT = 196;

    private final Inventory playerInventory;
    public final ItemPipeInterface pipeInterface;
    private boolean trackedWhitelist;
    private int trackedPriority0;
    private int trackedPriority1;
    private int trackedType;

    public static final int UPGRADE_SLOT_X = 150, UPGRADE_SLOT_Y = 70;

    public ItemPipeScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(syncId, playerInventory, ItemPipeInterface.ofBuf(buf));
    }

    public ItemPipeScreenHandler(int syncId, Inventory playerInventory, ItemPipeInterface pipeInterface) {
        super(MIPipes.SCREEN_HANDLER_TYPE_ITEM_PIPE, syncId);
        this.playerInventory = playerInventory;
        this.pipeInterface = pipeInterface;
        this.trackedWhitelist = pipeInterface.isWhitelist();
        this.trackedPriority0 = pipeInterface.getPriority(0);
        this.trackedPriority1 = pipeInterface.getPriority(1);
        this.trackedType = pipeInterface.getConnectionType();

        addPlayerInventorySlots(playerInventory, HEIGHT);

        // Filter slots
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 7; ++j) {
                this.addSlot(new FilterSlot(i * 7 + j, 16 + 18 * j, 18 + 18 * i));
            }
        }

        // Upgrade slot
        this.addSlot(new UpgradeSlot(UPGRADE_SLOT_X, UPGRADE_SLOT_Y));
    }

    @Override
    public void clicked(int i, int j, ClickType actionType, Player playerEntity) {
        if (i >= 0) {
            Slot slot = slots.get(i);
            if (slot instanceof FilterSlot) {
                if (actionType == ClickType.PICKUP) {
                    slot.set(getCarried().copy());
                } else if (actionType == ClickType.QUICK_MOVE) {
                    slot.set(ItemStack.EMPTY);
                }
                return;
            }
        }
        super.clicked(i, j, actionType, playerEntity);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            if (index < 36) {
                // Try to insert into the upgrade slot.
                if (moveItemStackTo(slot.getItem(), 57, 58, false)) {
                    return ItemStack.EMPTY;
                }
                // Return if the stack is already in the filter.
                for (int i = 0; i < 21; i++) {
                    if (ItemStackHelper.areEqualIgnoreCount(slots.get(36 + i).getItem(), slot.getItem())) {
                        return ItemStack.EMPTY;
                    }
                }
                // Copy the stack into the filter.
                for (int i = 0; i < 21; i++) {
                    if (pipeInterface.getStack(i).isEmpty()) {
                        slots.get(36 + i).set(slot.getItem().copy());
                        break;
                    }
                }
            } else if (index == 57) { // upgrade slot
                if (!moveItemStackTo(slot.getItem(), 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                throw new RuntimeException("Can't transfer slot from that index.");
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return pipeInterface.canUse(player);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (playerInventory.player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) playerInventory.player;
            if (trackedWhitelist != pipeInterface.isWhitelist()) {
                trackedWhitelist = pipeInterface.isWhitelist();
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(containerId);
                buf.writeBoolean(trackedWhitelist);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_ITEM_WHITELIST, buf);
            }
            if (trackedType != pipeInterface.getConnectionType()) {
                trackedType = pipeInterface.getConnectionType();
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(containerId);
                buf.writeInt(trackedType);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_CONNECTION_TYPE, buf);
            }
            if (trackedPriority0 != pipeInterface.getPriority(0)) {
                trackedPriority0 = pipeInterface.getPriority(0);
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(containerId);
                buf.writeInt(0);
                buf.writeInt(trackedPriority0);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_PRIORITY, buf);
            }
            if (trackedPriority1 != pipeInterface.getPriority(1)) {
                trackedPriority1 = pipeInterface.getPriority(1);
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(containerId);
                buf.writeInt(1);
                buf.writeInt(trackedPriority1);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_PRIORITY, buf);
            }
        }
    }

    @Override
    protected Object getInterface() {
        return pipeInterface;
    }

    private class FilterSlot extends Slot implements ReiDraggable {
        private final int index;

        public FilterSlot(int index, int x, int y) {
            super(new UnsupportedOperationInventory(), index, x, y);
            this.index = index;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player playerEntity) {
            return false;
        }

        @Override
        public ItemStack getItem() {
            return pipeInterface.getStack(index);
        }

        @Override
        public void set(ItemStack stack) {
            if (!stack.isEmpty()) {
                stack.setCount(1);
            }
            pipeInterface.setStack(index, stack);
        }

        @Override
        public boolean dragFluid(FluidVariant fluidKey, Simulation simulation) {
            return false;
        }

        @Override
        public boolean dragItem(ItemVariant itemKey, Simulation simulation) {
            if (simulation.isActing()) {
                set(itemKey.toStack());
            }
            return true;
        }
    }

    public class UpgradeSlot extends Slot {
        public UpgradeSlot(int x, int y) {
            super(new UnsupportedOperationInventory(), -1, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return SpeedUpgrade.UPGRADES.getOrDefault(stack.getItem(), 0L) != 0L;
        }

        @Override
        public ItemStack getItem() {
            return pipeInterface.getUpgradeStack();
        }

        @Override
        public void set(ItemStack stack) {
            pipeInterface.setUpgradeStack(stack);
        }

        @Override
        public void setChanged() {
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 64;
        }

        @Override
        public ItemStack remove(int amount) {
            return pipeInterface.getUpgradeStack().split(amount);
        }
    }
}
