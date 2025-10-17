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

package aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction;

/**
 * A subset of a {@link Transaction} that lets journals properly record, manage their state,
 * or open inner transactions, but does not allow them to close the transaction they are passed.
 * <p>
 * Recording changes to a transaction can be done with an implementation of {@link SnapshotJournal} and calling
 * {@link SnapshotJournal#updateSnapshots(TransactionContext) updateSnapshots(TransactionContext)} before having the state change.
 *
 * @see SnapshotJournal#updateSnapshots(TransactionContext)
 */
public sealed interface TransactionContext permits Transaction {
    /**
     * Gets the current depth of the transaction.
     *
     * @return The depth of this transaction: 0 if it is the root and has no parent; 1 or more otherwise indicating how far away from the root the
     *         transaction is.
     * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
     */
    int depth();
}
