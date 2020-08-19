package aztech.modern_industrialization.blockentity.steam;

import aztech.modern_industrialization.blockentity.MachineBlockEntity;
import aztech.modern_industrialization.blockentity.factory.MachineFactory;

public class SteamPumpBlockEntity extends MachineBlockEntity {
    public SteamPumpBlockEntity() {
        super(MachineFactory.steamPumpFactory);
    }

    @Override
    public void tick() {

    }
}
