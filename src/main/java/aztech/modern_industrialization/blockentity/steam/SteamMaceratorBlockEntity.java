package aztech.modern_industrialization.blockentity.steam;

import aztech.modern_industrialization.blockentity.MachineBlockEntity;
import aztech.modern_industrialization.blockentity.factory.MachineFactory;

public class SteamMaceratorBlockEntity extends MachineBlockEntity {

    public SteamMaceratorBlockEntity() {
        super(MachineFactory.steamMaceratorFactory);
    }

    @Override
    public void tick() {

    }
}
