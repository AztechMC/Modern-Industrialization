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
package aztech.modern_industrialization.transferapi.impl.item;

import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class InventoryWrappersImpl {
    // List<Storage<ItemKey>> has 7 values.
    // The 6 first for the various directions, and the last element for a null
    // direction.
    private static final WeakHashMap<Inventory, List<Storage<ItemKey>>> WRAPPERS = new WeakHashMap<>();

    public static Storage<ItemKey> of(Inventory inventory, @Nullable Direction direction) {
        List<Storage<ItemKey>> storages = WRAPPERS.computeIfAbsent(inventory, InventoryWrappersImpl::buildWrappers);
        return direction != null ? storages.get(direction.ordinal()) : storages.get(6);
    }

    private static List<Storage<ItemKey>> buildWrappers(Inventory inventory) {
        List<Storage<ItemKey>> result = new ArrayList<>(7); // 6 directions + null

        // wrapper around the whole inventory
        List<InventorySlotWrapper> slots = IntStream.range(0, inventory.size()).mapToObj(i -> new InventorySlotWrapper(inventory, i))
                .collect(Collectors.toList());
        Storage<ItemKey> fullWrapper = inventory instanceof PlayerInventory ? new PlayerInventoryWrapperImpl(slots, (PlayerInventory) inventory)
                : new CombinedStorage<>(slots);

        if (inventory instanceof SidedInventory) {
            // sided logic, only use the slots returned by SidedInventory#getAvailableSlots,
            // and check canInsert/canExtract
            SidedInventory sidedInventory = (SidedInventory) inventory;

            for (Direction direction : Direction.values()) {
                List<SidedInventorySlotWrapper> sideSlots = IntStream.of(sidedInventory.getAvailableSlots(direction))
                        .mapToObj(slot -> new SidedInventorySlotWrapper(slots.get(slot), sidedInventory, direction)).collect(Collectors.toList());
                result.add(new CombinedStorage<>(sideSlots));
            }
        } else {
            // unsided logic, just use the same Storage 7 times
            for (int i = 0; i < 6; ++i) { // 6 directions
                result.add(fullWrapper);
            }
        }

        result.add(fullWrapper);
        return result;
    }
}
