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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public interface ItemContainingItemHelper {
    long getStackCapacity();

    default boolean canDirectInsert(ItemStack stack) {
        return stack.getItem().canFitInsideContainerItems();
    }

    default boolean isEmpty(ItemStack stack) {
        return stack.getTagElement("BlockEntityTag") == null;
    }

    default ItemVariant getItemVariant(ItemStack stack) {
        if (isEmpty(stack)) {
            return ItemVariant.blank();
        } else {
            return ItemVariant.fromNbt(stack.getTagElement("BlockEntityTag").getCompound("item"));
        }
    }

    private void setItemVariant(ItemStack stack, ItemVariant item) {
        stack.getOrCreateTagElement("BlockEntityTag").put("item", item.toNbt());
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
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag == null)
            return 0;
        else
            return tag.getLong("amt");
    }

    default void setAmount(ItemStack stack, long amount) {
        Preconditions.checkArgument(amount >= 0, "Can not set a barrel item to a negative amount");

        stack.getOrCreateTagElement("BlockEntityTag").putLong("amt", amount);
        if (amount == 0) {
            stack.removeTagKey("BlockEntityTag");
        }
    }

    default long getCapacity(ItemVariant variant) {
        return getStackCapacity() * variant.getItem().getMaxStackSize();
    }

    default long getCurrentCapacity(ItemStack barrelStack) {
        return getStackCapacity() * getItemVariant(barrelStack).getItem().getMaxStackSize();
    }

    default boolean handleStackedOnOther(ItemStack stackBarrel, Slot slot, ClickAction clickType, Player player) {
        if (clickType == ClickAction.SECONDARY && slot.allowModification(player)) {
            Mutable<ItemStack> ref = new MutableObject<>(slot.getItem());
            boolean result = handleClick(stackBarrel, ref);
            slot.set(ref.getValue());
            return result;
        } else {
            return false;
        }
    }

    default boolean handleOtherStackedOnMe(ItemStack stackBarrel, ItemStack itemStack, Slot slot, ClickAction clickType, Player player,
            SlotAccess cursorStackReference) {
        if (clickType == ClickAction.SECONDARY && slot.allowModification(player)) {
            Mutable<ItemStack> ref = new MutableObject<>(itemStack);
            boolean result = handleClick(stackBarrel, ref);
            cursorStackReference.set(ref.getValue());
            slot.setChanged();
            return result;
        } else {
            return false;
        }
    }

    private boolean handleClick(ItemStack stackBarrel, Mutable<ItemStack> otherStack) {
        ItemStack other = otherStack.getValue().copy();
        // Try to put into barrel
        if ((isEmpty(stackBarrel) || getItemVariant(stackBarrel).matches(other)) && !other.isEmpty() && canDirectInsert(other)) {
            other.shrink((int) insert(stackBarrel, ItemVariant.of(other), other.getCount()));
            otherStack.setValue(other);
            return true;
        } else if (!isEmpty(stackBarrel) && other.isEmpty()) {
            int amount = (int) Math.min(getAmount(stackBarrel), getItemVariant(stackBarrel).getItem().getMaxStackSize());
            ItemStack newStack = getItemVariant(stackBarrel).toStack(amount);
            otherStack.setValue(newStack);
            setAmount(stackBarrel, getAmount(stackBarrel) - amount);
            return true;
        } else {
            return !isEmpty(stackBarrel) || !other.isEmpty();
        }
    }
}
