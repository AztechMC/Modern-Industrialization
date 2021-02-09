package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.machinesv2.components.CrafterComponent;
import aztech.modern_industrialization.machinesv2.components.EnergyComponent;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Tickable;

public class MachineBlockEntity extends FastBlockEntity implements CrafterComponent.Behavior, Tickable {
    public MachineBlockEntity(BlockEntityType<?> type, MachineRecipeType recipeType, MachineInventoryComponent inventory, MachineTier tier) {
        super(type);
        this.inventory = inventory;
        this.type = recipeType;
        this.crafter = new CrafterComponent(inventory, this);
        this.tier = tier;
    }

    private final MachineInventoryComponent inventory;
    private final CrafterComponent crafter;
    private final EnergyComponent energy = new EnergyComponent();

    private final MachineRecipeType type;
    private final MachineTier tier;

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
}
