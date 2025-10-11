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

package aztech.modern_industrialization.thirdparty.fabrictransfer.impl.item;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemVariantImpl implements ItemVariant {
    private static final Map<Item, ItemVariant> noTagCache = new ConcurrentHashMap<>();

    public static ItemVariant of(Item item) {
        Objects.requireNonNull(item, "Item may not be null.");

        return noTagCache.computeIfAbsent(item, i -> new ItemVariantImpl(new ItemStack(i)));
    }

    public static ItemVariant of(ItemStack stack) {
        Objects.requireNonNull(stack);

        // Only tag-less or empty item variants are cached for now.
        if (stack.isComponentsPatchEmpty() || stack.isEmpty()) {
            return of(stack.getItem());
        } else {
            return new ItemVariantImpl(stack);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-transfer-api-v1/item");

    private final ItemStack stack;
    private final int hashCode;

    private ItemVariantImpl(ItemStack stack) {
        this.stack = stack.copyWithCount(1); // defensive copy
        hashCode = ItemStack.hashItemAndComponents(stack);
    }

    @Override
    public Item getObject() {
        return stack.getItem();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return this.stack.getComponentsPatch();
    }

    @Override
    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(this.stack, stack);
    }

    @Override
    public boolean test(Predicate<ItemStack> predicate) {
        return predicate.test(this.stack);
    }

    @Override
    public ItemStack toStack(int count) {
        return this.stack.copyWithCount(count);
    }

    @Override
    public int getMaxStackSize() {
        return this.stack.getMaxStackSize();
    }

    @Override
    public boolean isBlank() {
        return this.stack.isEmpty();
    }

    @Override
    public String toString() {
        return "ItemVariant{stack=" + stack + '}';
    }

    @Override
    public boolean equals(Object o) {
        // succeed fast with == check
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ItemVariantImpl itemVariant = (ItemVariantImpl) o;
        // fail fast with hash code
        return hashCode == itemVariant.hashCode && matches(itemVariant.stack);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
