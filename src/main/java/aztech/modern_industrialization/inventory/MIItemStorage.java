package aztech.modern_industrialization.inventory;

import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.fabricmc.fabric.api.transfer.v1.base.CombinedStorageFunction;
import net.fabricmc.fabric.api.transfer.v1.item.ItemPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageFunction;

import java.util.List;
import java.util.stream.Collectors;

public class MIItemStorage implements Storage<ItemKey> {
    private final List<ConfigurableItemStack> stacks;
    private final ItemInsertionFunction insertionFunction;
    private final StorageFunction<ItemKey> extractionFunction;

    public MIItemStorage(List<ConfigurableItemStack> stacks) {
        this.stacks = stacks;
        this.extractionFunction = new CombinedStorageFunction<>(stacks.stream().map(ConfigurableItemStack::extractionFunction).collect(Collectors.toList()));
        this.insertionFunction = (key, count, tx, filter, lockSlots) -> {
            ItemPreconditions.notEmptyNotNegative(key, count);
            int totalInsert = 0;
            for (int iter = 0; iter < 2; ++iter) {
                boolean insertIntoEmptySlots = iter == 1;
                for (ConfigurableItemStack stack : stacks) {
                    if (filter.test(stack) && stack.isValid(key.getItem())) {
                        if ((stack.getCount() == 0 && insertIntoEmptySlots) || stack.getItemKey().equals(key)) {
                            int inserted = Math.min(count, Math.min(key.getItem().getMaxCount(), 64) - stack.getCount());

                            if (inserted > 0) {
                                totalInsert += inserted;
                                count -= inserted;
                                tx.enlist(stack);
                                stack.decrement(inserted);

                                if (lockSlots) {
                                    stack.enableMachineLock(key.getItem());
                                }
                            }
                        }
                    }
                }
            }
            return totalInsert;
        };
    }

    @Override
    public ItemInsertionFunction insertionFunction() {
        return insertionFunction;
    }

    @Override
    public StorageFunction<ItemKey> extractionFunction() {
        return extractionFunction;
    }

    @Override
    public boolean forEach(Visitor<ItemKey> visitor) {
        for (ConfigurableItemStack stack : stacks) {
            if (stack.getCount() > 0) {
                if (visitor.visit(stack)) {
                    return true;
                }
            }
        }

        return false;
    }
}
