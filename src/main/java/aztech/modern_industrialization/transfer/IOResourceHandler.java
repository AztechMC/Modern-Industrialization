package aztech.modern_industrialization.transfer;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class IOResourceHandler<T extends Resource> extends DelegatingResourceHandler<T> {
    private final boolean allowInsert, allowExtract;

    public IOResourceHandler(ResourceHandler<T> delegate, boolean allowInsert, boolean allowExtract) {
        super(delegate);
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
    }

    @Override
    public boolean isValid(int index, T resource) {
        return allowInsert && super.isValid(index, resource);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        if (!allowInsert) {
            return 0;
        }
        return super.insert(resource, amount, transaction);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        if (!allowInsert) {
            return 0;
        }
        return super.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (!allowExtract) {
            return 0;
        }
        return super.extract(resource, amount, transaction);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        if (!allowExtract) {
            return 0;
        }
        return super.extract(index, resource, amount, transaction);
    }
}
