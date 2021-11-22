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

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;

public interface ItemContainingItemHelper {
    long getStackCapacity();

    default boolean canDirectInsert(ItemStack stack) {
        return stack.getItem().canBeNested();
    }

    default boolean isEmpty(ItemStack stack) {
        return stack.getSubNbt("BlockEntityTag") == null;
    }

    default ItemVariant getItemVariant(ItemStack stack) {
        if (isEmpty(stack)) {
            return ItemVariant.blank();
        } else {
            return ItemVariant.fromNbt(stack.getSubNbt("BlockEntityTag").getCompound("item"));
        }
    }

    private void setItemVariant(ItemStack stack, ItemVariant item) {
        stack.getOrCreateSubNbt("BlockEntityTag").put("item", item.toNbt());
    }

    default long insert(ItemStack stackBarrel, ItemVariant inserted, long maxAmount) {
        StoragePreconditions.notBlankNotNegative(inserted, maxAmount);

        if (this.isEmpty(stackBarrel) || this.getItemVariant(stackBarrel).equals(inserted)) {
            long maxInsert = getCapacity(inserted) - getAmount(stackBarrel);
            long insertedAmount = Math.min(maxAmount, maxInsert);

            if (insertedAmount > 0) {
                setAmount(stackBarrel, getAmount(stackBarrel) + insertedAmount);
                setItemVariant(stackBarrel, inserted);
            }
            return insertedAmount;
        }
        return 0;
    }

    default long getAmount(ItemStack stack) {
        if (getItemVariant(stack).isBlank()) {
            return 0;
        }
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag == null)
            return 0;
        else
            return tag.getLong("amt");
    }

    default void setAmount(ItemStack stack, long amount) {
        Preconditions.checkArgument(amount >= 0, "Can not set a barrel item to a negative amount");

        stack.getOrCreateSubNbt("BlockEntityTag").putLong("amt", amount);
        if (amount == 0) {
            stack.removeSubNbt("BlockEntityTag");
        }
    }

    default long getCapacity(ItemVariant variant) {
        return getStackCapacity() * variant.getItem().getMaxCount();
    }

    default long getCurrentCapacity(ItemStack barrelStack) {
        return getStackCapacity() * getItemVariant(barrelStack).getItem().getMaxCount();
    }

    default boolean handleOnStackClicked(ItemStack stackBarrel, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT) {
            return false;
        } else {
            ItemStack itemStack = slot.getStack();
            if (itemStack.isEmpty() && !isEmpty(stackBarrel)) {
                long amount = Math.min(getAmount(stackBarrel), getItemVariant(stackBarrel).getItem().getMaxCount());
                ItemStack newStack = getItemVariant(stackBarrel).toStack((int) (amount));
                slot.setStack(newStack);
                setAmount(stackBarrel, getAmount(stackBarrel) - amount);
            } else if (!itemStack.isEmpty() && canDirectInsert(itemStack)) {
                itemStack.decrement((int) insert(stackBarrel, ItemVariant.of(itemStack), itemStack.getCount()));
            }
            return true;
        }
    }

    default boolean handleOnClicked(ItemStack stackBarrel, ItemStack itemStack, Slot slot, ClickType clickType, PlayerEntity player,
            StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
            if (!itemStack.isEmpty() && canDirectInsert(itemStack)) {
                itemStack.decrement((int) insert(stackBarrel, ItemVariant.of(itemStack), itemStack.getCount()));
            }
            return true;
        } else {
            return false;
        }
    }
}
