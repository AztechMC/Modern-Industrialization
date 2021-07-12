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
package aztech.modern_industrialization.transferapi.api.context;

import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import aztech.modern_industrialization.transferapi.impl.context.PlayerEntityContainerItemContext;
import aztech.modern_industrialization.transferapi.impl.context.StorageContainerItemContext;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * A context for interaction with item-provided apis, bound to a specific
 * ItemKey that must match that provided to {@link ItemApiLookup#get}.
 *
 * <p>
 * In many cases such as bucket filling/emptying, it is necessary to add stacks
 * other than the current stack. For example, filling a bottle that is in a
 * stack requires putting the water bottle in the inventory.
 */
public interface ContainerItemContext {
    /**
     * Get the current count. If the ItemKey is not present anymore, return 0
     * instead.
     */
    long getCount(TransactionContext transaction);

    /**
     * Transform some of the bound items into another item key.
     * 
     * @param count How much to transform, must be positive.
     * @param into  The target item key. If empty, delete the items instead.
     * @return How many items were successfully transformed.
     * @throws RuntimeException If there aren't enough items to replace, that is if
     *                          {@link ContainerItemContext#getCount
     *                          this.getCount()} < count.
     */
    // TODO: consider using an enum instead of a boolean? what about
    // TransactionResult?
    boolean transform(long count, ItemKey into, TransactionContext transaction);

    static ContainerItemContext ofPlayerHand(PlayerEntity player, Hand hand) {
        return PlayerEntityContainerItemContext.ofHand(player, hand);
    }

    static ContainerItemContext ofPlayerCursor(PlayerEntity player) {
        return PlayerEntityContainerItemContext.ofCursor(player);
    }

    static ContainerItemContext ofStorage(ItemKey boundKey, Storage<ItemKey> storage) {
        return new StorageContainerItemContext(boundKey, storage);
    }
}
