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
import alexiil.mc.lib.attributes.item.GroupedItemInv;
import alexiil.mc.lib.attributes.item.ItemStackCollections;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import com.google.common.primitives.Ints;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.ItemStack;

public class WrappedItemStorage implements GroupedItemInv {
    private final Storage<ItemKey> itemKeyStorage;

    public WrappedItemStorage(Storage<ItemKey> itemKeyStorage) {
        this.itemKeyStorage = itemKeyStorage;
    }

    @Override
    public ItemStack attemptInsertion(ItemStack itemStack, Simulation simulation) {
        if (itemStack.isEmpty())
            return itemStack;

        try (Transaction tx = TransferLbaCompat.openPossiblyNestedTransaction()) {
            int inserted = (int) itemKeyStorage.insert(ItemKey.of(itemStack), itemStack.getCount(), tx);

            if (simulation.isAction()) {
                tx.commit();
            }

            ItemStack result = itemStack.copy();
            result.decrement(inserted);
            return result;
        }
    }

    @Override
    public ItemStack attemptExtraction(ItemFilter filter, int count, Simulation simulation) {
        try (Transaction tx = Transaction.openOuter()) {
            // Find a suitable item key to extract
            ItemKey[] extractedKey = new ItemKey[] { null };
            TransferLbaCompat.OPEN_TRANSACTION.set(tx);
            for (StorageView<ItemKey> view : itemKeyStorage.iterable(tx)) {
                ItemKey key = view.getResource();
                if (!key.isEmpty() && filter.matches(key.toStack())) {
                    try (Transaction testTx = tx.openNested()) {
                        if (view.extract(key, count, testTx) > 0) {
                            extractedKey[0] = key;
                            break;
                        }
                    }
                }
            }
            TransferLbaCompat.OPEN_TRANSACTION.remove();
            if (extractedKey[0] == null)
                return ItemStack.EMPTY;
            // Extract it
            int extracted = (int) itemKeyStorage.extract(extractedKey[0], count, tx);
            if (simulation.isAction()) {
                tx.commit();
            }
            return extractedKey[0].toStack(extracted);
        }
    }

    @Override
    public String toString() {
        return "WrappedItemStorage{" + "itemKeyStorage=" + itemKeyStorage + '}';
    }

    @Override
    public Set<ItemStack> getStoredStacks() {
        Set<ItemStack> stacks = ItemStackCollections.openHashSet();
        try (Transaction tx = TransferLbaCompat.openPossiblyNestedTransaction()) {
            for (StorageView<ItemKey> view : itemKeyStorage.iterable(tx)) {
                if (!view.getResource().isEmpty()) {
                    stacks.add(view.getResource().toStack());
                }
            }
        }
        return stacks;
    }

    @Override
    public int getTotalCapacity() {
        // lol what?
        return getStatistics(is -> true).spaceTotal;
    }

    @Override
    public ItemInvStatistic getStatistics(ItemFilter filter) {
        long amount = 0;
        long capacity = 0;

        try (Transaction tx = TransferLbaCompat.openPossiblyNestedTransaction()) {
            for (StorageView<ItemKey> view : itemKeyStorage.iterable(tx)) {
                if (!view.getResource().isEmpty() && filter.matches(view.getResource().toStack())) {
                    amount += view.getAmount();
                    capacity += view.getCapacity();
                }
            }
        }

        int amountInt = Ints.saturatedCast(amount);
        int capacityInt = Ints.saturatedCast(capacity);

        return new ItemInvStatistic(filter, amountInt, capacityInt - amountInt, capacityInt);
    }
}
