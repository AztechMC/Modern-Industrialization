package aztech.modern_industrialization.tools;

import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public class WrenchItem extends ToolItem implements MachineOverlayItem {
    public WrenchItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        if(block instanceof IWrenchable) {
            return ((IWrenchable) block).onWrenchUse(context);
        }
        return super.useOnBlock(context);
    }


}
