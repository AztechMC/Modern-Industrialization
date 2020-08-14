package aztech.modern_industrialization.fluid.gui;

import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * A slot linked to a `FluidStackItem` stack.
 */
public class FluidSlot extends Slot {
    public FluidSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        /*if(!(inventory.getStack(index).getItem() instanceof FluidStackItem)) {
            throw new IllegalArgumentException("FluidSlot must be linked to an FluidStackItem stack!");
        }*/
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    public boolean canInsertFluid(Fluid fluid) {
        return true;
    }

    public boolean canExtractFluid(Fluid fluid) {
        return true;
    }
}
