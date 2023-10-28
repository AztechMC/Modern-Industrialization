package aztech.modern_industrialization.api.item.modular_tools;

import java.util.IdentityHashMap;
import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

public class ModuleRegistry {
    private static final IdentityHashMap<Item, ModuleProperties> modules = new IdentityHashMap<>();

    public static void register(Item item, ModuleProperties properties) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(properties);
        modules.put(item, properties);
    }

    public static ModuleProperties getProperties(Item item) {
        return modules.get(item);
    }

    public static record ModuleProperties(@Nullable Enchantment enchantment, @Nullable CustomModuleEffect customEffect,
            double multiplier) {
    }

    public static enum CustomModuleEffect {
        AREA
    }
}
