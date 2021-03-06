package aztech.modern_industrialization.machinesv2.blockentities.hatches;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.impl.multiblock.HatchType;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.multiblocks.HatchBlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import java.util.List;

public class FluidHatch extends HatchBlockEntity {
    public FluidHatch(BlockEntityType<?> type, MachineGuiParameters guiParams, boolean input, boolean upgradesToSteel, MIInventory inventory) {
        super(type, guiParams, new OrientationComponent.Params(true, false, true));

        this.input = input;
        this.upgradesToSteel = upgradesToSteel;
        this.inventory = inventory;
    }

    private final boolean input;
    private final boolean upgradesToSteel;
    private final MIInventory inventory;

    @Override
    public HatchType getHatchType() {
        return input ? HatchType.FLUID_INPUT : HatchType.FLUID_OUTPUT;
    }

    @Override
    public boolean upgradesToSteel() {
        return upgradesToSteel;
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    public void appendFluidInputs(List<ConfigurableFluidStack> list) {
        if (input) {
            list.addAll(inventory.fluidStacks);
        }
    }

    @Override
    public void appendFluidOutputs(List<ConfigurableFluidStack> list) {
        if (!input) {
            list.addAll(inventory.fluidStacks);
        }
    }
}
