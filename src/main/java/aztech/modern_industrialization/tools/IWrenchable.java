package aztech.modern_industrialization.tools;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

/**
 * A generic wrenchable block.
 */
public interface IWrenchable {
    /**
     * Called when the wrench is used on the block.
     */
    ActionResult onWrenchUse(ItemUsageContext context);
}
