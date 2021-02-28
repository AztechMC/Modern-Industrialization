package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.api.energy.CableTier;
import net.minecraft.block.entity.BlockEntityType;

public class TransformerMachineBlockEntity  extends AbstractStorageMachineBlockEntity{

    public TransformerMachineBlockEntity(BlockEntityType<?> type, CableTier from, CableTier to) {
        super(type, from, to, getTransformerName(from, to) , 200*Math.max(from.eu, to.eu));
    }

    public static String getTransformerName(CableTier from, CableTier to){
        return from.name+"_"+to.name+"_transformer";
    }
}
