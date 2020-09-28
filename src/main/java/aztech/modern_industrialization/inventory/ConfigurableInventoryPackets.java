package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.MIIdentifier;
import net.minecraft.util.Identifier;

public class ConfigurableInventoryPackets {
    public static final Identifier UPDATE_ITEM_SLOT = new MIIdentifier("update_item_slot");
    public static final Identifier UPDATE_FLUID_SLOT = new MIIdentifier("update_fluid_slot");
    public static final Identifier SET_LOCKING_MODE = new MIIdentifier("set_locking_mode");
}
