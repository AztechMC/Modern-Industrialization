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
import alexiil.mc.lib.attributes.item.ItemTransferable;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.ItemStack;

public class WrappedItemStorage implements ItemTransferable {
    private final Storage<ItemKey> itemKeyStorage;

    public WrappedItemStorage(Storage<ItemKey> itemKeyStorage) {
        this.itemKeyStorage = itemKeyStorage;
    }

    @Override
    public ItemStack attemptInsertion(ItemStack itemStack, Simulation simulation) {
        if (itemStack.isEmpty())
            return itemStack;

        try (Transaction tx = Transaction.openOuter()) {
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
            itemKeyStorage.forEach(view -> {
                ItemKey key = view.resource();
                if (filter.matches(key.toStack())) {
                    try (Transaction testTx = tx.openNested()) {
                        if (view.extract(key, count, testTx) > 0) {
                            extractedKey[0] = key;
                            return true;
                        }
                    }
                }
                return false;
            }, tx);
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
}
