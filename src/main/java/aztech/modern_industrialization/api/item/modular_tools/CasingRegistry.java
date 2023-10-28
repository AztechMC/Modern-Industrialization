package aztech.modern_industrialization.api.item.modular_tools;

import java.util.IdentityHashMap;
import java.util.Objects;
import net.minecraft.world.item.Item;

public class CasingRegistry {
    private static final IdentityHashMap<Item, CasingProperties> casings = new IdentityHashMap<>();

    public static void register(Item item, CasingProperties properties) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(properties);
        casings.put(item, properties);
    }

    public static CasingProperties getProperties(Item item) {
        return casings.get(item);
    }

    public static record CasingProperties(ComponentTier maxComponentTier, int moduleSlots) {
    }
}
