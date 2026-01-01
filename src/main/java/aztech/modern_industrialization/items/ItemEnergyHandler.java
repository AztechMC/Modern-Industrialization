package aztech.modern_industrialization.items;

import com.google.common.math.LongMath;
import com.google.common.primitives.Longs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Copied from {@link ItemAccessEnergyHandler} and modified to support {@code long} energy amounts,
 * and stack-dependent capacities.
 */
public abstract class ItemEnergyHandler implements EnergyHandler {
    protected final ItemAccess itemAccess;
    protected final Item validItem;
    protected final DataComponentType<Long> energyComponent;
    protected final int maxInsert;
    protected final int maxExtract;

    public ItemEnergyHandler(ItemAccess itemAccess, DataComponentType<Long> energyComponent, int maxTransfer) {
        this(itemAccess, energyComponent, maxTransfer, maxTransfer);
    }

    public ItemEnergyHandler(ItemAccess itemAccess, DataComponentType<Long> energyComponent, int maxInsert, int maxExtract) {
        TransferPreconditions.checkNonNegative(maxInsert);
        TransferPreconditions.checkNonNegative(maxExtract);

        this.itemAccess = itemAccess;
        // Store the current item, such that if the item changes later we don't return any stored content from it.
        this.validItem = itemAccess.getResource().getItem();
        this.energyComponent = energyComponent;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    protected abstract long getCapacity(ItemResource accessResource);

    /**
     * Retrieves the amount stored in the {@linkplain ItemAccess#getResource() current contents} of the item access.
     */
    protected long getAmountFrom(ItemResource accessResource) {
        if (!accessResource.is(validItem)) {
            return 0;
        }
        return accessResource.getOrDefault(energyComponent, 0L);
    }

    /**
     * Returns a resource with updated amount.
     *
     * @param accessResource current resource, before the update
     * @param newAmount      the new amount
     * @return {@code accessResource} updated with the new amount,
     *         or {@link ItemResource#EMPTY} if the new amount cannot be stored
     * @implNote This function <strong>should not</strong> mutate the {@linkplain #itemAccess item access},
     *           that will be done by the calling code based on the results of this function.
     */
    protected ItemResource update(ItemResource accessResource, long newAmount) {
        return accessResource.with(energyComponent, newAmount);
    }

    @Override
    public long getAmountAsLong() {
        return LongMath.saturatedMultiply(itemAccess.getAmount(), getAmountFrom(itemAccess.getResource()));
    }

    @Override
    public long getCapacityAsLong() {
        var accessResource = itemAccess.getResource();
        if (!accessResource.is(validItem)) {
            return 0;
        }

        return LongMath.saturatedMultiply(itemAccess.getAmount(), getCapacity(accessResource));
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }
        int amountPerItem = Math.min(maxInsert, amount / accessAmount);
        if (amountPerItem == 0) {
            return 0;
        }

        ItemResource accessResource = itemAccess.getResource();
        if (!accessResource.is(validItem)) {
            return 0;
        }
        long currentAmountPerItem = getAmountFrom(accessResource);

        int insertedPerItem = (int) Math.min(amountPerItem, getCapacity(accessResource) - currentAmountPerItem);
        if (insertedPerItem > 0) {
            ItemResource filledResource = update(accessResource, currentAmountPerItem + insertedPerItem);

            if (!filledResource.isEmpty()) {
                return insertedPerItem * itemAccess.exchange(filledResource, accessAmount, transaction);
            }
        }

        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }
        int amountPerItem = Math.min(maxExtract, amount / accessAmount);
        if (amountPerItem == 0) {
            return 0;
        }

        ItemResource accessResource = itemAccess.getResource();
        // If the resource is not validItem this will return 0 and avoid extraction
        long currentAmountPerItem = getAmountFrom(accessResource);

        int extractedPerItem = (int) Math.min(amountPerItem, currentAmountPerItem);
        if (extractedPerItem > 0) {
            ItemResource emptiedResource = update(accessResource, currentAmountPerItem - extractedPerItem);

            if (!emptiedResource.isEmpty()) {
                return extractedPerItem * itemAccess.exchange(emptiedResource, accessAmount, transaction);
            }
        }

        return 0;
    }
}
