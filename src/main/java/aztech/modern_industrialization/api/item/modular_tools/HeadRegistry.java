package aztech.modern_industrialization.api.item.modular_tools;

import aztech.modern_industrialization.items.modulartools.ModularToolItem.ToolType;
import java.util.IdentityHashMap;
import java.util.Objects;
import net.minecraft.world.item.Item;

public class HeadRegistry {
    private static final IdentityHashMap<Item, HeadProperties> heads = new IdentityHashMap<>();

    public static void register(Item item, HeadProperties properties) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(properties);
        heads.put(item, properties);
    }

    public static HeadProperties getProperties(Item item) {
        return heads.get(item);
    }

    public static record HeadProperties(ComponentTier tier, ToolType toolType, int miningLevel, float miningSpeed,
            double attackDamage) {
    }
}
