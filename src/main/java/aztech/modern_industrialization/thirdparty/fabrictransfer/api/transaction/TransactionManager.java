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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.jetbrains.annotations.Nullable;

/**
 * Manager for handling opening new {@link Transaction Transactions} or querying status of a transaction chain in a given thread.
 *
 * @see Transaction
 */
final class TransactionManager {
    private static final ThreadLocal<TransactionManager> MANAGERS = ThreadLocal.withInitial(TransactionManager::new);
    final Thread thread = Thread.currentThread();

    final List<Transaction> stack = new ArrayList<>();
    int currentDepth = -1;

    final Queue<SnapshotJournal<?>> rootCommitQueue = new ArrayDeque<>();
    boolean processingRootCommitQueue = false;

    /**
     * @return The manager for the current thread.
     */
    static TransactionManager getManagerForThread() {
        return MANAGERS.get();
    }

    Transaction open(@Nullable TransactionContext parent, Class<?> callerClass) {
        if (parent != null) {
            Transaction parentImpl = (Transaction) parent;
            validateCurrentTransaction(parentImpl);
            parentImpl.validateOpen();
        } else if (currentDepth >= 0) {
            String currentRoot = getOpenTransaction(0).getDebugName();
            throw new IllegalStateException("A root transaction of `" + currentRoot + "` is already active on this thread " + thread + " when `"
                    + callerClass + "` tried to open.");
        }

        Transaction current;
        if (stack.size() == ++currentDepth) {
            current = new Transaction(this, currentDepth, callerClass);
            stack.add(current);
        } else {
            current = stack.get(currentDepth);
            current.callerClass = callerClass;
        }
        current.open = true;
        return current;
    }

    /**
     * Return the transaction with the specified depth.
     *
     * @param depth Queried depth of the transaction desired.
     * @throws IndexOutOfBoundsException If there is no open transaction with the requested depth.
     * @throws IllegalStateException     If this function is not called on the thread this transaction was opened in.
     */
    Transaction getOpenTransaction(int depth) {
        validateCurrentThread();

        if (depth < 0) {
            throw new IndexOutOfBoundsException("Depth may not be negative.");
        }

        if (depth > this.currentDepth) {
            throw new IndexOutOfBoundsException("There is no open transaction for depth `" + depth + "`");
        }

        Transaction transaction = this.stack.get(depth);
        transaction.validateOpen();
        return transaction;
    }

    void validateCurrentThread() {
        if (Thread.currentThread() != thread) {
            String errorMessage = String.format(
                    "Attempted to access transaction state from thread %s, but this transaction is only valid on thread %s.",
                    Thread.currentThread().getName(),
                    thread.getName());
            throw new IllegalStateException(errorMessage);
        }
    }

    void validateCurrentTransaction(Transaction transaction) {
        validateCurrentThread();

        if (currentDepth != -1 && stack.get(currentDepth) == transaction)
            return;

        String errorMessage = String.format(
                "Transaction function was called on a transaction (%s) with depth `%d`, " +
                        "but the current transaction (%s) has depth `%d`.",
                transaction.getDebugName(),
                transaction.depth(),
                stack.get(currentDepth).getDebugName(),
                currentDepth);
        throw new IllegalStateException(errorMessage);
    }

    @Nullable
    RuntimeException processRootCommitQueue(@Nullable RuntimeException closeException) {
        if (processingRootCommitQueue) {
            // onRootCommit callbacks can trigger more transactions, which will trigger more onRootCommit callbacks.
            // When this happens, the additional callbacks are added to the back of the queue of the transaction manager,
            // such that they are eventually processed.
            // The queue is already being processed higher up the call stack.
            // Let control resume to queue processing of the "true" root transaction.
            return closeException;
        }

        processingRootCommitQueue = true;

        // Invoke root close callbacks
        while (!rootCommitQueue.isEmpty()) {
            SnapshotJournal<?> journal = rootCommitQueue.remove();
            try {
                journal.callOnRootCommit();
            } catch (Exception exception) {
                if (closeException == null) {
                    closeException = new RuntimeException("Encountered an exception while invoking a journal's onRootCommit method.", exception);
                } else {
                    closeException.addSuppressed(exception);
                }
            }
        }

        processingRootCommitQueue = false;
        return closeException;
    }

    private TransactionManager() {}
}
