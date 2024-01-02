package aztech.modern_industrialization.pipes.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public interface IItemSink {
    /**
     * @return leftover items
     * @see IItemHandler#insertItem(int, ItemStack, boolean)
     */
    ItemStack insertItem(ItemStack stack, boolean simulate);

    record HandlerWrapper(IItemHandler handler) implements IItemSink {
        @Override
        public ItemStack insertItem(ItemStack stack, boolean simulate) {
            return ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
        }
    }

    record Combined(List<? extends IItemSink> sinks) implements IItemSink {
        @Override
        public ItemStack insertItem(ItemStack stack, boolean simulate) {
            for (IItemSink sink : sinks) {
                stack = sink.insertItem(stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
            return stack;
        }
    }
}
