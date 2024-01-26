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
package aztech.modern_industrialization.thirdparty.fabrictransfer.impl.transaction;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

public class TransactionManagerImpl {
    public static final ThreadLocal<TransactionManagerImpl> MANAGERS = ThreadLocal.withInitial(TransactionManagerImpl::new);

    private final Thread thread = Thread.currentThread();
    private final ArrayList<TransactionImpl> stack = new ArrayList<>();
    private final ArrayList<Transaction.OuterCloseCallback> outerCloseCallbacks = new ArrayList<>();
    private int currentDepth = -1;

    public boolean isOpen() {
        return currentDepth > -1;
    }

    public Transaction openOuter() {
        if (isOpen()) {
            throw new IllegalStateException("An outer transaction is already active on this thread.");
        }

        return open();
    }

    @Nullable
    public TransactionContext getCurrentUnsafe() {
        if (currentDepth == -1) {
            return null;
        } else if (stack.get(currentDepth).lifecycle == Transaction.Lifecycle.OPEN) {
            return stack.get(currentDepth);
        } else {
            throw new IllegalStateException("May not call getCurrentUnsafe() from a close callback.");
        }
    }

    /**
     * Open a new transaction, outer or nested, without performing any state check.
     */
    Transaction open() {
        currentDepth++;

        if (stack.size() == currentDepth) {
            stack.add(new TransactionImpl(currentDepth));
        }

        TransactionImpl current = stack.get(currentDepth);
        current.lifecycle = Transaction.Lifecycle.OPEN;
        return current;
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

    public Transaction.Lifecycle getLifecycle() {
        if (currentDepth == -1) {
            return Transaction.Lifecycle.NONE;
        } else {
            return stack.get(currentDepth).lifecycle;
        }
    }

    private class TransactionImpl implements Transaction {
        final int nestingDepth;
        final ArrayList<CloseCallback> closeCallbacks = new ArrayList<>();
        Lifecycle lifecycle = Lifecycle.NONE;

        TransactionImpl(int nestingDepth) {
            this.nestingDepth = nestingDepth;
        }

        void validateCurrentTransaction() {
            validateCurrentThread();

            if (currentDepth == -1 || stack.get(currentDepth) != this) {
                String errorMessage = String.format(
                        "Transaction function was called on a transaction with depth %d, but the current transaction has depth %d.",
                        nestingDepth,
                        currentDepth);
                throw new IllegalStateException(errorMessage);
            }
        }

        // Validate that this transaction is open.
        private void validateOpen() {
            if (lifecycle != Lifecycle.OPEN) {
                throw new IllegalStateException("Transaction operation cannot be applied to a closed transaction.");
            }
        }

        @Override
        public Transaction openNested() {
            validateCurrentTransaction();
            validateOpen();
            return open();
        }

        private void close(Result result) {
            validateCurrentTransaction();
            validateOpen();
            // Block transaction operations
            lifecycle = Lifecycle.CLOSING;

            // Note: it is important that we don't let exceptions corrupt the global state of the transaction manager.
            // That is why any callback has to run inside a try block.
            RuntimeException closeException = null;

            // Invoke callbacks in reverse order
            for (int i = closeCallbacks.size() - 1; i >= 0; i--) {
                try {
                    closeCallbacks.get(i).onClose(this, result);
                } catch (Exception exception) {
                    if (closeException == null) {
                        closeException = new RuntimeException("Encountered an exception while invoking a transaction close callback.", exception);
                    } else {
                        closeException.addSuppressed(exception);
                    }
                }
            }

            closeCallbacks.clear();

            if (currentDepth == 0) {
                lifecycle = Lifecycle.OUTER_CLOSING;

                // Invoke outer close callbacks in reverse order
                for (int i = outerCloseCallbacks.size() - 1; i >= 0; i--) {
                    try {
                        outerCloseCallbacks.get(i).afterOuterClose(result);
                    } catch (Exception exception) {
                        if (closeException == null) {
                            closeException = new RuntimeException("Encountered an exception while invoking a transaction outer close callback.",
                                    exception);
                        } else {
                            closeException.addSuppressed(exception);
                        }
                    }
                }

                outerCloseCallbacks.clear();
            }

            // Only this check will allow openOuter operations.
            currentDepth--;
            lifecycle = Lifecycle.NONE;

            // Throw exception if necessary
            if (closeException != null) {
                throw closeException;
            }
        }

        @Override
        public void abort() {
            close(Result.ABORTED);
        }

        @Override
        public void commit() {
            close(Result.COMMITTED);
        }

        @Override
        public void close() {
            if (isOpen() && lifecycle == Lifecycle.OPEN) { // check that a transaction is open on this thread and that this transaction is open.
                abort();
            }
        }

        @Override
        public int nestingDepth() {
            validateCurrentThread();
            return nestingDepth;
        }

        @Override
        public Transaction getOpenTransaction(int nestingDepth) {
            validateCurrentThread();

            if (nestingDepth < 0) {
                throw new IndexOutOfBoundsException("Nesting depth may not be negative.");
            }

            if (nestingDepth > currentDepth) {
                throw new IndexOutOfBoundsException("There is no open transaction for nesting depth " + nestingDepth);
            }

            TransactionImpl transaction = stack.get(nestingDepth);
            transaction.validateOpen();
            return transaction;
        }

        @Override
        public void addCloseCallback(CloseCallback closeCallback) {
            validateCurrentThread();
            validateOpen();
            closeCallbacks.add(closeCallback);
        }

        @Override
        public void addOuterCloseCallback(OuterCloseCallback outerCloseCallback) {
            validateCurrentThread();
            // Note: we don't call validateOpen() because this transaction may not be open if this is called during a CloseCallback.
            // We rely on a currentDepth check instead, as the depth is only set to -1 at the very end of close(Result).

            if (currentDepth == -1) {
                throw new IllegalStateException("There is no open transaction on this thread.");
            }

            outerCloseCallbacks.add(outerCloseCallback);
        }

        @Override
        public String toString() {
            return "Transaction[depth=%d, lifecycle=%s, thread=%s]".formatted(nestingDepth, lifecycle.name(), thread.getName());
        }
    }
}
