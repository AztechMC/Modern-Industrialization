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

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public interface ItemContainingItemHelper extends ContainerItem<ItemVariant> {

    default boolean handleStackedOnOther(ItemStack stackBarrel, Slot slot, ClickAction clickType, Player player) {
        if (clickType == ClickAction.SECONDARY && slot.allowModification(player)) {
            Mutable<ItemStack> ref = new MutableObject<>(slot.getItem());
            boolean result = handleClick(player, stackBarrel, ref);
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
            boolean result = handleClick(player, stackBarrel, ref);
            cursorStackReference.set(ref.getValue());
            slot.setChanged();
            return result;
        } else {
            return false;
        }
    }

    default boolean handleClick(Player player, ItemStack barrelLike, Mutable<ItemStack> otherStack) {
        if (!(barrelLike.getItem() instanceof ItemContainingItemHelper helper)) {
            throw new AssertionError("This method should only be called on a ItemContainingItemHelper.");
        }

        var barrelStorage = GenericItemStorage.of(barrelLike, this);
        // The player can ignore the lock of the barrel when inserting items.
        InsertionOnlyStorage<ItemVariant> barrelStorageIgnoreLock = (res, max, tx) -> barrelStorage.insert(res, max, tx, true, true);

        SimpleContainer otherInv = new SimpleContainer(otherStack.getValue().copy()) {
            @Override
            public void setItem(int slot, ItemStack stack) {
                // Override vanilla clamping to max stack size.
                // This fixes interactions with stacks greater than their max size,
                // for example when interacting in Dank Storage GUIs.
                this.items.set(slot, stack);
            }
        };
        var otherInvStorage = InventoryStorage.of(otherInv, null);

        if (StorageUtil.move(otherInvStorage, barrelStorageIgnoreLock, (iv) -> true, Long.MAX_VALUE, null) > 0
                || StorageUtil.move(barrelStorage, otherInvStorage, (iv) -> true, Long.MAX_VALUE, null) > 0) {
            otherStack.setValue(otherInv.getItem(0));
            return true;
        } else {
            return !isEmpty(barrelLike) || !otherStack.getValue().isEmpty();
        }
    }

    @Override
    default ItemVariant getResource(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag != null) {
            return ItemVariant.fromNbt(tag.getCompound("item"));
        } else {
            return ItemVariant.blank();
        }
    }

    @Override
    default void setResourceNoClean(ItemStack stack, ItemVariant item) {
        stack.getOrCreateTagElement("BlockEntityTag").put("item", item.toNbt());
        onChange(stack);
    }
}
