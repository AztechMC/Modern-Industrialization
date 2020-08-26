package aztech.modern_industrialization.fluid;

import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.Direction;

/**
 * A generic fluid container block
 */
public interface FluidInventory {
    /**
     * Insert a fluid into the inventory. Returns the inserted amount.
     * @param direction The direction to insert from.
     * @param fluid The fluid to insert.
     * @param maxAmount The maximum amount of fluid to transfer.
     * @param simulate Whether to actually insert or just return the amount that would be inserted.
     * @return The inserted amount.
     */
    int insert(Direction direction, Fluid fluid, int maxAmount, boolean simulate);

    /**
     * Extract a fluid from the inventory. Returns the extracted amount.
     * @param direction The direction to extract from.
     * @param fluid The fluid to extract.
     * @param maxAmount The maximum amount of fluid to transfer.
     * @param simulate Whether to actually extract or just return the amount that would be extracted.
     * @return The extracted amount.
     */
    int extract(Direction direction, Fluid fluid, int maxAmount, boolean simulate);

    /**
     * Get extractable fluids.
     * @param direction The direction to extract from.
     * @return A list of fluids that can potentially be extracted.
     */
    Fluid[] getExtractableFluids(Direction direction);

    /**
     * Return whether a connection should be displayed in that direction, e.g. for pipe connections.
     * @param direction The direction.
     * @return True if a connection should be displayed, false otherwise.
     */
    boolean canFluidContainerConnect(Direction direction);
}
