package aztech.modern_industrialization.items;

import aztech.modern_industrialization.MIComponents;
import net.minecraft.world.item.ItemStack;

public interface MIEnergyItem {
    /**
     * @return The energy stored in the stack. Count is ignored.
     */
    default long getEnergy(ItemStack stack) {
        return stack.getOrDefault(MIComponents.ENERGY, 0L);
    }

    /**
     * Directly set the energy stored in the stack. Count is ignored.
     * It's up to callers to ensure that the new amount is >= 0 and <= capacity.
     */
    default void setEnergy(ItemStack stack, long newAmount) {
        if (newAmount == 0) {
            stack.remove(MIComponents.ENERGY);
        } else {
            stack.set(MIComponents.ENERGY, newAmount);
        }
    }
}
