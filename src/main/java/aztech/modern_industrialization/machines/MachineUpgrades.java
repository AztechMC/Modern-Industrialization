package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.api.energy.CableTier;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class MachineUpgrades {
    public static final Map<CableTier, Item> tierToHull = new HashMap<>();
    public static final Map<Item, CableTier> hullToTier = new HashMap<>();

    private static void registerHull(CableTier tier, Item hull) {
        tierToHull.put(tier, hull);
        hullToTier.put(hull, tier);
    }

    static {
        registerHull(CableTier.LV, MIBlock.BASIC_MACHINE_HULL.blockItem);
        registerHull(CableTier.MV, MIBlock.ADVANCED_MACHINE_HULL.blockItem);
        registerHull(CableTier.HV, MIBlock.TURBO_MACHINE_HULL.blockItem);
    }
}
