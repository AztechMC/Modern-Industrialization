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
package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.inventory.ConfigurableItemStack.ConfigurableItemSlot;
import aztech.modern_industrialization.util.Simulation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class ConfigurableInventoryPacketHandlers {
    public static class C2S {
        // sync id, whether to lock
        public static final ServerPlayNetworking.PlayChannelHandler LOCK_ALL = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            boolean lock = buf.readBoolean();
            ms.execute(() -> {
                AbstractContainerMenu menu = player.containerMenu;
                if (menu.containerId == syncId) {
                    ((ConfigurableScreenHandler) menu).lockAll(lock);
                }
            });
        };

        // sync id, new locking mode
        public static final ServerPlayNetworking.PlayChannelHandler SET_LOCKING_MODE = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            boolean lockingMode = buf.readBoolean();
            ms.execute(() -> {
                AbstractContainerMenu sh = player.containerMenu;
                if (sh.containerId == syncId) {
                    ConfigurableScreenHandler csh = (ConfigurableScreenHandler) sh;
                    csh.lockingMode = lockingMode;
                }
            });
        };

        // sync id, slot id, boolean: true for itemkey, false for fluidkey, item or
        // fluid key
        public static final ServerPlayNetworking.PlayChannelHandler DO_SLOT_DRAGGING = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            int slotId = buf.readVarInt();
            boolean isItemKey = buf.readBoolean();
            ItemVariant itemKey = isItemKey ? ItemVariant.fromPacket(buf) : null;
            FluidVariant fluidKey = isItemKey ? null : FluidVariant.fromPacket(buf);
            ms.execute(() -> {
                AbstractContainerMenu sh = player.containerMenu;
                if (sh.containerId == syncId) {
                    Slot slot = sh.getSlot(slotId);
                    ReiDraggable dw = (ReiDraggable) slot;
                    if (isItemKey) {
                        dw.dragItem(itemKey, Simulation.ACT);
                    } else {
                        dw.dragFluid(fluidKey, Simulation.ACT);
                    }
                }
            });
        };

        public static final ServerPlayNetworking.PlayChannelHandler ADJUST_SLOT_CAPACITY = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            int slotId = buf.readVarInt();
            boolean isIncrease = buf.readBoolean();
            boolean isShiftDown = buf.readBoolean();
            ms.execute(() -> {
                AbstractContainerMenu sh = player.containerMenu;
                if (sh.containerId == syncId) {
                    Slot slot = sh.getSlot(slotId);
                    if (slot instanceof ConfigurableItemSlot confSlot) {
                        confSlot.getConfStack().adjustCapacity(isIncrease, isShiftDown);
                    }
                }
            });
        };
    }
}
