package aztech.modern_industrialization.transfer;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;

public class MIPreconditions {
    /**
     * Ensures the value is non-negative, throws otherwise.
     *
     * @throws IllegalArgumentException when value is negative.
     */
    public static void checkNonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected value to be non-negative: " + value);
        }
    }

    /**
     * Ensures the resource is non-empty and the value is non-negative, throws otherwise.
     *
     * @throws IllegalArgumentException when resource is empty or value is negative.
     */
    public static void checkNonEmptyNonNegative(Resource resource, long value) {
        TransferPreconditions.checkNonEmpty(resource);
        checkNonNegative(value);
    }
}
