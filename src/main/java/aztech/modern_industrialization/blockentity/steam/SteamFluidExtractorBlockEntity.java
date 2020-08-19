package aztech.modern_industrialization.blockentity.steam;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blockentity.MachineBlockEntity;
import aztech.modern_industrialization.blockentity.factory.MachineFactory;

public class SteamFluidExtractorBlockEntity extends MachineBlockEntity {

    public SteamFluidExtractorBlockEntity() {
        super(MachineFactory.steamFluidExtractor, ModernIndustrialization.RECIPE_FLUID_EXTRACTOR);
    }

}
