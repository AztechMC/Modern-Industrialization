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

import aztech.modern_industrialization.api.datamaps.MIDataMaps;
import aztech.modern_industrialization.network.pipes.SetConnectionTypePacket;
import aztech.modern_industrialization.network.pipes.SetPriorityPacket;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.gui.PipeScreenHandler;
import aztech.modern_industrialization.util.UnsupportedOperationInventory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FEWireScreenHandler extends PipeScreenHandler {
    public static final int HEIGHT = 153;

    private final Inventory playerInventory;
    public final FEWireInterface iface;
    private int trackedPriority0;
    private int trackedType;

    public static final int UPGRADE_SLOT_X = 72, UPGRADE_SLOT_Y = 20;

    public FEWireScreenHandler(int syncId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(syncId, playerInventory, FEWireInterface.ofBuf(buf));
    }

    public FEWireScreenHandler(int syncId, Inventory playerInventory, FEWireInterface iface) {
        super(MIPipes.SCREEN_HANDLER_TYPE_FE_WIRE.get(), syncId);
        this.playerInventory = playerInventory;
        this.iface = iface;
        this.trackedPriority0 = iface.getPriority(0);
        this.trackedType = iface.getConnectionType();

        addPlayerInventorySlots(playerInventory, HEIGHT);

        addSlot(new UpgradeSlot(UPGRADE_SLOT_X, UPGRADE_SLOT_Y));
    }

    @Override
    protected Object getInterface() {
        return iface;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            if (index < 36) {
                // Try to insert into the upgrade slot.
                if (moveItemStackTo(slot.getItem(), 36, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 36) { // upgrade slot
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
        return iface.canUse(player);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (playerInventory.player instanceof ServerPlayer player) {
            if (trackedType != iface.getConnectionType()) {
                trackedType = iface.getConnectionType();
                new SetConnectionTypePacket(containerId, trackedType).sendToClient(player);
            }
            if (trackedPriority0 != iface.getPriority(0)) {
                trackedPriority0 = iface.getPriority(0);
                new SetPriorityPacket(containerId, 0, trackedPriority0).sendToClient(player);
            }
        }
    }

    public class UpgradeSlot extends Slot {
        public UpgradeSlot(int x, int y) {
            super(new UnsupportedOperationInventory(), -1, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (iface.getConnectionType() == 0) {
                // Prevent placing motors in `IN` pipes.
                return false;
            }
            return stack.getItemHolder().getData(MIDataMaps.FE_WIRE_UPGRADES) != null;
        }

        @Override
        public boolean isHighlightable() {
            return iface.getConnectionType() != 0;
        }

        @Override
        public ItemStack getItem() {
            return iface.getUpgradeStack();
        }

        @Override
        public void set(ItemStack stack) {
            iface.setUpgradeStack(stack);
        }

        @Override
        public void setChanged() {
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public ItemStack remove(int amount) {
            return iface.getUpgradeStack().split(amount);
        }
    }
}
