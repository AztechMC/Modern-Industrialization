package aztech.modern_industrialization.api.energy;

import net.minecraft.world.item.ItemStack;

// TODO PR to neo
// TODO unclear if we should account for stack size or not
class SimpleItemEnergyStorageImpl implements ILongEnergyStorage {
    private final ItemStack stack;
    private final long capacity;
    private final long maxInsert, maxExtract;

    SimpleItemEnergyStorageImpl(ItemStack stack, long capacity, long maxInsert, long maxExtract) {
        this.stack = stack;
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    @Override
    public long receive(long maxReceive, boolean simulate) {
        long count = stack.getCount();

        long maxAmountPerCount = maxReceive / count;
        long currentAmountPerCount = getAmount() / count;
        long insertedPerCount = Math.min(maxInsert, Math.min(maxAmountPerCount, capacity - currentAmountPerCount));

        if (insertedPerCount > 0) {
            if (!simulate) {
                SimpleEnergyItem.setStoredEnergyUnchecked(stack, currentAmountPerCount + insertedPerCount);
            }
            return insertedPerCount * count;
        }

        return 0;
    }

    @Override
    public long extract(long maxExtract, boolean simulate) {
        long count = stack.getCount();

        long maxAmountPerCount = maxExtract / count;
        long currentAmountPerCount = getAmount() / count;
        long extractedPerCount = Math.min(maxExtract, Math.min(maxAmountPerCount, currentAmountPerCount));

        if (extractedPerCount > 0) {
            if (!simulate) {
                SimpleEnergyItem.setStoredEnergyUnchecked(stack, currentAmountPerCount - extractedPerCount);
            }
            return extractedPerCount * count;
        }

        return 0;
    }

    @Override
    public long getAmount() {
        return stack.getCount() * SimpleEnergyItem.getStoredEnergyUnchecked(stack);
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return maxInsert > 0;
    }
}
