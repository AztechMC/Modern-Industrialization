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

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;

public class MIItemStorage extends MIStorage<Item, ItemVariant, ConfigurableItemStack> {
    public final IItemHandler itemHandler = new ItemHandler();

    public MIItemStorage(List<ConfigurableItemStack> stacks) {
        super(stacks, false);
    }

    public class ItemHandler implements IItemHandler, WhitelistedItemStorage, FilledItemStorage {
        @Override
        public int getSlots() {
            return stacks.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stacks.get(slot).toStack();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack item, boolean simulate) {
            if (item.isEmpty()) {
                return ItemStack.EMPTY;
            }

            var stack = stacks.get(slot);
            if (!stack.pipesInsert) {
                return item;
            }

            // A bit messy, but lets us avoid always getting an ItemVariant which takes unnecessary time with a map lookup.
            ItemVariant resource = null;
            boolean canInsert;
            if (stack.getAmount() == 0) {
                // If the amount is 0, we check if the lock allows it.
                canInsert = stack.isResourceAllowedByLock(item.getItem());
            } else if ((item.isEmpty() || item.isComponentsPatchEmpty()) && (stack.isEmpty() || stack.getResource().getComponentsPatch().isEmpty())) {
                // If both items don't have components, we check if they are the same item.
                canInsert = item.is(stack.getResource().getItem());
            } else {
                // Otherwise we check that the resources match exactly.
                resource = ItemVariant.of(item);
                canInsert = stack.getResource().equals(resource);
            }

            if (canInsert) {
                if (resource == null) {
                    resource = ItemVariant.of(item);
                }
                long inserted = Math.min(item.getCount(), stack.getRemainingCapacityFor(resource));

                if (inserted > 0 && !simulate) {
                    stack.setKey(resource);
                    stack.increment(inserted);
                }

                return inserted == item.getCount() ? ItemStack.EMPTY : resource.toStack((int) (item.getCount() - inserted));
            }

            return item;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }
            try (var tx = Transaction.hackyOpen()) {
                var variant = stacks.get(slot).getVariant();
                if (variant.isBlank()) {
                    return ItemStack.EMPTY;
                }

                long result = stacks.get(slot).extract(variant, amount, tx);
                if (result > 0 && !simulate) {
                    tx.commit();
                }
                return result == 0 ? ItemStack.EMPTY : variant.toStack((int) result);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return (int) stacks.get(slot).getCapacity();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stacks.get(slot).isResourceAllowedByLock(stack.getItem());
        }

        @Override
        public boolean currentlyWhitelisted() {
            // Only whitelisted if everything is locked.
            for (ConfigurableItemStack stack : stacks) {
                if (stack.pipesInsert && stack.getLockedInstance() == null) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void getWhitelistedItems(Set<Item> whitelist) {
            for (ConfigurableItemStack stack : stacks) {
                if (stack.pipesInsert && !stack.isLockedTo(Items.AIR)) {
                    whitelist.add(stack.getLockedInstance());
                }
            }
        }

        @Override
        public boolean isFull() {
            for (var stack : stacks) {
                if (!stack.isLockedTo(Items.AIR) && stack.getAmount() < stack.getCapacity()) {
                    return false;
                }
            }
            return true;
        }
    }
}
