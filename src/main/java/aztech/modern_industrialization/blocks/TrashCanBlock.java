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
package aztech.modern_industrialization.blocks;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TrashCanBlock extends Block {
    public TrashCanBlock(Properties properties) {
        super(properties);

    }

    @SuppressWarnings("unchecked")
    public static <T> Storage<T> trashStorage() {
        return TRASH;
    }

    @SuppressWarnings("rawtypes")
    private static final Storage TRASH = new TrashStorage();

    @SuppressWarnings("rawtypes")
    private static class TrashStorage implements InsertionOnlyStorage {
        @Override
        public long insert(Object o, long maxAmount, TransactionContext transaction) {
            Preconditions.checkArgument(maxAmount >= 0);
            return maxAmount;
        }

        @Override
        public Iterator<StorageView> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public long getVersion() {
            return 0;
        }
    }

    public static void onRegister(Block block, Item blockItem) {
        ItemStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> TrashCanBlock.trashStorage(), block);
        FluidStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> TrashCanBlock.trashStorage(), block);
        FluidStorage.ITEM.registerForItems((key, ctx) -> TrashCanBlock.trashStorage(), blockItem);
    }
}
