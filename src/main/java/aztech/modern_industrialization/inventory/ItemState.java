package aztech.modern_industrialization.inventory;

import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;

public class ItemState {
    public ItemKey key;
    public int count;

    public ItemState(ItemKey key, int count) {
        this.key = key;
        this.count = count;
    }
}
