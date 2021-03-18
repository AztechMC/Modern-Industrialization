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
package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.components.sync.AutoExtract;
import aztech.modern_industrialization.machinesv2.components.sync.ReiSlotLocking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class MachinePackets {
    public static class S2C {
        public static final Identifier COMPONENT_SYNC = new MIIdentifier("machine_component_sync");
        @Environment(EnvType.CLIENT)
        public static final ClientPlayNetworking.PlayChannelHandler ON_COMPONENT_SYNC = (mc, handler, buf, sender) -> {
            int syncId = buf.readInt();
            int componentIndex = buf.readInt();
            buf.retain();
            mc.execute(() -> {
                try {
                    if (mc.player.currentScreenHandler.syncId == syncId) {
                        MachineScreenHandlers.Client screenHandler = (MachineScreenHandlers.Client) mc.player.currentScreenHandler;
                        screenHandler.components.get(componentIndex).read(buf);
                    }
                } finally {
                    buf.release();
                }
            });
        };
    }

    public static class C2S {
        public static final Identifier SET_AUTO_EXTRACT = new MIIdentifier("set_auto_extract");
        public static final ServerPlayNetworking.PlayChannelHandler ON_SET_AUTO_EXTRACT = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            boolean isItem = buf.readBoolean();
            boolean isExtract = buf.readBoolean();
            ms.execute(() -> {
                if (player.currentScreenHandler.syncId == syncId) {
                    MachineScreenHandlers.Server screenHandler = (MachineScreenHandlers.Server) player.currentScreenHandler;
                    AutoExtract.Server autoExtract = screenHandler.blockEntity.getComponent(SyncedComponents.AUTO_EXTRACT);
                    OrientationComponent orientation = autoExtract.getOrientation();
                    if (isItem) {
                        orientation.extractItems = isExtract;
                    } else {
                        orientation.extractFluids = isExtract;
                    }
                    screenHandler.blockEntity.markDirty();
                    screenHandler.blockEntity.sync();
                }
            });
        };
        public static final Identifier REI_LOCK_SLOTS = new MIIdentifier("rei_lock_slots");
        public static final ServerPlayNetworking.PlayChannelHandler ON_REI_LOCK_SLOTS = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            Identifier recipeId = buf.readIdentifier();
            ms.execute(() -> {
                ScreenHandler sh = player.currentScreenHandler;
                if (sh.syncId == syncId && sh instanceof MachineScreenHandlers.Server) {
                    MachineScreenHandlers.Server screenHandler = (MachineScreenHandlers.Server) sh;
                    // Check that locking the slots is allowed in the first place
                    ReiSlotLocking.Server slotLocking = screenHandler.blockEntity.getComponent(SyncedComponents.REI_SLOT_LOCKING);
                    if (!slotLocking.allowLocking.get())
                        return;

                    // Lock
                    slotLocking.slotLockable.lockSlots(recipeId, player.inventory);
                }
            });
        };
    }
}
