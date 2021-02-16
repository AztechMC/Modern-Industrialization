package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machinesv2.components.sync.AutoExtract;
import aztech.modern_industrialization.machinesv2.components.sync.EnergyBar;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.components.sync.RecipeEfficiencyBar;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class SyncedComponents {
    public static final Identifier AUTO_EXTRACT = new MIIdentifier("auto_extract");
    public static final Identifier ENERGY_BAR = new MIIdentifier("energy_bar");
    public static final Identifier PROGRESS_BAR = new MIIdentifier("progress_bar");
    public static final Identifier RECIPE_EFFICIENCY_BAR = new MIIdentifier("recipe_efficiency_bar");

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
            register(AUTO_EXTRACT, AutoExtract.Client::new);
            register(ENERGY_BAR, EnergyBar.Client::new);
            register(PROGRESS_BAR, ProgressBar.Client::new);
            register(RECIPE_EFFICIENCY_BAR, RecipeEfficiencyBar.Client::new);
        }
    }
}
