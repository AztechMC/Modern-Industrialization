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

import aztech.modern_industrialization.machines.impl.MachineScreenHandler;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandler;

public class ConfigurableInventoryPacketHandlers {
    // sync id, slot id, slot tag
    public static final PacketConsumer UPDATE_ITEM_SLOT = (context, data) -> {
        int syncId = data.readInt();
        int stackId = data.readInt();
        CompoundTag tag = data.readCompoundTag();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
            if (handler.syncId == syncId) {
                MachineScreenHandler machineHandler = (MachineScreenHandler) handler;
                machineHandler.inventory.getInventory().itemStacks.get(stackId).readFromTag(tag);
            }
        });
    };
    // sync id, slot id, slot tag
    public static final PacketConsumer UPDATE_FLUID_SLOT = (context, data) -> {
        int syncId = data.readInt();
        int stackId = data.readInt();
        CompoundTag tag = data.readCompoundTag();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
            if (handler.syncId == syncId) {
                MachineScreenHandler machineHandler = (MachineScreenHandler) handler;
                machineHandler.inventory.getInventory().fluidStacks.get(stackId).readFromTag(tag);
            }
        });
    };
    // sync id, new locking mode
    public static final PacketConsumer SET_LOCKING_MODE = (context, data) -> {
        int syncId = data.readInt();
        boolean lockingMode = data.readBoolean();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = context.getPlayer().currentScreenHandler;
            if (handler.syncId == syncId) {
                ConfigurableScreenHandler confHandler = (ConfigurableScreenHandler) handler;
                confHandler.lockingMode = lockingMode;
            }
        });
    };
}
