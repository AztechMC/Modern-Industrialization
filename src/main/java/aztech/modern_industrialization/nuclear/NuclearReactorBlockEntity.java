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
package aztech.modern_industrialization.nuclear;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.multiblock.HatchBlockEntity;
import aztech.modern_industrialization.machines.impl.multiblock.HatchType;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockShape;
import dev.technici4n.fasttransferlib.api.item.ItemKey;
import java.util.List;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class NuclearReactorBlockEntity extends MultiblockMachineBlockEntity {

    public NuclearReactorBlockEntity(MachineFactory factory, List<MultiblockShape> shapes) {
        super(factory, shapes, false);

        // Replace the existing slots by slots that prevent any pipe I/O.
        for (int i = 0; i < inventory.itemStacks.size(); ++i) {
            inventory.itemStacks.set(i, ConfigurableItemStack.standardIOSlot(false));
        }
    }

    public boolean tryInsertItemInOutputHatch(Item item) {

        if (item == null) {
            return true;
        }

        ItemStack insertedStack = new ItemStack(item, 1);
        boolean inserted = false;

        attempt: for (int attempts = 0; attempts < 2; attempts++) {
            for (HatchBlockEntity hatch : linkedHatches.values()) {
                if (hatch.type == HatchType.ITEM_OUTPUT) {
                    for (ConfigurableItemStack outputCStack : hatch.getInventory().itemStacks) {
                        ItemKey key = outputCStack.getItemKey();
                        if (!key.isEmpty() || attempts == 1) {
                            if (outputCStack.canInsert(insertedStack)) {
                                if (key.isEmpty()) {
                                    outputCStack.setItemKey(key);
                                    outputCStack.setCount(insertedStack.getCount());
                                    inserted = true;
                                    break attempt;
                                } else if (outputCStack.getCount() + 1 <= item.getMaxCount()) {
                                    outputCStack.increment(1);
                                    inserted = true;
                                    break attempt;
                                }
                            }
                        }
                    }
                }
            }
        }
        return inserted;

    }

    private int getMaxFluid(Fluid fluid, boolean extraction) {
        int totalAvailable = 0;
        for (HatchBlockEntity hatch : linkedHatches.values()) {
            if (hatch.type == (extraction ? HatchType.FLUID_INPUT : HatchType.FLUID_OUTPUT)) {
                for (ConfigurableFluidStack stack : (extraction ? hatch.getFluidInputStacks() : hatch.getFluidOutputStacks())) {
                    if (stack.isFluidValid(fluid)) {
                        System.out.println(extraction);
                        totalAvailable += (extraction ? stack.getAmount() : stack.getRemainingSpace());
                    }
                }
            }
        }
        return totalAvailable;
    }

    public int extractFluidFromInputHatch(Fluid fluid, int amount) {
        int remaining = amount;
        for (HatchBlockEntity hatch : linkedHatches.values()) {
            if (hatch.type == HatchType.FLUID_INPUT) {
                for (ConfigurableFluidStack stack : hatch.getFluidInputStacks()) {
                    if (stack.getFluid() == fluid) {
                        long extract = Math.min(remaining, stack.getAmount());
                        stack.decrement(extract);
                        remaining -= extract;
                    }
                }
            }
        }
        return remaining;
    }

    public int insertFluidInOutputHatch(Fluid fluid, int amount) {
        int remaining = amount;
        for (int attempts = 0; attempts < 2; attempts++) {
            for (HatchBlockEntity hatch : linkedHatches.values()) {
                if (hatch.type == HatchType.FLUID_OUTPUT) {
                    for (ConfigurableFluidStack stack : hatch.getFluidOutputStacks()) {
                        if (!stack.isEmpty() || attempts == 1) {
                            if (stack.isFluidValid(fluid)) {
                                if (!stack.isEmpty()) {
                                    long insert = Math.min(remaining, stack.getRemainingSpace());
                                    stack.increment(insert);
                                    remaining -= insert;
                                } else if (remaining > 0) {
                                    long insert = Math.min(remaining, stack.getRemainingSpace());
                                    stack.setFluid(fluid);
                                    stack.increment(insert);
                                    remaining -= insert;
                                }
                            }
                        }
                    }
                }
            }
        }
        return remaining;
    }

    public int getMaxFluidExtraction(Fluid fluid) {
        return getMaxFluid(fluid, true);
    }

    public int getMaxFluidInsertion(Fluid fluid) {
        return getMaxFluid(fluid, false);
    }

    @Override
    public void tick() {
        if (world.isClient)
            return;
        this.tickCheckShape();
        for (HatchBlockEntity hatch : linkedHatches.values()) {
            if (hatch.type == HatchType.ITEM_INPUT) {
                hatch.getInventory().autoExtractItems(inventory.getItemView());
            }
        }
        // FIXME
        /*
         * ItemStack[][] grid = new ItemStack[8][8]; for (int i = 0; i < 64; i++) {
         * ItemStack is = inventory.itemStacks.get(i).getStack(); if (!is.isEmpty()) {
         * grid[i % 8][i / 8] = is; }
         * 
         * } if (ready) { NuclearReactorLogic.tick(grid, new Random(), this); for
         * (ConfigurableItemStack is : inventory.itemStacks) { if
         * (!is.getItemKey().isEmpty() && is.getItemKey().getItem() instanceof
         * MINuclearItem) { MINuclearItem item = (MINuclearItem)
         * is.getItemKey().getItem(); if (MINuclearItem.getHeat(is.getStack()) >=
         * item.getMaxHeat()) { is.getStack().setCount(0); } else if
         * (item.getDurability() != -1 && is.getStack().getDamage() ==
         * item.getDurability()) { if (tryInsertItemInOutputHatch(item.getDepleted())) {
         * is.getStack().setCount(0); } } } } }
         */
        markDirty();
    }
}
