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
package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineMenuServer;
import aztech.modern_industrialization.machines.guicomponents.AutoExtract;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLocking;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MachinePackets {
    public static class S2C {
        public static final ResourceLocation COMPONENT_SYNC = new MIIdentifier("machine_component_sync");
    }

    public static class C2S {
        public static final ResourceLocation SET_AUTO_EXTRACT = new MIIdentifier("set_auto_extract");
        public static final ServerPlayNetworking.PlayChannelHandler ON_SET_AUTO_EXTRACT = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            boolean isItem = buf.readBoolean();
            boolean isExtract = buf.readBoolean();
            ms.execute(() -> {
                if (player.containerMenu.containerId == syncId) {
                    var screenHandler = (MachineMenuServer) player.containerMenu;
                    AutoExtract.Server autoExtract = screenHandler.blockEntity.getComponent(GuiComponents.AUTO_EXTRACT);
                    OrientationComponent orientation = autoExtract.getOrientation();
                    if (isItem) {
                        orientation.extractItems = isExtract;
                    } else {
                        orientation.extractFluids = isExtract;
                    }
                    screenHandler.blockEntity.setChanged();
                    screenHandler.blockEntity.sync();
                }
            });
        };
        public static final ResourceLocation REI_LOCK_SLOTS = new MIIdentifier("rei_lock_slots");
        public static final ServerPlayNetworking.PlayChannelHandler ON_REI_LOCK_SLOTS = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            ResourceLocation recipeId = buf.readResourceLocation();
            ms.execute(() -> {
                AbstractContainerMenu sh = player.containerMenu;
                if (sh.containerId == syncId && sh instanceof MachineMenuServer screenHandler) {
                    // Check that locking the slots is allowed in the first place
                    ReiSlotLocking.Server slotLocking = screenHandler.blockEntity.getComponent(GuiComponents.REI_SLOT_LOCKING);
                    if (!slotLocking.allowLocking.get())
                        return;

                    // Lock
                    slotLocking.slotLockable.lockSlots(recipeId, player.getInventory());
                }
            });
        };
        public static final ResourceLocation FORGE_HAMMER_MOVE_RECIPE = new MIIdentifier("forge_hammer_move_recipe");
        public static final ServerPlayNetworking.PlayChannelHandler ON_FORGE_HAMMER_MOVE_RECIPE = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            ResourceLocation recipeId = buf.readResourceLocation();
            int fillAction = buf.readByte();
            int amount = buf.readInt();
            ms.execute(() -> {
                AbstractContainerMenu menu = player.containerMenu;
                if (menu.containerId == syncId && menu instanceof ForgeHammerScreenHandler fh) {
                    fh.moveRecipe(recipeId, fillAction, amount);
                }
            });
        };
        public static final ResourceLocation CHANGE_SHAPE = new MIIdentifier("change_shape");
        public static final ServerPlayNetworking.PlayChannelHandler ON_CHANGE_SHAPE = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            int shapeLine = buf.readVarInt();
            boolean clickedLeftButton = buf.readBoolean();
            ms.execute(() -> {
                AbstractContainerMenu menu = player.containerMenu;
                if (menu.containerId == syncId && menu instanceof MachineMenuServer machineMenu) {
                    ShapeSelection.Server shapeSelection = machineMenu.blockEntity.getComponent(GuiComponents.SHAPE_SELECTION);
                    shapeSelection.behavior.handleClick(shapeLine, clickedLeftButton ? -1 : +1);
                }
            });
        };

        public static FriendlyByteBuf encodeChangeShape(int syncId, int shapeLine, boolean clickedLeftButton) {
            var buf = PacketByteBufs.create();
            buf.writeInt(syncId);
            buf.writeVarInt(shapeLine);
            buf.writeBoolean(clickedLeftButton);
            return buf;
        }
    }
}
