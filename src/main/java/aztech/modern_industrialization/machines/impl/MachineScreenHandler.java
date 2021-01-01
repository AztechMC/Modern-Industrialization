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
package aztech.modern_industrialization.machines.impl;

import static aztech.modern_industrialization.machines.impl.MachineSlotType.*;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.inventory.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class MachineScreenHandler extends ConfigurableScreenHandler {

    public MachineInventory inventory;
    final PropertyDelegate propertyDelegate;
    private final int[] trackedProperties;
    private final MachineFactory factory;
    private final boolean[] trackedExtract = new boolean[2];

    public MachineScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, MachineInventories.clientOfBuf(buf), new ArrayPropertyDelegate(buf.readInt()),
                MachineFactory.getFactoryByID(buf.readString()));
    }

    public MachineScreenHandler(int syncId, PlayerInventory playerInventory, MachineInventory inventory, PropertyDelegate propertyDelegate,
            MachineFactory factory) {

        super(ModernIndustrialization.SCREEN_HANDLER_TYPE_MACHINE, syncId, playerInventory, inventory);

        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);
        this.factory = factory;
        this.propertyDelegate = propertyDelegate;
        this.trackedProperties = new int[propertyDelegate.size()];
        updateTrackedExtract();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9, factory.getInventoryPosX() + j * 18, factory.getInventoryPosY() + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, factory.getInventoryPosX() + j * 18, 58 + factory.getInventoryPosY()));
        }

        if (!factory.isMultiblock() || inventory.getItemStacks().size() > 0) {
            int itemCnt = 0;
            for (int i = 0; i < factory.getSlots(); i++) {
                if (factory.getSlotType(i) == INPUT_SLOT || factory.getSlotType(i) == OUTPUT_SLOT) {
                    ConfigurableItemStack stack = inventory.getItemStacks().get(itemCnt);
                    this.addSlot(stack.new ConfigurableItemSlot(inventory, itemCnt, factory.getSlotPosX(i), factory.getSlotPosY(i),
                            factory.insertPredicate));
                    ++itemCnt;
                } else {
                    ConfigurableFluidStack stack = inventory.getFluidStacks().get(i - itemCnt);
                    this.addSlot(stack.new ConfigurableFluidSlot(inventory, factory.getSlotPosX(i), factory.getSlotPosY(i)));
                }
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void close(PlayerEntity player) {
        inventory.onClose(playerInventory.player);
        super.close(player);
    }

    public MachineFactory getMachineFactory() {
        return factory;
    }

    public int getTickProgress() {
        return propertyDelegate.get(1);
    }

    public int getTickRecipe() {
        return propertyDelegate.get(2);
    }

    public boolean getIsActive() {
        return propertyDelegate.get(0) == 1;
    }

    public int getEfficiencyTicks() {
        return propertyDelegate.get(3);
    }

    public int getMaxEfficiencyTicks() {
        return propertyDelegate.get(4);
    }

    public int getStoredEu() {
        return propertyDelegate.get(5);
    }

    public int getMaxStoredEu() {
        return propertyDelegate.get(7);
    }

    public int getRecipeEu() {
        return propertyDelegate.get(6);
    }

    public int getRecipeMaxEu() {
        return propertyDelegate.get(8);
    }

    public boolean isShapeValid() {
        return propertyDelegate.get(9) == 1;
    }

    public int getSelectedShape() {
        return propertyDelegate.get(10);
    }

    public int getMaxShapes() {
        return propertyDelegate.get(11);
    }

    private void updateTrackedExtract() {
        trackedExtract[0] = inventory.getItemExtract();
        trackedExtract[1] = inventory.getFluidExtract();
    }

    @Override
    public void sendContentUpdates() {
        if (playerInventory.player instanceof ServerPlayerEntity) {
            if (trackedExtract[0] != inventory.getItemExtract() || trackedExtract[1] != inventory.getFluidExtract()) {
                updateTrackedExtract();
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeInt(syncId);
                buf.writeBoolean(inventory.getItemExtract());
                buf.writeBoolean(inventory.getFluidExtract());
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerInventory.player, MachinePackets.S2C.UPDATE_AUTO_EXTRACT, buf);
            }
            for (int i = 0; i < trackedProperties.length; ++i) {
                if (trackedProperties[i] != propertyDelegate.get(i)) {
                    trackedProperties[i] = propertyDelegate.get(i);
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(syncId);
                    buf.writeInt(i);
                    buf.writeInt(trackedProperties[i]);
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerInventory.player, MachinePackets.S2C.SYNC_PROPERTY, buf);
                }
            }
            super.sendContentUpdates();
        }
    }
}
