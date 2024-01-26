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
package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StoragePreconditions;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StorageView;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.ResourceAmount;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.base.SnapshotParticipant;
import aztech.modern_industrialization.util.Simulation;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractConfigurableStack<T, K extends TransferVariant<T>> extends SnapshotParticipant<ResourceAmount<K>>
        implements StorageView<K>, IConfigurableSlot {
    private final Map<ChangeListener, Object> listeners = new IdentityHashMap<>();
    protected K key = getBlankVariant();
    protected long amount = 0;
    protected T lockedInstance = null;
    protected boolean playerLocked = false;
    protected boolean machineLocked = false;
    protected boolean playerLockable = true;
    protected boolean playerInsert = false;
    protected boolean playerExtract = true;
    protected boolean pipesInsert = false;
    protected boolean pipesExtract = false;

    public AbstractConfigurableStack() {
    }

    public AbstractConfigurableStack(AbstractConfigurableStack<T, K> other) {
        this.key = other.key;
        this.amount = other.amount;
        this.lockedInstance = other.lockedInstance;
        this.playerLocked = other.playerLocked;
        this.machineLocked = other.machineLocked;
        this.playerLockable = other.playerLockable;
        this.playerInsert = other.playerInsert;
        this.playerExtract = other.playerExtract;
        this.pipesInsert = other.pipesInsert;
        this.pipesExtract = other.pipesExtract;
    }

    public AbstractConfigurableStack(CompoundTag tag) {
        this.key = readVariantFromNbt(tag.getCompound("key"));
        this.amount = tag.getLong("amount");
        if (tag.contains("locked")) {
            this.lockedInstance = getRegistry().get(new ResourceLocation(tag.getString("locked")));
        }
        this.machineLocked = tag.getBoolean("machineLocked");
        this.playerLocked = tag.getBoolean("playerLocked");
        this.playerLockable = tag.getBoolean("playerLockable");
        this.playerInsert = tag.getBoolean("playerInsert");
        this.playerExtract = tag.getBoolean("playerExtract");
        this.pipesInsert = tag.getBoolean("pipesInsert");
        this.pipesExtract = tag.getBoolean("pipesExtract");
    }

    protected void notifyListeners() {
        ChangeListener.notify(listeners);
    }

    public void addListener(ChangeListener listener, Object token) {
        listeners.put(listener, token);
    }

    public void removeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    protected abstract T getEmptyInstance();

    protected abstract K getBlankVariant();

    protected abstract Registry<T> getRegistry();

    protected abstract K readVariantFromNbt(CompoundTag compound);

    protected abstract long getRemainingCapacityFor(K key);

    @Override
    public SlotConfig getConfig() {
        return new SlotConfig(playerLockable, playerInsert, playerExtract, pipesInsert, pipesExtract);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbstractConfigurableStack<?, ?> that = (AbstractConfigurableStack<?, ?>) o;
        return amount == that.amount && playerLocked == that.playerLocked && machineLocked == that.machineLocked
                && playerLockable == that.playerLockable && playerInsert == that.playerInsert && playerExtract == that.playerExtract
                && pipesInsert == that.pipesInsert && pipesExtract == that.pipesExtract && key.equals(that.key)
                && lockedInstance == that.lockedInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, amount, lockedInstance, playerLocked, machineLocked, playerLockable, playerInsert, playerExtract, pipesInsert,
                pipesExtract);
    }

    public void setAmount(long amount) {
        this.amount = amount;
        if (amount == 0) {
            this.key = getBlankVariant();
        }
        notifyListeners();
    }

    public void empty() {
        this.setAmount(0);
    }

    public void increment(long amount) {
        setAmount(this.amount + amount);
    }

    public void decrement(long amount) {
        increment(-amount);
    }

    public void setKey(K key) {
        this.key = key;
        notifyListeners();
    }

    public boolean isResourceAllowedByLock(T instance) {
        return lockedInstance == null || lockedInstance == instance;
    }

    public boolean isResourceAllowedByLock(K key) {
        return isResourceAllowedByLock(key.getObject());
    }

    public boolean canPlayerInsert() {
        return playerInsert;
    }

    public boolean canPlayerExtract() {
        return playerExtract;
    }

    public boolean isPlayerLocked() {
        return playerLocked;
    }

    public boolean isMachineLocked() {
        return machineLocked;
    }

    public void enableMachineLock(T lockedInstance) {
        if (this.lockedInstance != null && lockedInstance != this.lockedInstance)
            throw new RuntimeException("Trying to override locked instance");
        machineLocked = true;
        this.lockedInstance = lockedInstance;
        notifyListeners();
    }

    public void disableMachineLock() {
        machineLocked = false;
        updatedLockedInstance();
    }

    public T getLockedInstance() {
        return lockedInstance;
    }

    public boolean isLockedTo(T otherInstance) {
        return getLockedInstance() == otherInstance;
    }

    public void togglePlayerLock() {
        if (playerLockable) {
            playerLocked = !playerLocked;
            updatedLockedInstance();
        }
    }

    public void togglePlayerLock(T cursorInstance) {
        if (playerLockable) {
            if (playerLocked && lockedInstance == getEmptyInstance() && cursorInstance != getEmptyInstance()) {
                lockedInstance = cursorInstance;
            } else {
                playerLocked = !playerLocked;
            }
            updatedLockedInstance();
        }
    }

    private void updatedLockedInstance() {
        if (!machineLocked && !playerLocked) {
            lockedInstance = null;
        } else if (lockedInstance == null) {
            lockedInstance = key.getObject();
        }
        notifyListeners();
    }

    public boolean canPlayerLock() {
        return playerLockable;
    }

    /**
     * Lock range of stacks (without overriding existing locks).
     */
    public static <T, K extends TransferVariant<T>> void playerLockNoOverride(T instance, List<? extends AbstractConfigurableStack<T, K>> stacks) {
        for (int iter = 0; iter < 2; ++iter) {
            boolean allowEmptyStacks = iter == 1;

            for (AbstractConfigurableStack<T, K> stack : stacks) {
                if (stack.lockedInstance == null || stack.lockedInstance == stack.getEmptyInstance()) {
                    if (stack.key.isOf(instance) || (stack.isResourceBlank() && allowEmptyStacks)) {
                        stack.lockedInstance = instance;
                        stack.playerLocked = true;
                        return;
                    }
                }
            }
        }
    }

    /**
     * Try locking the slot to the given instance, return true if it succeeded
     */
    public boolean playerLock(T instance, Simulation simulation) {
        if (key.isBlank() || key.getObject() == instance) {
            if (simulation.isActing()) {
                lockedInstance = instance;
                playerLocked = true;
            }
            return true;
        }
        return false;
    }

    public boolean canPipesExtract() {
        return pipesExtract;
    }

    public boolean canPipesInsert() {
        return pipesInsert;
    }

    @Override
    public long extract(K key, long maxAmount, TransactionContext transaction) {
        if (pipesExtract) {
            return extractDirect(key, maxAmount, transaction);
        } else {
            return 0;
        }
    }

    public long extractDirect(K key, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(key, maxAmount);
        if (key.equals(this.key)) {
            long extracted = Math.min(amount, maxAmount);
            updateSnapshots(transaction);
            decrement(extracted);
            return extracted;
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return key.isBlank();
    }

    public boolean isEmpty() {
        return isResourceBlank();
    }

    @Override
    public K getResource() {
        return key;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public ResourceAmount<K> createSnapshot() {
        return new ResourceAmount<>(key, amount);
    }

    @Override
    public void readSnapshot(ResourceAmount<K> ra) {
        this.amount = ra.amount();
        this.key = ra.resource();
    }

    @Override
    protected void onFinalCommit() {
        notifyListeners();
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.put("key", key.toNbt());
        tag.putLong("amount", amount);
        if (lockedInstance != null) {
            tag.putString("locked", getRegistry().getKey(lockedInstance).toString());
        }
        // TODO: more efficient encoding?
        tag.putBoolean("machineLocked", machineLocked);
        tag.putBoolean("playerLocked", playerLocked);
        tag.putBoolean("playerLockable", playerLockable);
        tag.putBoolean("playerInsert", playerInsert);
        tag.putBoolean("playerExtract", playerExtract);
        tag.putBoolean("pipesInsert", pipesInsert);
        tag.putBoolean("pipesExtract", pipesExtract);
        return tag;
    }
}
