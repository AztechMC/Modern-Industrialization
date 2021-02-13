package aztech.modern_industrialization.machinesv2.components;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;

import java.util.ArrayList;
import java.util.List;

public class MachineInventoryComponent implements CrafterComponent.Inventory {
    public final int itemInputCount;
    public final int itemOutputCount;
    public final int fluidInputCount;
    public final int fluidOutputCount;

    public final MIInventory inventory;

    public MachineInventoryComponent(
            List<ConfigurableItemStack> itemInputs, List<ConfigurableItemStack> itemOutputs,
            List<ConfigurableFluidStack> fluidInputs, List<ConfigurableFluidStack> fluidOutputs,
            SlotPositions itemPositions, SlotPositions fluidPositions) {
        this.itemInputCount = itemInputs.size();
        this.itemOutputCount = itemOutputs.size();
        this.fluidInputCount = fluidInputs.size();
        this.fluidOutputCount = fluidOutputs.size();

        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        itemStacks.addAll(itemInputs);
        itemStacks.addAll(itemOutputs);
        List<ConfigurableFluidStack> fluidStacks = new ArrayList<>();
        fluidStacks.addAll(fluidInputs);
        fluidStacks.addAll(fluidOutputs);

        this.inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);
    }

    @Override
    public List<ConfigurableItemStack> getItemInputs() {
        return inventory.itemStacks.subList(0, itemInputCount);
    }

    @Override
    public List<ConfigurableItemStack> getItemOutputs() {
        return inventory.itemStacks.subList(itemInputCount, itemInputCount + itemOutputCount);
    }

    @Override
    public List<ConfigurableFluidStack> getFluidInputs() {
        return inventory.fluidStacks.subList(0, fluidInputCount);
    }

    @Override
    public List<ConfigurableFluidStack> getFluidOutputs() {
        return inventory.fluidStacks.subList(fluidInputCount, fluidInputCount + fluidOutputCount);
    }
}
