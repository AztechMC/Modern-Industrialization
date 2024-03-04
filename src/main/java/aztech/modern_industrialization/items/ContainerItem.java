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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import com.google.common.primitives.Ints;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;

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
                return stack;
            }

            if (containerItem.getBehaviour().isCreative()) {
                return stack;
            }

            var resource = ItemVariant.of(stack);
            if (containerItem.getBehaviour().canInsert(resource) || ignoreFilter) {

                if ((isResourceBlank() && (ignoreLock || containerItem.isUnlocked(this.stack)))
                        || getResource().equals(resource)) {
                    long amount = getAmount();
                    int inserted = (int) Math.min(stack.getCount(), containerItem.getBehaviour().getCapacityForResource(resource) - amount);
                    if (inserted > 0 && !simulate) {
                        update(resource, amount + inserted);
                    }
                    return stack.copyWithCount(stack.getCount() - inserted);
                }
            }
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int maxAmount, boolean simulate) {
            if (slot != 0 || maxAmount <= 0 || isResourceBlank()) {
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

    class FluidHandler extends BaseHandler<FluidVariant> implements IFluidHandlerItem {
        public FluidHandler(ItemStack stack, ContainerItem<FluidVariant> containerItem) {
            super(stack, containerItem);
        }

        @Override
        public ItemStack getContainer() {
            return stack;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int slot) {
            return getResource().toStack(Ints.saturatedCast(getAmount()));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return fill(resource, action, false, false);
        }

        public int fill(FluidStack stack, FluidAction action, boolean ignoreFilter, boolean ignoreLock) {
            if (stack.isEmpty()) {
                return 0;
            }

            if (containerItem.getBehaviour().isCreative()) {
                return 0;
            }

            var resource = FluidVariant.of(stack);
            if (containerItem.getBehaviour().canInsert(resource) || ignoreFilter) {

                if ((isResourceBlank() && (ignoreLock || containerItem.isUnlocked(this.stack)))
                        || getResource().equals(resource)) {
                    long amount = getAmount();
                    int inserted = (int) Math.min(stack.getAmount(), containerItem.getBehaviour().getCapacityForResource(resource) - amount);
                    if (inserted > 0 && action.execute()) {
                        update(resource, amount + inserted);
                    }
                    return inserted;
                }
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || isResourceBlank() || !resource.isFluidEqual(getResource().toStack(1))) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxAmount, FluidAction action) {
            if (maxAmount <= 0 || isResourceBlank()) {
                return FluidStack.EMPTY;
            }

            var resource = getResource();
            if (containerItem.getBehaviour().canExtract(resource)) {
                if (containerItem.getBehaviour().isCreative()) {
                    return resource.toStack(maxAmount);
                } else {
                    long amount = getAmount();
                    int extracted = (int) Math.min(maxAmount, amount);

                    if (extracted > 0 && action.execute()) {
                        update(resource, amount - extracted);
                    }
                    return resource.toStack(extracted);
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int slot) {
            return Ints.saturatedCast(containerItem.getBehaviour().getCapacityForResource(FluidVariant.blank()));
        }

        @Override
        public boolean isFluidValid(int slot, FluidStack stack) {
            return containerItem.getBehaviour().canInsert(FluidVariant.of(stack));
        }
    }
}
