package aztech.modern_industrialization.util;

import net.minecraft.item.ItemStack;

public class ItemStackHelper {
    public static boolean areEqualIgnoreCount(ItemStack s1, ItemStack s2) {
        return ItemStack.areItemsEqual(s1, s2) && ItemStack.areTagsEqual(s1, s2);
    }
}
