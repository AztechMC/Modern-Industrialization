package aztech.modern_industrialization.transfer;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public class MITransferUtil {
    /**
     * @see ResourceHandlerUtil#move
     */
    public static <T extends Resource> int move(
            @Nullable ResourceHandler<T> from,
            @Nullable InsertionHandler<T> to,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0) return 0;

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int totalMoved = 0;
            int size = from.size();

            for (int index = 0; index < size; ++index) {
                T fromResource = from.getResource(index);
                if (fromResource.isEmpty() || !filter.test(fromResource)) continue;

                // check how much can be extracted
                int maxExtracted;
                try (Transaction simulatedExtract = Transaction.open(subTransaction)) {
                    maxExtracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtract);
                }

                if (maxExtracted == 0) continue;

                try (Transaction transferTransaction = Transaction.open(subTransaction)) {
                    // check how much can be inserted
                    int inserted = to.insert(fromResource, maxExtracted, transferTransaction);

                    // extract it, or rollback if we cannot actually extract the amount we inserted
                    // this can happen even for a well-behaving handler if it only supports extracting the exact
                    // amount we previously simulated, but the destination only accepted less.
                    if (inserted != from.extract(index, fromResource, inserted, transferTransaction))
                        continue;

                    totalMoved += inserted;
                    transferTransaction.commit();

                    //if we have the amount we are targeting exit the for-loop
                    if (totalMoved >= amount) break;
                }

            }

            subTransaction.commit();
            return totalMoved;
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving resources between resource handlers");
            //noinspection DataFlowIssue
            report.addCategory("Move details")
                    .setDetail("Input", from::toString)
                    .setDetail("Output", to::toString)
                    .setDetail("Filter", filter::toString)
                    .setDetail("Amount", amount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }
}
