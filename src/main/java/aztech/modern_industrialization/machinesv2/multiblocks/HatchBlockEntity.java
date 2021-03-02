package aztech.modern_industrialization.machinesv2.multiblocks;

import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.multiblock.HatchType;
import net.minecraft.util.math.BlockPos;

public abstract class HatchBlockEntity extends MachineBlockEntity {
    public HatchBlockEntity(MachineFactory factory) {
        super(factory);
    }

    BlockPos matchedController = null;

    public void unlink() {
        matchedController = null;
    }

    public void link(BlockPos controller) {
        matchedController = controller;
    }

    public abstract HatchType getHatchType();
}
