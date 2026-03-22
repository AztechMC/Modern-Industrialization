package aztech.modern_industrialization.transfer;

import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

/**
 * Long version of {@link ResourceStack}.
 */
public record LongResourceStack<T extends Resource>(T resource, long amount) {
    /**
     * Checks if the resource stack is empty, meaning that the amount is zero
     * or that the resource is {@link Resource#isEmpty() empty}.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return amount <= 0 || resource.isEmpty();
    }

    @Override
    public String toString() {
        return amount + "x " + resource;
    }
}
