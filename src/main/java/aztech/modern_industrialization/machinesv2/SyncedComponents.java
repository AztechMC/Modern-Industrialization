package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class SyncedComponents {
    public static final Identifier PROGRESS_BAR = new MIIdentifier("progress_bar");

    public static final class Client {
        private static final Map<Identifier, SyncedComponent.ClientFactory> components = new HashMap<>();

        public static SyncedComponent.ClientFactory get(Identifier identifier) {
            return components.get(identifier);
        }

        public static void register(Identifier id, SyncedComponent.ClientFactory clientFactory) {
            if (components.put(id, clientFactory) != null) {
                throw new RuntimeException("Duplicate registration of component identifier.");
            }
        }

        static {
            register(PROGRESS_BAR, ProgressBar.Client::new);
        }
    }
}
