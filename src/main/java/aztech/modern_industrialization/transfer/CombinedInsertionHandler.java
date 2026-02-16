package aztech.modern_industrialization.transfer;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class CombinedInsertionHandler<T extends Resource, H extends InsertionHandler<T>> implements InsertionHandler<T> {
    public final List<H> handlers;

    public CombinedInsertionHandler(List<H> handlers) {
        this.handlers = handlers;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int inserted = 0;
        for (var handler : handlers) {
            inserted += handler.insert(resource, amount - inserted, transaction);
            if (inserted == amount) break;
        }
        return inserted;
    }
}
