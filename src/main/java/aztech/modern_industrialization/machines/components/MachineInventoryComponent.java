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
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.inventory.ChangeListener;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.IComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

public class MachineInventoryComponent implements CrafterComponent.Inventory, IComponent.ServerOnly {
    public final int itemInputCount;
    public final int itemOutputCount;
    public final int fluidInputCount;
    public final int fluidOutputCount;

    public final MIInventory inventory;

    private int invHash = 0;
    private final ChangeListener listener = new ChangeListener() {
        @Override
        protected void onChange() {
            invHash++;
        }

        @Override
        protected boolean isValid(Object token) {
            return true;
        }
    };

    public MachineInventoryComponent(List<ConfigurableItemStack> itemInputs, List<ConfigurableItemStack> itemOutputs,
            List<ConfigurableFluidStack> fluidInputs, List<ConfigurableFluidStack> fluidOutputs, SlotPositions itemPositions,
            SlotPositions fluidPositions) {
        this.itemInputCount = itemInputs.size();
        this.itemOutputCount = itemOutputs.size();
        this.fluidInputCount = fluidInputs.size();
        this.fluidOutputCount = fluidOutputs.size();

        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        itemStacks.addAll(itemInputs);
        itemStacks.addAll(itemOutputs);
        List<ConfigurableFluidStack> fluidStacks = new ArrayList<>();
        fluidStacks.addAll(fluidInputs);
        fluidStacks.addAll(fluidOutputs);

        this.inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);
        this.inventory.addListener(listener, null);
    }

    @Override
    public List<ConfigurableItemStack> getItemInputs() {
        return inventory.getItemStacks().subList(0, itemInputCount);
    }

    @Override
    public List<ConfigurableItemStack> getItemOutputs() {
        return inventory.getItemStacks().subList(itemInputCount, itemInputCount + itemOutputCount);
    }

    @Override
    public List<ConfigurableFluidStack> getFluidInputs() {
        return inventory.getFluidStacks().subList(0, fluidInputCount);
    }

    @Override
    public List<ConfigurableFluidStack> getFluidOutputs() {
        return inventory.getFluidStacks().subList(fluidInputCount, fluidInputCount + fluidOutputCount);
    }

    @Override
    public int hash() {
        return invHash;
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        this.inventory.writeNbt(tag);
    }

    @Override
    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        this.inventory.readNbt(tag, isUpgradingMachine);
        this.inventory.addListener(listener, null);
    }
}
