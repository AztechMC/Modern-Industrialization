package aztech.modern_industrialization.inventory;

import com.google.common.primitives.Ints;
import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.fabricmc.fabric.api.transfer.v1.base.IntegerStorageFunction;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import java.util.function.Predicate;

/**
 * An integer insertion function that can also lock slots.
 */
@FunctionalInterface
public interface ItemInsertionFunction extends IntegerStorageFunction<ItemKey> {
    /**
     * @param filter Return false to skip some ConfigurableItemStacks.
     * @param lockSlots Whether to lock slots or not.
     */
    long apply(ItemKey key, int count, Transaction tx, Predicate<ConfigurableItemStack> filter, boolean lockSlots);

    @Override
    default long applyFixedDenominator(ItemKey key, long count, Transaction tx) {
        return apply(key, Ints.saturatedCast(count), tx, ConfigurableItemStack::canPipesInsert, false);
    }
}
