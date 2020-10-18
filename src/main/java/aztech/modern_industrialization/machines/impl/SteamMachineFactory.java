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

import aztech.modern_industrialization.machines.recipe.MachineRecipeType;

public class SteamMachineFactory extends MachineFactory {

    private int steamBucketCapacity;

    public SteamMachineFactory(String ID, MachineTier tier, BlockEntityFactory factory, MachineRecipeType type, int inputSlots, int outputSlots,
            int liquidInputSlots, int liquidOutputSlots) {
        super(ID, tier, factory, type, inputSlots, outputSlots, liquidInputSlots + 1, liquidOutputSlots);
    }

    public SteamMachineFactory(String ID, MachineTier tier, BlockEntityFactory factory, MachineRecipeType type, int inputSlots, int outputSlots) {
        super(ID, tier, factory, type, inputSlots, outputSlots, 1, 0);
    }

    public SteamMachineFactory setSteamSlotPos(int posX, int posY) {
        setSlotPos(this.getInputSlots(), posX, posY);
        return this;
    }

    @Override
    public MachineFactory setInputLiquidSlotPosition(int x, int y, int column, int row) {
        if (row * column != this.getLiquidInputSlots() - 1) { // one slot is reserved for steam input
            throw new IllegalArgumentException(
                    "Row x Column : " + row + " and " + column + " must be que equal to liquidInputSlots : " + (this.getLiquidInputSlots() - 1));
        } else {
            setInputSlotPositionWithDelta(x, y, column, row, this.getInputSlots() + 1);
        }
        return this;
    }

    public int getSteamBucketCapacity() {
        return steamBucketCapacity;
    }

    public SteamMachineFactory setSteamBucketCapacity(int steamBucketCapacity) {
        this.steamBucketCapacity = steamBucketCapacity;
        return this;
    }
}
