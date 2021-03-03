package aztech.modern_industrialization.machinesv2.models;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class MachineCasing {
    public final String name;
    @Environment(EnvType.CLIENT)
    MachineCasingModel mcm;

    MachineCasing(String name) {
        this.name = name;
    }
}
