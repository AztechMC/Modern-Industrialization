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
package aztech.modern_industrialization.inventory;

import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class MIItemStorage extends MIStorage<Item, ItemVariant, ConfigurableItemStack> implements WhitelistedItemStorage {
    public MIItemStorage(List<ConfigurableItemStack> stacks) {
        super(stacks, false);
    }

    @Override
    public boolean currentlyWhitelisted() {
        // Only whitelisted if nothing is locked.
        for (ConfigurableItemStack stack : stacks) {
            if (stack.pipesInsert && stack.getLockedInstance() == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void getWhitelistedItems(Set<Item> whitelist) {
        for (ConfigurableItemStack stack : stacks) {
            if (stack.pipesInsert && stack.getLockedInstance() != Items.AIR) {
                whitelist.add(stack.getLockedInstance());
            }
        }
    }
}
