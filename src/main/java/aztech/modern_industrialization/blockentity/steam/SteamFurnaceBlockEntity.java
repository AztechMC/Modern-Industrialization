package aztech.modern_industrialization.blockentity.steam;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blockentity.MachineBlockEntity;
import aztech.modern_industrialization.blockentity.factory.MachineFactory;
import net.minecraft.block.entity.BlockEntityType;

public class SteamFurnaceBlockEntity extends MachineBlockEntity {

    public SteamFurnaceBlockEntity() {
        super(MachineFactory.steamFurnaceFactory);
    }

    @Override
    public void tick() {

    }
}
