package aztech.modern_industrialization.machines.steam;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.factory.MachineBlockEntity;
import aztech.modern_industrialization.machines.factory.MachineFactory;

public class SteamFurnaceBlockEntity extends MachineBlockEntity {

    public SteamFurnaceBlockEntity() {
        super(MachineFactory.steamFurnaceFactory, ModernIndustrialization.RECIPE_TYPE_MACERATOR);
    }

    @Override
    public void tick() {

    }
}
