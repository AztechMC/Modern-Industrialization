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
package aztech.modern_industrialization.api.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Simple battery-like energy containing item. If this is implemented on an item:
 * <ul>
 * <li>The energy will directly be stored in the NBT.</li>
 * <li>Helper functions in this class to work with the stored energy can be used.</li>
 * <li>Use {@link #createStorage(ItemStack, long, long, long)} to register a storage implementation.</li>
 * </ul>
 */
// TODO: Consider adding a tooltip and a recipe input -> output energy transfer handler like RC has.
public interface SimpleEnergyItem {
    String ENERGY_KEY = "energy";

    /**
     * Return a base energy storage implementation for items, with fixed capacity, and per-operation insertion and extraction limits.
     * This is used internally for items that implement SimpleEnergyItem, but it may also be used outside of that.
     * The energy is stored in the {@code energy} tag of the stacks, the same as the constant {@link #ENERGY_KEY}.
     *
     * <p>
     * Stackable energy containers are supported just fine, and they will distribute energy evenly.
     * For example, insertion of 3 units of energy into a stack of 2 items using this class will either insert 0 or 2 depending on the remaining
     * capacity.
     */
    static ILongEnergyStorage createStorage(ItemStack stack, long capacity, long maxInsert, long maxExtract) {
        return new SimpleItemEnergyStorageImpl(stack, capacity, maxInsert, maxExtract);
    }

    static <T extends Item & SimpleEnergyItem> void registerStorage(RegisterCapabilitiesEvent event, T item) {
        event.registerItem(ILongEnergyStorage.ITEM, (stack, context) -> createStorage(stack, item.getEnergyCapacity(stack),
                item.getEnergyMaxInput(stack), item.getEnergyMaxOutput(stack)), item);
    }

    /**
     * @param stack Current stack.
     * @return The max energy that can be stored in this item stack (ignoring current stack size).
     */
    long getEnergyCapacity(ItemStack stack);

    /**
     * @param stack Current stack.
     * @return The max amount of energy that can be inserted in this item stack (ignoring current stack size) in a single operation.
     */
    long getEnergyMaxInput(ItemStack stack);

    /**
     * @param stack Current stack.
     * @return The max amount of energy that can be extracted from this item stack (ignoring current stack size) in a single operation.
     */
    long getEnergyMaxOutput(ItemStack stack);

    /**
     * @return The energy stored in the stack. Count is ignored.
     */
    default long getStoredEnergy(ItemStack stack) {
        return getStoredEnergyUnchecked(stack);
    }

    /**
     * Directly set the energy stored in the stack. Count is ignored.
     * It's up to callers to ensure that the new amount is >= 0 and <= capacity.
     */
    default void setStoredEnergy(ItemStack stack, long newAmount) {
        setStoredEnergyUnchecked(stack, newAmount);
    }

    /**
     * Try to use exactly {@code amount} energy if there is enough available and return true if successful,
     * otherwise do nothing and return false.
     * 
     * @throws IllegalArgumentException If the count of the stack is not exactly 1!
     */
    default boolean tryUseEnergy(ItemStack stack, long amount) {
        if (stack.getCount() != 1) {
            throw new IllegalArgumentException("Invalid count: " + stack.getCount());
        }

        long newAmount = getStoredEnergy(stack) - amount;

        if (newAmount < 0) {
            return false;
        } else {
            setStoredEnergy(stack, newAmount);
            return true;
        }
    }

    /**
     * @return The currently stored energy, ignoring the count and without checking the current item.
     */
    static long getStoredEnergyUnchecked(ItemStack stack) {
        return getStoredEnergyUnchecked(stack.getTag());
    }

    /**
     * @return The currently stored energy of this raw tag.
     */
    static long getStoredEnergyUnchecked(@Nullable CompoundTag nbt) {
        return nbt != null ? nbt.getLong(ENERGY_KEY) : 0;
    }

    /**
     * Set the energy, ignoring the count and without checking the current item.
     */
    static void setStoredEnergyUnchecked(ItemStack stack, long newAmount) {
        if (newAmount == 0) {
            // Make sure newly crafted energy containers stack with emptied ones.
            stack.removeTagKey(ENERGY_KEY);
        } else {
            stack.getOrCreateTag().putLong(ENERGY_KEY, newAmount);
        }
    }
}
