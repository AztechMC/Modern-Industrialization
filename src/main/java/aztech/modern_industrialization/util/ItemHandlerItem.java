package aztech.modern_industrialization.util;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyHandler;
import net.neoforged.neoforge.items.wrapper.ForwardingItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class ItemHandlerItem extends ForwardingItemHandler {
    private final SlotAccess slot;

    public ItemHandlerItem(SlotAccess slot, Function<ItemStack, @Nullable IItemHandler> getter) {
        super(() -> {
            var cap = getter.apply(slot.get());
            return cap == null ? EmptyHandler.INSTANCE : cap;
        });
        this.slot = slot;
    }

    public ItemHandlerItem(SlotAccess slot) {
        this(slot, stack -> stack.getCapability(Capabilities.ItemHandler.ITEM));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (simulate || this.slot.get().getCount() <= 1) {
            return super.insertItem(slot, stack, simulate);
        } else {
            return doOnCopy(() -> super.insertItem(slot, stack, false));
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (simulate || this.slot.get().getCount() <= 1) {
            return super.extractItem(slot, amount, simulate);
        } else {
            return doOnCopy(() -> super.extractItem(slot, amount, false));
        }
    }

    private ItemStack doOnCopy(Supplier<ItemStack> runnable) {
        var slotStack = this.slot.get();
        // Copy stack
        var stackCopy = slotStack.copy();
        stackCopy.shrink(1);

        // Do insertion via super
        slotStack.setCount(1);
        var ret = runnable.get();

        // Replace stack, and stow away the new result
        var newStack = this.slot.get();
        if (ItemStack.isSameItemSameTags(stackCopy, newStack)) {
            stackCopy.grow(1);
        } else if (!newStack.isEmpty()) {
            // TODO stow
        }

        this.slot.set(stackCopy);

        return ret;
    }
}
