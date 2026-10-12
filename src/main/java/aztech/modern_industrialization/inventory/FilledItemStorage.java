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

package aztech.modern_industrialization.inventory;

import net.neoforged.neoforge.items.IItemHandler;

public interface FilledItemStorage {
    static boolean isFull(IItemHandler handler) {
        // If we have a FilledItemStorage implementation, check that since it will be faster
        if (handler instanceof FilledItemStorage filled) {
            return filled.isFull();
        }
        // Check the slots manually for fullness
        // Beware that if a mod implements IItemHandler#getStackInSlot poorly (i.e. copies stacks each time it returns) this can do more harm than good
        for (int index = 0; index < handler.getSlots(); index++) {
            if (handler.getStackInSlot(index).getCount() < handler.getSlotLimit(index)) {
                return false;
            }
        }
        return true;
    }

    boolean isFull();
}
