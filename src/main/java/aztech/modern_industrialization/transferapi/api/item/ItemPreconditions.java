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

import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * Preconditions for item transfer.
 */
public final class ItemPreconditions {
    public static void notEmpty(ItemKey key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("ItemKey may not be empty or null.");
        }
    }

    public static void notEmpty(Item item) {
        if (item == null || item == Items.AIR) {
            throw new IllegalArgumentException("Item may not be empty or null.");
        }
    }

    public static void notEmptyNotNegative(ItemKey key, long amount) {
        ItemPreconditions.notEmpty(key);
        Preconditions.checkArgument(amount >= 0);
    }

    private ItemPreconditions() {
    }
}
