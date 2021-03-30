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
package aztech.modern_industrialization.mixin;

import aztech.modern_industrialization.transferapi.impl.item.ItemItemKeyCache;
import aztech.modern_industrialization.transferapi.impl.item.ItemKeyImpl;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public class ItemMixin implements ItemItemKeyCache {
    @Unique
    private volatile ItemKeyImpl fabric_cachedItemKey = null;

    @SuppressWarnings("ConstantConditions")
    @Override
    public ItemKeyImpl fabric_getOrCreateItemKey() {
        // We use a double-checked lock to lazily create and store ItemKeyImpl instances
        // while keeping thread-safety for the creation part
        // and lock-free retrieval once the cache has been initialized.
        // The volatile keyword ensures correct publication of created ItemKeyImpl
        // instances under Java 1.5+.
        if (fabric_cachedItemKey != null) {
            return fabric_cachedItemKey;
        }

        synchronized (this) {
            if (fabric_cachedItemKey == null) {
                fabric_cachedItemKey = new ItemKeyImpl((Item) (Object) this, null);
            }

            return fabric_cachedItemKey;
        }
    }
}
