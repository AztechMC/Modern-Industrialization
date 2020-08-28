package aztech.modern_industrialization.fluid;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

/**
 * A generic fluid container item.
 */
public interface FluidContainerItem {
    /**
     * Insert some fluid into a slot.
     *
     * @param fluid       Which fluid to insert.
     * @param maxAmount   The maximum amount of fluid that can be inserted.
     * @param stackUpdate Function to update the item stack after the insertion. First call replaces the stack, subsequent calls add a new stack to the inventory.
     * @return Amount of fluid that was or would be inserted.
     */
    int insertFluid(ItemStack stack, Fluid fluid, int maxAmount, Consumer<ItemStack> stackUpdate);

    /**
     * Extract some fluid from a slot.
     *
     * @param fluid       Which fluid to extract.
     * @param maxAmount   The maximum amount of fluid that can be extracted.
     * @param stackUpdate Function to update the item stack after the insertion. First call replaces the stack, subsequent calls add a new stack to the inventory.
     * @return Amount of fluid that was or would be extracted.
     */
    int extractFluid(ItemStack stack, Fluid fluid, int maxAmount, Consumer<ItemStack> stackUpdate);

    /**
     * Get a fluid that can be extracted.
     *
     * @return A fluid that can be extracted, or null if no fluid can be extracted.
     */
    Fluid getExtractableFluid(ItemStack stack);

    /**
     * Get a consumer for player interactions in the inventory.
     */
    static Consumer<ItemStack> cursorPlayerConsumer(PlayerEntity player) {
        boolean[] firstInvoke = new boolean[] { true };
        return stack -> {
            if(firstInvoke[0]) {
                firstInvoke[0] = false;
                player.inventory.setCursorStack(stack);
            } else {
                player.inventory.offerOrDrop(player.world, stack);
            }
        };
    }


    /**
     * Get a consumer for player interactions in the world.
     */
    static Consumer<ItemStack> handPlayerConsumer(PlayerEntity player) {
        boolean[] firstInvoke = new boolean[] { true };
        return stack -> {
            if(firstInvoke[0]) {
                firstInvoke[0] = false;
                player.inventory.main.set(player.inventory.selectedSlot, stack);
            } else {
                player.inventory.offerOrDrop(player.world, stack);
            }
        };
    }
}
