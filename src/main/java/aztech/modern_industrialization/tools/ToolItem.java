package aztech.modern_industrialization.tools;

import aztech.modern_industrialization.ModernIndustrialization;
import net.minecraft.item.Item;

public class ToolItem extends Item {
    public ToolItem(Settings settings) {
        super(settings.maxCount(1).group(ModernIndustrialization.ITEM_GROUP)); // TODO: recipe remainder
    }
}
