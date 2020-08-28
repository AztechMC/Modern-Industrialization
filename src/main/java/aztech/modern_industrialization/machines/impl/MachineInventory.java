package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.inventory.ConfigurableInventory;

public interface MachineInventory extends ConfigurableInventory {
    void setItemExtract(boolean extract);
    void setFluidExtract(boolean extract);
    boolean getItemExtract();
    boolean getFluidExtract();
    boolean hasOutput();
}
