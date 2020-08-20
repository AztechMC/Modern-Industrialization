package aztech.modern_industrialization.machines.steam;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;

public class SteamPumpBlockEntity extends MachineBlockEntity {
    public SteamPumpBlockEntity() {
        super(MachineFactory.steamPumpFactory, ModernIndustrialization.RECIPE_TYPE_MACERATOR);
    }

    @Override
    public void tick() {

    }
}
