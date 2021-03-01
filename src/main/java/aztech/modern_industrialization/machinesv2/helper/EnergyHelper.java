package aztech.modern_industrialization.machinesv2.helper;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.api.energy.EnergyMoveable;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.EnergyComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;

public class EnergyHelper {

    public static void autoOuput(MachineBlockEntity machine, OrientationComponent orientation, CableTier output, EnergyComponent energy){
        EnergyMoveable insertable = EnergyApi.MOVEABLE.get(machine.getWorld(), machine.getPos().offset(orientation.outputDirection), orientation.outputDirection.getOpposite());
        if (insertable instanceof EnergyInsertable && ((EnergyInsertable) insertable).canInsert(output)) {
            energy.insertEnergy((EnergyInsertable) insertable);
        }
        machine.markDirty();
    }
}
