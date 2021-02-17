package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.block.entity.BlockEntityType;

public class SteamMachineBlockEntity extends AbstractRegularMachineBlockEntity {
    public SteamMachineBlockEntity(BlockEntityType<?> type, MachineRecipeType recipeType, MachineInventoryComponent inventory, MachineGuiParameters guiParams, ProgressBar.Parameters progressBarParams, MachineTier tier) {
        super(type, recipeType, inventory, guiParams, progressBarParams, tier);
    }

    @Override
    public long consumeEu(long max, Simulation simulation) {
        int totalRem = 0;
        for (ConfigurableFluidStack stack : getInventory().fluidStacks) {
            if (stack.getFluid() == MIFluids.STEAM) {
                long amount = stack.getAmount();
                long rem = Math.min(max, amount / 81);
                if (simulation.isActing()) {
                    stack.decrement(rem * 81);
                }
                max -= rem;
                totalRem += rem;
            }
        }
        return totalRem;
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData(tier == MachineTier.BRONZE ? MachineCasings.BRONZE : MachineCasings.STEEL);
        orientation.writeModelData(data);
        data.isActive = isActive;
        return data;
    }
}
