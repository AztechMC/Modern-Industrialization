package aztech.modern_industrialization.transfer;

import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@FunctionalInterface
public interface InsertionHandler<T extends Resource> {
    int insert(T resource, int amount, TransactionContext transaction);
}
