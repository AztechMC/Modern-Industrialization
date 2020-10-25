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

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.multiblock.HatchBlockEntity;
import aztech.modern_industrialization.machines.impl.multiblock.HatchType;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockShape;
import java.util.Random;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class NuclearReactorBlockEntity extends MultiblockMachineBlockEntity {

    public NuclearReactorBlockEntity(MachineFactory factory, MultiblockShape shape) {
        super(factory, shape, false);

        // Replace the existing slots by slots that prevent any pipe I/O.
        for (int i = 0; i < itemStacks.size(); ++i) {
            itemStacks.set(i, ConfigurableItemStack.standardIOSlot(false));
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
                    for (ConfigurableItemStack outputCStack : hatch.getItemStacks()) {
                        ItemStack outputStack = outputCStack.getStack();
                        if (!outputStack.isEmpty() || attempts == 1) {
                            if (outputCStack.canInsert(insertedStack)) {
                                if (outputStack.isEmpty()) {
                                    outputCStack.setStack(insertedStack);
                                    inserted = true;
                                    break attempt;
                                } else if (outputStack.getCount() + 1 <= item.getMaxCount()) {
                                    outputStack.increment(1);
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

    private int getMaxFluid(FluidKey fluidKey, boolean extraction) {
        int totalAvailable = 0;
        for (HatchBlockEntity hatch : linkedHatches.values()) {
            if (hatch.type == (extraction ? HatchType.FLUID_INPUT : HatchType.FLUID_OUTPUT)) {
                for (ConfigurableFluidStack stack : (extraction ? hatch.getFluidInputStacks() : hatch.getFluidOutputStacks())) {
                    if (stack.isFluidValid(fluidKey)) {
                        System.out.println(extraction);
                        totalAvailable += (extraction ? stack.getAmount() : stack.getRemainingSpace());
                    }
                }
            }
        }
        return totalAvailable;
    }

    public int extractFluidFromInputHatch(FluidKey fluidKey, int amount) {
        int remaining = amount;
        for (HatchBlockEntity hatch : linkedHatches.values()) {
            if (hatch.type == HatchType.FLUID_INPUT) {
                for (ConfigurableFluidStack stack : hatch.getFluidInputStacks()) {
                    if (stack.getFluid() == fluidKey) {
                        int extract = Math.min(remaining, stack.getAmount());
                        stack.decrement(extract);
                        remaining -= extract;
                    }
                }
            }
        }
        return remaining;
    }

    public int insertFluidInOutputHatch(FluidKey fluidKey, int amount) {
        int remaining = amount;
        for (int attempts = 0; attempts < 2; attempts++) {
            for (HatchBlockEntity hatch : linkedHatches.values()) {
                if (hatch.type == HatchType.FLUID_OUTPUT) {
                    for (ConfigurableFluidStack stack : hatch.getFluidOutputStacks()) {
                        if (!stack.isEmpty() || attempts == 1) {
                            if (stack.isFluidValid(fluidKey)) {
                                if (!stack.isEmpty()) {
                                    int insert = Math.min(remaining, stack.getRemainingSpace());
                                    stack.increment(insert);
                                    remaining -= insert;
                                } else if (remaining > 0) {
                                    int insert = Math.min(remaining, stack.getRemainingSpace());
                                    stack.setFluid(fluidKey);
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

    public int getMaxFluidExtraction(FluidKey fluidKey) {
        return getMaxFluid(fluidKey, true);
    }

    public int getMaxFluidInsertion(FluidKey fluidKey) {
        return getMaxFluid(fluidKey, false);
    }

    @Override
    public void tick() {
        if (world.isClient)
            return;
        this.tickCheckShape();
        for (HatchBlockEntity hatch : linkedHatches.values()) {
            if (hatch.type == HatchType.ITEM_INPUT) {
                hatch.autoExtractItems(ItemAttributes.INSERTABLE.get(this.getWorld(), this.getPos()), true);
            }
        }
        ItemStack[][] grid = new ItemStack[8][8];
        for (int i = 0; i < 64; i++) {
            ItemStack is = this.itemStacks.get(i).getStack();
            if (!is.isEmpty()) {
                grid[i % 8][i / 8] = is;
            }

        }
        if (ready) {
            NuclearReactorLogic.tick(grid, new Random(), this);
            for (ConfigurableItemStack is : this.itemStacks) {
                if (!is.getStack().isEmpty() && is.getStack().getItem() instanceof MINuclearItem) {
                    MINuclearItem item = (MINuclearItem) is.getStack().getItem();
                    if (MINuclearItem.getHeat(is.getStack()) >= item.getMaxHeat()) {
                        is.getStack().setCount(0);
                    } else if (item.getDurability() != -1 && is.getStack().getDamage() == item.getDurability()) {
                        if (tryInsertItemInOutputHatch(item.getDepleted())) {
                            is.getStack().setCount(0);
                        }
                    }
                }
            }
        }
        markDirty();
    }
}
