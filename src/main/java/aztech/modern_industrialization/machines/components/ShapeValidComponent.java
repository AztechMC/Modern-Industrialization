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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * Syncing whether the multiblock shape is currently valid with the clients, to
 * decide if the BER should display something or not.
 */
public class ShapeValidComponent implements IComponent.ClientOnly {
    private boolean lastShapeValid = false;
    public boolean shapeValid = false;

    /**
     * Return true if this component should be synced with the client.
     */
    public boolean update() {
        if (lastShapeValid != shapeValid) {
            lastShapeValid = shapeValid;
            return true;
        }
        return false;
    }

    @Override
    public void writeClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("shapeValid", shapeValid);
    }

    @Override
    public void readClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        shapeValid = tag.getBoolean("shapeValid");
    }
}
