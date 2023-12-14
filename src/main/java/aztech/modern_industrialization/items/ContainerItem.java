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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Interface use for Tanks, Barrels, SteamMiningDrill and other items that can contain a resource.
 */
public interface ContainerItem<T extends TransferVariant<?>> {

    T getResource(ItemStack stack);

    void setResourceNoClean(ItemStack stack, T resource);

    default void setResource(ItemStack stack, T resource) {
        setResourceNoClean(stack, resource);
        clean(stack);
    }

    default boolean isUnlocked(ItemStack stack) {
        if (getBehaviour().isLockable()) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag == null)
                return true;
            else
                return !tag.getBoolean("locked");
        } else {
            return true;
        }
    }

    default void setLockedNoClean(ItemStack stack, boolean locked) {
        if (getBehaviour().isLockable()) {
            stack.getOrCreateTagElement("BlockEntityTag").putBoolean("locked", locked);
        }
    }

    default void setLocked(ItemStack stack, boolean locked) {
        setLockedNoClean(stack, locked);
        clean(stack);
    }

    default long getAmount(ItemStack stack) {
        if (getResource(stack).isBlank()) {
            return 0;
        }
        if (getBehaviour().isCreative()) {
            return Long.MAX_VALUE;
        } else {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag == null)
                return 0;
            else
                return tag.getLong("amt");
        }
    }

    default void setAmountNoClean(ItemStack stack, long amount) {
        if (!getBehaviour().isCreative()) {
            stack.getOrCreateTagElement("BlockEntityTag").putLong("amt", amount);
            onChange(stack);
        }
    }

    default void setAmount(ItemStack stack, long amount) {
        setAmountNoClean(stack, amount);
        clean(stack);
    }

    default void clean(ItemStack stack) {
        if (isUnlocked(stack) && (getResource(stack).isBlank() || getAmount(stack) == 0)) {
            stack.removeTagKey("BlockEntityTag");
        }
    }

    default boolean isEmpty(ItemStack stack) {
        if (stack.getTagElement("BlockEntityTag") == null) {
            return true;
        } else
            return getAmount(stack) == 0;
    }

    default void onChange(ItemStack stack) {
    }

    StorageBehaviour<T> getBehaviour();

    class GenericItemStorage<T extends TransferVariant<?>> implements SingleSlotStorage<T> {

        private final ContainerItemContext context;
        private final ContainerItem<T> containerItem;

        public GenericItemStorage(ContainerItem<T> containerItem, ContainerItemContext context) {
            this.context = context;
            this.containerItem = containerItem;
        }

        public static <T extends TransferVariant<?>> GenericItemStorage<T> of(ItemStack stack,
                ContainerItem<T> containerItem) {
            SimpleContainer virtualStackInv = new SimpleContainer(stack);
            var virtualStackStorage = InventoryStorage.of(virtualStackInv, null);
            ContainerItemContext stackContext = ContainerItemContext.ofSingleSlot(virtualStackStorage.getSlot(0));
            return new GenericItemStorage<>(containerItem, stackContext);
        }

        public long insert(T resource, long maxAmount, TransactionContext transaction, boolean ignoreFilter, boolean ignoreLock) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            if (context.getItemVariant().getItem() != containerItem)
                return 0;

            if (containerItem.getBehaviour().isCreative()) {
                return 0;
            }

            if (containerItem.getBehaviour().canInsert(resource) || ignoreFilter) {

                if ((isResourceBlank() && (ignoreLock || containerItem.isUnlocked(context.getItemVariant().toStack())))
                        || getResource().equals(resource)) {
                    long amount = getAmount();
                    long inserted = Math.min(maxAmount, containerItem.getBehaviour().getCapacityForResource(resource) - amount);
                    if (inserted > 0) {
                        if (context.exchange(getUpdatedVariant(context.getItemVariant(), resource, amount + inserted), 1, transaction) == 1) {
                            return inserted;
                        }
                    }
                }
            }
            return 0;
        }

        @Override
        public long insert(T resource, long maxAmount, TransactionContext transaction) {
            return insert(resource, maxAmount, transaction, false, false);
        }

        @Override
        public long extract(T resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (context.getItemVariant().getItem() != containerItem)
                return 0;

            if (!containerItem.getBehaviour().isCreative() && containerItem.getBehaviour().canExtract(resource)) {
                if (resource.equals(getResource())) {
                    long amount = getAmount();
                    long extracted = Math.min(maxAmount, amount);

                    if (extracted > 0) {
                        if (context.exchange(getUpdatedVariant(context.getItemVariant(), resource, amount - extracted), 1, transaction) == 1) {
                            return extracted;
                        }
                    }
                    return extracted;
                }
            } else {
                return maxAmount;
            }
            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public T getResource() {
            return containerItem.getResource(
                    context.getItemVariant().toStack());
        }

        @Override
        public long getAmount() {
            if (isResourceBlank()) {
                return 0;
            }
            if (!containerItem.getBehaviour().isCreative()) {
                return containerItem.getAmount(context.getItemVariant().toStack());
            } else {
                return Long.MAX_VALUE;
            }
        }

        @Override
        public long getCapacity() {
            return containerItem.getBehaviour().getCapacityForResource(getResource());
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        public boolean supportsInsertion() {
            return !containerItem.getBehaviour().isCreative();
        }

        protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, T newResource, long newAmount) {
            ItemStack stack = currentVariant.toStack();

            containerItem.setResourceNoClean(stack, newResource);
            containerItem.setAmountNoClean(stack, newAmount);

            containerItem.clean(stack);

            return ItemVariant.of(stack);
        }

    }

}
