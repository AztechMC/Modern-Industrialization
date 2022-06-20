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

import java.util.Collections;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class IoStorage<T> implements Storage<T> {
    private final Storage<T> storage;
    private final boolean allowInsert, allowExtract;

    public IoStorage(Storage<T> storage, boolean allowInsert, boolean allowExtract) {
        this.storage = storage;
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
    }

    @Override
    public boolean supportsInsertion() {
        return allowInsert;
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        if (allowInsert) {
            return storage.insert(resource, maxAmount, transaction);
        } else {
            return 0;
        }
    }

    @Override
    public boolean supportsExtraction() {
        return allowExtract;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        if (allowExtract) {
            return storage.extract(resource, maxAmount, transaction);
        } else {
            return 0;
        }
    }

    @Override
    public Iterator<StorageView<T>> iterator() {
        if (allowExtract) {
            return storage.iterator();
        } else {
            return Collections.emptyIterator();
        }
    }
}
