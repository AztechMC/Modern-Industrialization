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

import aztech.modern_industrialization.api.ReiDraggable;
import aztech.modern_industrialization.api.pipes.item.SpeedUpgrade;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemPipeScreenHandler extends PipeScreenHandler {
    public static final int HEIGHT = 196;

    private final PlayerInventory playerInventory;
    public final ItemPipeInterface pipeInterface;
    private boolean trackedWhitelist;
    private int trackedPriority0;
    private int trackedPriority1;
    private int trackedType;

    public ItemPipeScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, ItemPipeInterface.ofBuf(buf));
    }

    public ItemPipeScreenHandler(int syncId, PlayerInventory playerInventory, ItemPipeInterface pipeInterface) {
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
        this.addSlot(new UpgradeSlot(150, 70));
    }

    @Override
    public void onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if (i >= 0) {
            Slot slot = slots.get(i);
            if (slot instanceof FilterSlot) {
                if (actionType == SlotActionType.PICKUP) {
                    slot.setStack(getCursorStack().copy());
                } else if (actionType == SlotActionType.QUICK_MOVE) {
                    slot.setStack(ItemStack.EMPTY);
                }
                return;
            }
        }
        super.onSlotClick(i, j, actionType, playerEntity);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        if (slot != null && slot.hasStack()) {
            if (index < 36) {
                // Try to insert into the upgrade slot.
                if (insertItem(slot.getStack(), 57, 58, false)) {
                    return ItemStack.EMPTY;
                }
                // Return if the stack is already in the filter.
                for (int i = 0; i < 21; i++) {
                    if (ItemStackHelper.areEqualIgnoreCount(slots.get(36 + i).getStack(), slot.getStack())) {
                        return ItemStack.EMPTY;
                    }
                }
                // Copy the stack into the filter.
                for (int i = 0; i < 21; i++) {
                    if (pipeInterface.getStack(i).isEmpty()) {
                        slots.get(36 + i).setStack(slot.getStack().copy());
                        break;
                    }
                }
            } else if (index == 57) { // upgrade slot
                if (!insertItem(slot.getStack(), 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                throw new RuntimeException("Can't transfer slot from that index.");
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return pipeInterface.canUse(player);
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
        if (playerInventory.player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) playerInventory.player;
            if (trackedWhitelist != pipeInterface.isWhitelist()) {
                trackedWhitelist = pipeInterface.isWhitelist();
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(syncId);
                buf.writeBoolean(trackedWhitelist);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_ITEM_WHITELIST, buf);
            }
            if (trackedType != pipeInterface.getConnectionType()) {
                trackedType = pipeInterface.getConnectionType();
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(syncId);
                buf.writeInt(trackedType);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_CONNECTION_TYPE, buf);
            }
            if (trackedPriority0 != pipeInterface.getPriority(0)) {
                trackedPriority0 = pipeInterface.getPriority(0);
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(syncId);
                buf.writeInt(0);
                buf.writeInt(trackedPriority0);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_PRIORITY, buf);
            }
            if (trackedPriority1 != pipeInterface.getPriority(1)) {
                trackedPriority1 = pipeInterface.getPriority(1);
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(syncId);
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
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        @Override
        public ItemStack getStack() {
            return pipeInterface.getStack(index);
        }

        @Override
        public void setStack(ItemStack stack) {
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
                setStack(itemKey.toStack());
            }
            return true;
        }
    }

    private class UpgradeSlot extends Slot {
        public UpgradeSlot(int x, int y) {
            super(new UnsupportedOperationInventory(), -1, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return SpeedUpgrade.LOOKUP.find(stack, null) != null;
        }

        @Override
        public ItemStack getStack() {
            return pipeInterface.getUpgradeStack();
        }

        @Override
        public void setStack(ItemStack stack) {
            pipeInterface.setUpgradeStack(stack);
        }

        @Override
        public void markDirty() {
        }

        @Override
        public int getMaxItemCount(ItemStack stack) {
            return 64;
        }

        @Override
        public ItemStack takeStack(int amount) {
            return pipeInterface.getUpgradeStack().split(amount);
        }
    }
}
