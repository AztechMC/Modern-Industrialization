package aztech.modern_industrialization.api.item.modular_tools;

import aztech.modern_industrialization.items.modulartools.ModularToolItem.EnergyType;
import java.util.IdentityHashMap;
import java.util.Objects;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class EnergyStorageRegistry {
    private static final IdentityHashMap<Item, StorageProperties> storages = new IdentityHashMap<>();

    public static void register(Item item, StorageProperties properties) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(properties);
        storages.put(item, properties);
    }

    public static StorageProperties getProperties(Item item) {
        return storages.get(item);
    }

    public static record StorageProperties(@Nullable ComponentTier tier, long capacity, EnergyType energyType) {
    }
}
