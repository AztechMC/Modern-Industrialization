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
package aztech.modern_industrialization.transferapi.api.item;

import aztech.modern_industrialization.transferapi.impl.item.InventoryWrappersImpl;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps {@link Inventory} and {@link PlayerInventory} as {@link Storage}
 * implementations.
 */
public final class InventoryWrappers {
    /**
     * Return a wrapper around an {@link Inventory} or a {@link SidedInventory}.
     *
     * <p>
     * Note: If the inventory is a {@link PlayerInventory}, this function will
     * return a wrapper around all the slots of the player inventory except the
     * cursor stack. This may cause insertion to insert arbitrary items into
     * equipment slots or other unexpected behavior. To prevent this,
     * {@link PlayerInventoryWrapper}'s specialized functions should be used
     * instead.
     *
     * @param inventory The inventory to wrap.
     * @param direction The direction to use if the access is sided, or {@code null}
     *                  if the access is not sided.
     */
    // TODO: should we throw if we receive a PlayerInventory? (it's probably a
    // mistake)
    public static Storage<ItemKey> of(Inventory inventory, @Nullable Direction direction) {
        Objects.requireNonNull(inventory, "Null inventory is not supported.");
        return InventoryWrappersImpl.of(inventory, direction);
    }

    /**
     * Return a wrapper around the inventory of a player.
     * 
     * @see PlayerInventoryWrapper
     */
    public static PlayerInventoryWrapper ofPlayer(PlayerEntity player) {
        Objects.requireNonNull(player, "Null player is not supported.");
        return ofPlayerInventory(player.inventory);
    }

    /**
     * Return a wrapper around the inventory of a player.
     * 
     * @see PlayerInventoryWrapper
     */
    public static PlayerInventoryWrapper ofPlayerInventory(PlayerInventory playerInventory) {
        return (PlayerInventoryWrapper) of(playerInventory, null);
    }

    private InventoryWrappers() {
    }
}
