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

/**
 * Preconditions that can be used when working with storages.
 *
 * <p>
 * In particular, {@link #notNegative} or {@link #notBlankNotNegative} can be used by implementations of
 * {@link Storage#insert} and {@link Storage#extract} to fail-fast if the arguments are invalid.
 */
public final class StoragePreconditions {
    /**
     * Ensure that the passed transfer variant is not blank.
     *
     * @throws IllegalArgumentException If the variant is blank.
     */
    public static void notBlank(TransferVariant<?> variant) {
        if (variant.isBlank()) {
            throw new IllegalArgumentException("Transfer variant may not be blank.");
        }
    }

    /**
     * Ensure that the passed amount is not negative. That is, it must be {@code >= 0}.
     *
     * @throws IllegalArgumentException If the amount is negative.
     */
    public static void notNegative(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount may not be negative, but it is: " + amount);
        }
    }

    /**
     * Check both for a not blank transfer variant and a not negative amount.
     */
    public static void notBlankNotNegative(TransferVariant<?> variant, long amount) {
        notBlank(variant);
        notNegative(amount);
    }

    private StoragePreconditions() {
    }
}
