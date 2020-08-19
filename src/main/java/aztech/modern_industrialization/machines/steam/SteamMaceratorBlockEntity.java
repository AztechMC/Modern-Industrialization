package aztech.modern_industrialization.machines.steam;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.factory.MachineBlockEntity;
import aztech.modern_industrialization.machines.factory.MachineFactory;

public class SteamMaceratorBlockEntity extends MachineBlockEntity {

    public SteamMaceratorBlockEntity() {
        super(MachineFactory.steamMaceratorFactory, ModernIndustrialization.RECIPE_TYPE_MACERATOR);
    }

    @Override
    public void tick() {
        super.tick();
    }
}
