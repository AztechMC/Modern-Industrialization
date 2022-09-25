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
package aztech.modern_industrialization.blocks.storage;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public abstract class AbstractStorageBlockItem<T extends TransferVariant<?>> extends BlockItem {

    private final StorageBehaviour<T> behaviour;

    public AbstractStorageBlockItem(Block block, Properties properties, StorageBehaviour<T> behaviour) {
        super(block, properties);
        assert block instanceof AbstractStorageBlock;
        this.behaviour = block.be;
    }

    public abstract T getBlankResource();

    public abstract T getResource(ItemStack stack);

    public abstract void setResource(ItemStack stack, T resource);

    public long getAmount(ItemStack stack) {
        if (getResource(stack).isBlank()) {
            return 0;
        }
        if (behaviour.isCreative()) {
            return Long.MAX_VALUE;
        } else {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag == null)
                return 0;
            else
                return tag.getLong("amt");
        }
    }

    private void setAmount(ItemStack stack, long amount) {
        if (!behaviour.isCreative()) {
            stack.getOrCreateTagElement("BlockEntityTag").putLong("amt", amount);
            clean(stack);
        }
    }

    public boolean isUnlocked(ItemStack stack) {
        if (behaviour.isLockable()) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag == null)
                return true;
            else
                return !tag.getBoolean("locked");
        } else {
            return true;
        }
    }

    public void setLocked(ItemStack stack, boolean locked) {
        if (behaviour.isLockable()) {
            stack.getOrCreateTagElement("BlockEntityTag").putBoolean("locked", locked);
            clean(stack);
        }
    }

    public void clean(ItemStack stack) {
        if (isUnlocked(stack) && (getResource(stack).isBlank() || getAmount(stack) == 0)) {
            stack.removeTagKey("BlockEntityTag");
        }
    }

    public boolean isEmpty(ItemStack stack) {
        if (stack.getTagElement("BlockEntityTag") == null) {
            return true;
        } else
            return getAmount(stack) == 0;
    }

    class GenericItemStorage implements SingleSlotStorage<T> {

        private final ItemStack stack;
        private final ContainerItemContext context;

        public GenericItemStorage(ItemStack stack, ContainerItemContext context) {
            this.stack = stack;
            this.context = context;
        }

        @Override
        public long insert(T resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            if (!context.getItemVariant().isOf(stack.getItem()))
                return 0;

            if (behaviour.isCreative()) {
                return 0;
            }

            if ((isResourceBlank() && isUnlocked(stack)) || getResource().equals(resource)) {
                long amount = getAmount();
                long inserted = Math.min(maxAmount, behaviour.getCapacityForResource(resource) - amount);
                if (inserted > 0) {
                    if (context.exchange(getUpdatedVariant(context.getItemVariant(), resource, amount + inserted), 1, transaction) == 1) {
                        return inserted;
                    }
                }
            }
            return 0;
        }

        @Override
        public long extract(T resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (!context.getItemVariant().isOf(stack.getItem()))
                return 0;

            if (behaviour.isCreative()) {
                return 0;
            } else {
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
                return 0;
            }

        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public T getResource() {
            return AbstractStorageBlockItem.this.getResource(stack);
        }

        @Override
        public long getAmount() {
            if (isResourceBlank()) {
                return 0;
            }
            if (!behaviour.isCreative()) {
                return AbstractStorageBlockItem.this.getAmount(stack);
            } else {
                return Long.MAX_VALUE;
            }
        }

        @Override
        public long getCapacity() {
            return behaviour.getCapacityForResource(getResource());
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        public boolean supportsInsertion() {
            return !behaviour.isCreative();
        }

        protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, T newResource, long newAmount) {
            // TODO: Note that any enchantment or custom name is nuked, fix this?
            ItemStack stack = new ItemStack(currentVariant.getItem());
            if (!newResource.isBlank() && newAmount > 0) {
                setResource(stack, newResource);
                setAmount(stack, newAmount);
                clean(stack);
            }
            return ItemVariant.of(stack);
        }
    }
}
