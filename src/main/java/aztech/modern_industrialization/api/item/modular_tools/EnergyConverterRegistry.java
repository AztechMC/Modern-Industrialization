package aztech.modern_industrialization.api.item.modular_tools;

import aztech.modern_industrialization.items.modulartools.ModularToolItem.EnergyType;
import java.util.IdentityHashMap;
import java.util.Objects;
import net.minecraft.world.item.Item;

public class EnergyConverterRegistry {
    private static final IdentityHashMap<Item, ConverterProperties> converters = new IdentityHashMap<>();

    public static void register(Item item, ConverterProperties properties) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(properties);
        converters.put(item, properties);
    }

    public static ConverterProperties getProperties(Item item) {
        return converters.get(item);
    }

    public static record ConverterProperties(ComponentTier tier, long maxEu, EnergyType energyType) {
    }
}
