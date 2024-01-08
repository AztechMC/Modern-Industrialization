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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StoragePreconditions;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleSlotStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;
import com.google.common.primitives.Ints;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

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

    class BaseHandler<T extends TransferVariant<?>> {
        protected final ItemStack stack;
        protected final ContainerItem<T> containerItem;

        public BaseHandler(ItemStack stack, ContainerItem<T> containerItem) {
            this.stack = stack;
            this.containerItem = containerItem;
        }

        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        public T getResource() {
            return containerItem.getResource(stack);
        }

        public long getAmount() {
            if (isResourceBlank()) {
                return 0;
            }
            if (!containerItem.getBehaviour().isCreative()) {
                return containerItem.getAmount(stack);
            } else {
                return Long.MAX_VALUE;
            }
        }

        protected void update(T newResource, long newAmount) {
            containerItem.setResourceNoClean(stack, newResource);
            containerItem.setAmountNoClean(stack, newAmount);

            containerItem.clean(stack);
        }
    }

    class ItemHandler extends BaseHandler<ItemVariant> implements IItemHandler {
        public ItemHandler(ItemStack stack, ContainerItem<ItemVariant> containerItem) {
            super(stack, containerItem);
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return getResource().toStack(Ints.saturatedCast(getAmount()));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return insertItem(slot, stack, simulate, false, false);
        }

        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate, boolean ignoreFilter, boolean ignoreLock) {
            if (stack.isEmpty() || slot != 0) {
                return ItemStack.EMPTY;
            }

            if (containerItem.getBehaviour().isCreative()) {
                return stack;
            }

            var resource = ItemVariant.of(stack);
            if (containerItem.getBehaviour().canInsert(resource) || ignoreFilter) {

                if ((isResourceBlank() && (ignoreLock || containerItem.isUnlocked(stack)))
                        || getResource().equals(resource)) {
                    long amount = getAmount();
                    int inserted = (int) Math.min(stack.getCount(), containerItem.getBehaviour().getCapacityForResource(resource) - amount);
                    if (inserted > 0 && !simulate) {
                        update(resource, amount + inserted);
                    }
                    return stack.copyWithCount(stack.getCount() - inserted);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int maxAmount, boolean simulate) {
            if (slot == 0 || maxAmount <= 0 || isResourceBlank()) {
                return ItemStack.EMPTY;
            }

            var resource = getResource();
            if (containerItem.getBehaviour().canExtract(resource)) {
                if (containerItem.getBehaviour().isCreative()) {
                    return resource.toStack(maxAmount);
                } else {
                    long amount = getAmount();
                    int extracted = (int) Math.min(maxAmount, amount);

                    if (extracted > 0 && !simulate) {
                        update(resource, amount - extracted);
                    }
                    return resource.toStack(extracted);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Ints.saturatedCast(containerItem.getBehaviour().getCapacityForResource(ItemVariant.blank()));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return containerItem.getBehaviour().canInsert(ItemVariant.of(stack));
        }
    }

//    class GenericItemStorage<T extends TransferVariant<?>> implements SingleSlotStorage<T> {
//
//        private final ContainerItemContext context;
//        private final ContainerItem<T> containerItem;
//
//        public GenericItemStorage(ContainerItem<T> containerItem, ContainerItemContext context) {
//            this.context = context;
//            this.containerItem = containerItem;
//        }
//
//        public static <T extends TransferVariant<?>> GenericItemStorage<T> of(ItemStack stack,
//                ContainerItem<T> containerItem) {
//            SimpleContainer virtualStackInv = new SimpleContainer(stack);
//            var virtualStackStorage = InventoryStorage.of(virtualStackInv, null);
//            ContainerItemContext stackContext = ContainerItemContext.ofSingleSlot(virtualStackStorage.getSlot(0));
//            return new GenericItemStorage<>(containerItem, stackContext);
//        }
//
//        public long insert(T resource, long maxAmount, TransactionContext transaction, boolean ignoreFilter, boolean ignoreLock) {
//            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
//
//            if (context.getItemVariant().getItem() != containerItem)
//                return 0;
//
//            if (containerItem.getBehaviour().isCreative()) {
//                return 0;
//            }
//
//            if (containerItem.getBehaviour().canInsert(resource) || ignoreFilter) {
//
//                if ((isResourceBlank() && (ignoreLock || containerItem.isUnlocked(context.getItemVariant().toStack())))
//                        || getResource().equals(resource)) {
//                    long amount = getAmount();
//                    long inserted = Math.min(maxAmount, containerItem.getBehaviour().getCapacityForResource(resource) - amount);
//                    if (inserted > 0) {
//                        if (context.exchange(getUpdatedVariant(context.getItemVariant(), resource, amount + inserted), 1, transaction) == 1) {
//                            return inserted;
//                        }
//                    }
//                }
//            }
//            return 0;
//        }
//
//        @Override
//        public long insert(T resource, long maxAmount, TransactionContext transaction) {
//            return insert(resource, maxAmount, transaction, false, false);
//        }
//
//        @Override
//        public long extract(T resource, long maxAmount, TransactionContext transaction) {
//            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
//            if (context.getItemVariant().getItem() != containerItem)
//                return 0;
//
//            if (!containerItem.getBehaviour().isCreative() && containerItem.getBehaviour().canExtract(resource)) {
//                if (resource.equals(getResource())) {
//                    long amount = getAmount();
//                    long extracted = Math.min(maxAmount, amount);
//
//                    if (extracted > 0) {
//                        if (context.exchange(getUpdatedVariant(context.getItemVariant(), resource, amount - extracted), 1, transaction) == 1) {
//                            return extracted;
//                        }
//                    }
//                    return extracted;
//                }
//            } else {
//                return maxAmount;
//            }
//            return 0;
//        }
//
//        @Override
//        public boolean isResourceBlank() {
//            return getResource().isBlank();
//        }
//
//        @Override
//        public T getResource() {
//            return containerItem.getResource(
//                    context.getItemVariant().toStack());
//        }
//
//        @Override
//        public long getAmount() {
//            if (isResourceBlank()) {
//                return 0;
//            }
//            if (!containerItem.getBehaviour().isCreative()) {
//                return containerItem.getAmount(context.getItemVariant().toStack());
//            } else {
//                return Long.MAX_VALUE;
//            }
//        }
//
//        @Override
//        public long getCapacity() {
//            return containerItem.getBehaviour().getCapacityForResource(getResource());
//        }
//
//        @Override
//        public boolean supportsExtraction() {
//            return true;
//        }
//
//        @Override
//        public boolean supportsInsertion() {
//            return !containerItem.getBehaviour().isCreative();
//        }
//
//        protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, T newResource, long newAmount) {
//            ItemStack stack = currentVariant.toStack();
//
//            containerItem.setResourceNoClean(stack, newResource);
//            containerItem.setAmountNoClean(stack, newAmount);
//
//            containerItem.clean(stack);
//
//            return ItemVariant.of(stack);
//        }
//
//    }

}
