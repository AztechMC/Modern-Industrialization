package aztech.modern_industrialization.api.pipes.item;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookupRegistry;
import net.minecraft.util.Identifier;

/**
 * A speed upgrade for an item pipe
 */
@FunctionalInterface
public interface SpeedUpgrade {
    /**
     * @return By how much this increases the number of transferred items every 3 seconds.
     */
    long value();

    ItemApiLookup<SpeedUpgrade, Void> LOOKUP = ItemApiLookupRegistry.getLookup(
            new Identifier("modern_industrialization:item_pipe_speed_upgrade"), SpeedUpgrade.class, Void.class);
}
