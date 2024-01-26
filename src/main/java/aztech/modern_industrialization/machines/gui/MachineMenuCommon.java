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
package aztech.modern_industrialization.machines.gui;

import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.ConfigurableScreenHandler;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotGroup;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public abstract class MachineMenuCommon extends ConfigurableScreenHandler implements GuiComponent.MenuFacade {
    public final MachineGuiParameters guiParams;

    MachineMenuCommon(int syncId, Inventory playerInventory, MIInventory inventory, MachineGuiParameters guiParams,
            List<? extends GuiComponent.Common> guiComponents) {
        super(MIRegistries.MACHINE_MENU.get(), syncId, playerInventory, inventory);
        this.guiParams = guiParams;

        // Player inventory slots
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9, guiParams.playerInventoryX + j * 18, guiParams.playerInventoryY + i * 18));
            }
        }
        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, guiParams.playerInventoryX + j * 18, guiParams.playerInventoryY + 58));
        }

        // Gui components first (we want to prioritize them with shift click)
        for (var component : guiComponents) {
            component.setupMenu(this);
        }

        // Configurable slots
        for (int i = 0; i < inventory.getItemStacks().size(); ++i) {
            ConfigurableItemStack stack = inventory.getItemStacks().get(i);
            // FIXME: markDirty and insert predicate
            this.addSlot(stack.new ConfigurableItemSlot(() -> {
            }, inventory.itemPositions.getX(i), inventory.itemPositions.getY(i), s -> true), SlotGroup.CONFIGURABLE_STACKS);
        }
        for (int i = 0; i < inventory.getFluidStacks().size(); ++i) {
            ConfigurableFluidStack stack = inventory.getFluidStacks().get(i);
            // FIXME: markDirty
            this.addSlot(stack.new ConfigurableFluidSlot(() -> {
            }, inventory.fluidPositions.getX(i), inventory.fluidPositions.getY(i)), SlotGroup.CONFIGURABLE_STACKS);
        }
    }

    @Override
    public void addSlotToMenu(Slot slot, SlotGroup slotGroup) {
        addSlot(slot, slotGroup);
    }

    @Override
    public MachineGuiParameters getGuiParams() {
        return guiParams;
    }

    public abstract void readClientComponentSyncData(int componentIndex, FriendlyByteBuf buf);
}
