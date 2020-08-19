package aztech.modern_industrialization.machines.steam;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.factory.MachineBlockEntity;
import aztech.modern_industrialization.machines.factory.MachineFactory;

public class SteamFluidExtractorBlockEntity extends MachineBlockEntity {

    public SteamFluidExtractorBlockEntity() {
        super(MachineFactory.steamFluidExtractor, ModernIndustrialization.RECIPE_FLUID_EXTRACTOR);
    }

}
