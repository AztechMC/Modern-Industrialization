package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.sync.EnergyBar;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.components.CrafterComponent;
import aztech.modern_industrialization.machinesv2.components.EnergyComponent;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Tickable;

public class ElectricMachineBlockEntity extends MachineBlockEntity implements CrafterComponent.Behavior, Tickable {
    public ElectricMachineBlockEntity(BlockEntityType<?> type, MachineRecipeType recipeType, MachineInventoryComponent inventory, MachineGuiParameters guiParams, EnergyBar.Parameters energyBarParams, ProgressBar.Parameters progressBarParams, MachineTier tier, long euCapacity) {
        super(type, guiParams);
        this.inventory = inventory;
        this.crafter = new CrafterComponent(inventory, this);
        this.energy = new EnergyComponent(euCapacity);
        this.type = recipeType;
        this.tier = tier;
        this.insertable = energy.buildInsertable(cableTier -> cableTier == CableTier.LV);
        registerClientComponent(new ProgressBar.Server(progressBarParams, crafter::getProgress));
        registerClientComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));
    }

    private final MachineInventoryComponent inventory;
    private final CrafterComponent crafter;
    private final EnergyComponent energy;

    private final MachineRecipeType type;
    private final MachineTier tier;

    private final EnergyInsertable insertable;

    @Override
    public long consumeEu(long max, Simulation simulation) {
        return energy.consumeEu(max, simulation); // FIXME steam machines
    }

    @Override
    public MachineRecipeType recipeType() {
        return type;
    }

    @Override
    public long getBaseRecipeEu() {
        return tier.getBaseEu();
    }

    @Override
    public long getMaxRecipeEu() {
        return tier.getMaxEu();
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            crafter.tickRecipe();
            markDirty();
            // FIXME auto-extract
        }
    }

    @Override
    public MIInventory getInventory() {
        return inventory.inventory;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        EnergyApi.MOVEABLE.registerForBlockEntities((be, direction) -> ((ElectricMachineBlockEntity) be).insertable, bet);
    }
}
