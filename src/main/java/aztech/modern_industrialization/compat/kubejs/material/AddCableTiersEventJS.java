package aztech.modern_industrialization.compat.kubejs.material;

import aztech.modern_industrialization.api.energy.CableTier;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.network.chat.Component;

/**
 * Event class for registering new cable tiers.
 */
public final class AddCableTiersEventJS extends EventJS {
    // useful primarily for setting EU based off of a previous tier's value.
    @Info("""
        Gets a previously registered cable tier by name.
        """)
    public CableTier get(String name) {
        return CableTier.getTier(name);
    }

    @SuppressWarnings("unused") // shh, intellij
    @Info("""
        Adds a new tier to the list of registered cable tiers.
        """)
    public CableTier addTier(String englishName, String name, long eu) {
        Component key = Component.translatable("cable_tier.modern_industrialization." + name);
        CableTier tier = new CableTier(englishName, name, eu, key);
        CableTier.addTier(tier);
        return tier;
    }
}
