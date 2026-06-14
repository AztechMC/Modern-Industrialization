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

package aztech.modern_industrialization.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Otherwise closing screen handler slots with a null inventory throws an
 * exception due to copySharedSlots.
 */
public class UnsupportedOperationInventory implements Container {
    @Override
    public int getContainerSize() {
        // Don't crash so mouse wheelie can have a chance at noticing this is a peculiar
        // slot.
        return 0;
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getItem(int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChanged() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean stillValid(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearContent() {
        throw new UnsupportedOperationException();
    }
}
