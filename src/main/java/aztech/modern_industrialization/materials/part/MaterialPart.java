package aztech.modern_industrialization.materials.part;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface MaterialPart {
    /**
     * @return The name of this part, for example "ingot" or "dust".
     */
    String getPart();

    /**
     * @return The common tag of this material prefixed by # if available, or the id otherwise.
     */
    String getTaggedItemId();

    /**
     * @return The id of this part.
     */
    String getItemId();

    void register();

    /**
     * @throws NullPointerException if no item available.
     */
    Item getItem();
}
