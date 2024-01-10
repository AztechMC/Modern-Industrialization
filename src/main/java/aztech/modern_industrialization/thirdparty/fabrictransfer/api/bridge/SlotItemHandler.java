package aztech.modern_industrialization.thirdparty.fabrictransfer.api.bridge;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleSlotStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import com.google.common.primitives.Ints;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public record SlotItemHandler(SingleSlotStorage<ItemVariant> storage) implements IItemHandler {
    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return storage.getResource().toStack(Ints.saturatedCast(storage.getAmount()));
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }
        try (var tx = Transaction.hackyOpen()) {
            var inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            return inserted == 0 ? stack : stack.copyWithCount(stack.getCount() - (int) inserted);
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.hackyOpen()) {
            var resource = storage.getResource();
            if (resource.isBlank()) {
                return ItemStack.EMPTY;
            }
            var extracted = storage.extract(resource, amount, tx);
            if (!simulate) {
                tx.commit();
            }
            return extracted == 0 ? ItemStack.EMPTY : resource.toStack((int) extracted);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return Ints.saturatedCast(storage.getCapacity());
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }
}
