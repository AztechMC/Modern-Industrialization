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

import aztech.modern_industrialization.transfer.MIPreconditions;
import net.neoforged.neoforge.transfer.resource.DataComponentHolderResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import aztech.modern_industrialization.util.Simulation;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class AbstractConfigurableStack<T, K extends DataComponentHolderResource<T>> extends SnapshotJournal<ResourceStack<K>>
        implements ConfigurableSlot {
    private final Map<ChangeListener, Object> listeners = new IdentityHashMap<>();
    protected K key = getBlankVariant();
    protected int amount = 0;
    @Nullable
    protected T lockedInstance = null;
    protected boolean playerLocked = false;
    protected boolean machineLocked = false;
    protected boolean playerLockable = true;
    protected boolean playerInsert = false;
    protected boolean playerExtract = true;
    protected boolean pipesInsert = false;
    protected boolean pipesExtract = false;

    public AbstractConfigurableStack() {}

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

    protected AbstractConfigurableStack(K key, int amount, @Nullable T lockedInstance, boolean playerLocked, boolean machineLocked, boolean playerLockable, boolean playerInsert, boolean playerExtract, boolean pipesInsert, boolean pipesExtract) {
        this.key = key;
        this.amount = amount;
        this.lockedInstance = lockedInstance;
        this.playerLocked = playerLocked;
        this.machineLocked = machineLocked;
        this.playerLockable = playerLockable;
        this.playerInsert = playerInsert;
        this.playerExtract = playerExtract;
        this.pipesInsert = pipesInsert;
        this.pipesExtract = pipesExtract;
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

    protected abstract int getRemainingCapacityFor(K key);

    public abstract int getTotalCapacityFor(T instance);

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

    public void setAmount(int amount) {
        this.amount = amount;
        if (amount == 0) {
            this.key = getBlankVariant();
        }
        notifyListeners();
    }

    public void empty() {
        this.setAmount(0);
    }

    public void increment(int amount) {
        setAmount(this.amount + amount);
    }

    public void decrement(int amount) {
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
        return isResourceAllowedByLock(key.value());
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
            throw new RuntimeException("Trying to override locked instance from %s to %s".formatted(this.lockedInstance, lockedInstance));
        machineLocked = true;
        this.lockedInstance = lockedInstance;
        notifyListeners();
    }

    public void disableMachineLock() {
        machineLocked = false;
        updatedLockedInstance();
    }

    @Nullable
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
            lockedInstance = key.value();
        }
        notifyListeners();
    }

    public boolean canPlayerLock() {
        return playerLockable;
    }

    /**
     * Lock range of stacks (without overriding existing locks).
     */
    public static <T, K extends DataComponentHolderResource<T>> void playerLockNoOverride(T instance, int requiredAmount,
            List<? extends AbstractConfigurableStack<T, K>> stacks) {
        for (int iter = 0; iter < 2; ++iter) {
            boolean allowEmptyStacks = iter == 1;

            for (AbstractConfigurableStack<T, K> stack : stacks) {
                if (stack.lockedInstance == null || stack.lockedInstance == stack.getEmptyInstance()) {
                    if (stack.key.is(instance) || (stack.isEmpty() && allowEmptyStacks)) {
                        var capacity = stack.getTotalCapacityFor(instance);
                        if (capacity <= 0) {
                            continue;
                        }
                        stack.lockedInstance = instance;
                        stack.playerLocked = true;
                        stack.notifyListeners();
                        requiredAmount -= capacity;
                        if (requiredAmount <= 0) {
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Try locking the slot to the given instance, return true if it succeeded
     */
    public boolean playerLock(T instance, Simulation simulation) {
        if (key.isEmpty() || key.is(instance)) {
            if (simulation.isActing()) {
                lockedInstance = instance;
                playerLocked = true;
                notifyListeners();
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

    public boolean isEmpty() {
        return key.isEmpty();
    }

    public K getResource() {
        return key;
    }

    public int getAmount() {
        return amount;
    }

    public abstract int getCapacity();

    @Override
    public ResourceStack<K> createSnapshot() {
        return new ResourceStack<>(key, amount);
    }

    @Override
    public void revertToSnapshot(ResourceStack<K> ra) {
        this.amount = ra.amount();
        this.key = ra.resource();
    }

    @Override
    protected void onRootCommit(ResourceStack<K> originalState) {
        notifyListeners();
    }
}
