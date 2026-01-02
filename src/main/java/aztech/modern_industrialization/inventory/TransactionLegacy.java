package aztech.modern_industrialization.inventory;

import net.neoforged.neoforge.transfer.transaction.Transaction;

@Deprecated(forRemoval = true)
public class TransactionLegacy {
    public static Transaction hackyOpen() {
        return Transaction.open(Transaction.getCurrentOpenedTransaction());
    }
}
