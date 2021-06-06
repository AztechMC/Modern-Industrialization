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
package aztech.modern_industrialization.transferapi.impl.compat;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.GroupedItemInv;
import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;

/**
 * Hacky wrapper, only to get AE2 compat.
 */
public class FixedGroupedItemInv implements FixedItemInv {
    private final GroupedItemInv groupedInv;
    private final List<ItemKey> items = new ArrayList<>();

    public FixedGroupedItemInv(GroupedItemInv groupedInv) {
        this.groupedInv = groupedInv;

        for (ItemStack stack : groupedInv.getStoredStacks()) {
            items.add(ItemKey.of(stack));
        }
    }

    @Override
    public int getSlotCount() {
        return Math.max(1, items.size());
    }

    @Override
    public ItemStack getInvStack(int slot) {
        if (slot >= items.size()) {
            return ItemStack.EMPTY;
        } else {
            ItemKey key = items.get(slot);
            return key.toStack(groupedInv.getAmount(key.toStack()));
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public boolean setInvStack(int slot, ItemStack to, Simulation simulation) {
        return false;
    }

    @Override
    public GroupedItemInv getGroupedInv() {
        return groupedInv;
    }
}
