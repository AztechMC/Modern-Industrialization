package aztech.modern_industrialization;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;

@Config(name = "modern_industrialization")
public class MIConfig implements ConfigData {
    public boolean generateOres = true;

    @ConfigEntry.Gui.Excluded
    private transient static boolean registered = false;
    public static synchronized MIConfig getConfig() {
        if (!registered) {
            AutoConfig.register(MIConfig.class, Toml4jConfigSerializer::new);
            registered = true;
        }

        return AutoConfig.getConfigHolder(MIConfig.class).getConfig();
    }
}
