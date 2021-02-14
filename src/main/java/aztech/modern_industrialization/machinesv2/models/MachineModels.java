package aztech.modern_industrialization.machinesv2.models;

import aztech.modern_industrialization.MIIdentifier;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

public final class MachineModels {
    public static void init() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new MachineModelProvider());
        ModelLoadingRegistry.INSTANCE.registerModelProvider(new MachineModelProvider());

        MachineUnbakedModel model = new MachineUnbakedModel("macerator", true, true, false, MachineCasings.LV);
        MachineModelProvider.register(new MIIdentifier("block/lv_macerator"), model);
        MachineModelProvider.register(new MIIdentifier("item/lv_macerator"), model);
    }

    private MachineModels() {
    }
}
