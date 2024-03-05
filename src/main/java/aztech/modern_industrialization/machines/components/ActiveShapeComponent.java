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

import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class ActiveShapeComponent implements IComponent {
    public final ShapeTemplate[] shapeTemplates;
    private int activeShape = 0;

    public ActiveShapeComponent(ShapeTemplate[] shapeTemplates) {
        this.shapeTemplates = shapeTemplates;
    }

    public void incrementShape(MultiblockMachineBlockEntity machine, int delta) {
        setShape(machine, Mth.clamp(activeShape + delta, 0, shapeTemplates.length - 1));
    }

    public void setShape(MultiblockMachineBlockEntity machine, int newShape) {
        if (newShape != activeShape) {
            activeShape = newShape;
            machine.setChanged();
            machine.unlink();
            machine.sync(false);
        }
    }

    public ShapeTemplate getActiveShape() {
        return shapeTemplates[activeShape];
    }

    public int getActiveShapeIndex() {
        return activeShape;
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putInt("activeShape", activeShape);
    }

    @Override
    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        activeShape = tag.getInt("activeShape");
    }
}
