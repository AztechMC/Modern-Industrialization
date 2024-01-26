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
package aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StoragePreconditions;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.CompoundTag;

/**
 * A storage that can store a single transfer variant at any given time.
 * Implementors should at least override {@link #getCapacity(TransferVariant)},
 * and probably {@link #onFinalCommit} as well for {@code markDirty()} and similar calls.
 *
 * <p>
 * {@link #canInsert} and {@link #canExtract} can be used for more precise control over which variants may be inserted or extracted.
 * If one of these two functions is overridden to always return false, implementors may also wish to override
 * {@link #supportsInsertion} and/or {@link #supportsExtraction}.
 *
 * @see aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.base.SingleFluidStorage SingleFluidStorage for fluid variants.
 * @see aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.base.SingleItemStorage SingleItemStorage for item variants.
 */
public abstract class SingleVariantStorage<T extends TransferVariant<?>> extends SnapshotParticipant<ResourceAmount<T>>
        implements SingleSlotStorage<T> {
    public T variant = getBlankVariant();
    public long amount = 0;

    /**
     * Return the blank variant.
     *
     * <p>
     * Note: this is called very early in the constructor.
     * If fields need to be accessed from this function, make sure to re-initialize {@link #variant} yourself.
     */
    protected abstract T getBlankVariant();

    /**
     * Return the maximum capacity of this storage for the passed transfer variant.
     * If the passed variant is blank, an estimate should be returned.
     */
    protected abstract long getCapacity(T variant);

    /**
     * @return {@code true} if the passed non-blank variant can be inserted, {@code false} otherwise.
     */
    protected boolean canInsert(T variant) {
        return true;
    }

    /**
     * @return {@code true} if the passed non-blank variant can be extracted, {@code false} otherwise.
     */
    protected boolean canExtract(T variant) {
        return true;
    }

    /**
     * Simple implementation of writing to NBT. Other formats are allowed, this is just a convenient suggestion.
     */
    // Reading from NBT is not provided because it would need to call the static FluidVariant/ItemVariant.fromNbt
    public void writeNbt(CompoundTag nbt) {
        nbt.put("variant", variant.toNbt());
        nbt.putLong("amount", amount);
    }

    @Override
    public long insert(T insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

        if ((insertedVariant.equals(variant) || variant.isBlank()) && canInsert(insertedVariant)) {
            long insertedAmount = Math.min(maxAmount, getCapacity(insertedVariant) - amount);

            if (insertedAmount > 0) {
                updateSnapshots(transaction);

                if (variant.isBlank()) {
                    variant = insertedVariant;
                    amount = insertedAmount;
                } else {
                    amount += insertedAmount;
                }

                return insertedAmount;
            }
        }

        return 0;
    }

    @Override
    public long extract(T extractedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount);

        if (extractedVariant.equals(variant) && canExtract(extractedVariant)) {
            long extractedAmount = Math.min(maxAmount, amount);

            if (extractedAmount > 0) {
                updateSnapshots(transaction);
                amount -= extractedAmount;

                if (amount == 0) {
                    variant = getBlankVariant();
                }

                return extractedAmount;
            }
        }

        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return variant.isBlank();
    }

    @Override
    public T getResource() {
        return variant;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getCapacity() {
        return getCapacity(variant);
    }

    @Override
    protected ResourceAmount<T> createSnapshot() {
        return new ResourceAmount<>(variant, amount);
    }

    @Override
    protected void readSnapshot(ResourceAmount<T> snapshot) {
        variant = snapshot.resource();
        amount = snapshot.amount();
    }

    @Override
    public String toString() {
        return "SingleVariantStorage[%d %s]".formatted(amount, variant);
    }
}
