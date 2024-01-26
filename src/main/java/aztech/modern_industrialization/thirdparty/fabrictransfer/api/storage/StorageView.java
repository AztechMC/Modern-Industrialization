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
package aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;

/**
 * A view of a single stored resource in a {@link Storage}, for use with {@link Storage#iterator}.
 *
 * @param <T> The type of the stored resource.
 */
public interface StorageView<T> {
    /**
     * Try to extract a resource from this view.
     *
     * @return The amount that was extracted.
     */
    long extract(T resource, long maxAmount, TransactionContext transaction);

    /**
     * Return {@code true} if the {@link #getResource} contained in this storage view is blank, or {@code false} otherwise.
     *
     * <p>
     * This function is mostly useful when dealing with storages of arbitrary types.
     * For transfer variant storages, this should always be equivalent to {@code getResource().isBlank()}.
     */
    boolean isResourceBlank();

    /**
     * @return The resource stored in this view. May not be blank if {@link #isResourceBlank} is {@code false}.
     */
    T getResource();

    /**
     * @return The amount of {@link #getResource} stored in this view.
     */
    long getAmount();

    /**
     * @return The total amount of {@link #getResource} that could be stored in this view,
     *         or an estimated upper bound on the number of resources that could be stored if this view has a blank resource.
     */
    long getCapacity();

    /**
     * If this is view is a delegate around another storage view, return the underlying view.
     * This can be used to check if two views refer to the same inventory "slot".
     * <b>Do not try to extract from the underlying view, or you risk bypassing some checks.</b>
     *
     * <p>
     * It is expected that two storage views with the same underlying view ({@code a.getUnderlyingView() == b.getUnderlyingView()})
     * share the same content, and mutating one should mutate the other. However, one of them may allow extraction, and the other may not.
     */
    default StorageView<T> getUnderlyingView() {
        return this;
    }
}
