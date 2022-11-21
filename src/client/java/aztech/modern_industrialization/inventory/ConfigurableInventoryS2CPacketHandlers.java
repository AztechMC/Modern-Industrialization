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

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class ConfigurableInventoryS2CPacketHandlers {
    // sync id, slot id, slot tag
    public static final ClientPlayNetworking.PlayChannelHandler UPDATE_ITEM_SLOT = (mc, handler, buf, sender) -> {
        int syncId = buf.readInt();
        int stackId = buf.readInt();
        CompoundTag tag = buf.readNbt();
        mc.execute(() -> {
            AbstractContainerMenu sh = mc.player.containerMenu;
            if (sh.containerId == syncId) {
                ConfigurableScreenHandler csh = (ConfigurableScreenHandler) sh;
                ConfigurableItemStack oldStack = csh.inventory.getItemStacks().get(stackId);
                // update stack
                ConfigurableItemStack newStack = new ConfigurableItemStack(tag);
                csh.inventory.getItemStacks().set(stackId, newStack);
                // update slot
                for (int i = 0; i < csh.slots.size(); ++i) {
                    Slot slot = csh.slots.get(i);
                    if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot is) {
                        if (is.getConfStack() == oldStack) {
                            csh.slots.set(i, newStack.new ConfigurableItemSlot(is));
                            return;
                        }
                    }
                }
                throw new RuntimeException("Could not find slot to replace!");
            }
        });
    };
    // sync id, slot id, slot tag
    public static final ClientPlayNetworking.PlayChannelHandler UPDATE_FLUID_SLOT = (mc, handler, buf, sender) -> {
        int syncId = buf.readInt();
        int stackId = buf.readInt();
        CompoundTag tag = buf.readNbt();
        mc.execute(() -> {
            AbstractContainerMenu sh = mc.player.containerMenu;
            if (sh.containerId == syncId) {
                ConfigurableScreenHandler csh = (ConfigurableScreenHandler) sh;
                ConfigurableFluidStack oldStack = csh.inventory.getFluidStacks().get(stackId);
                // update stack
                ConfigurableFluidStack newStack = new ConfigurableFluidStack(tag);
                csh.inventory.getFluidStacks().set(stackId, newStack);
                // update slot
                for (int i = 0; i < csh.slots.size(); ++i) {
                    Slot slot = csh.slots.get(i);
                    if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot fs) {
                        if (fs.getConfStack() == oldStack) {
                            csh.slots.set(i, newStack.new ConfigurableFluidSlot(fs));
                            return;
                        }
                    }
                }
                throw new RuntimeException("Could not find slot to replace!");
            }
        });
    };
}
