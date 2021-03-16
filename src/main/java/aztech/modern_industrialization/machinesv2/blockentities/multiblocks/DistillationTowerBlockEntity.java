package aztech.modern_industrialization.machinesv2.blockentities.multiblocks;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.impl.multiblock.HatchType;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.components.CrafterComponent;
import aztech.modern_industrialization.machinesv2.components.EnergyComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.multiblocks.*;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

// TODO: should the common part with ElectricCraftingMultiblockBlockEntity be refactored?
public class DistillationTowerBlockEntity extends AbstractCraftingMultiblockBlockEntity {
    private static final ShapeTemplate[] shapeTemplates;

    public DistillationTowerBlockEntity(BlockEntityType<?> type) {
        super(type, "distillation_tower", new OrientationComponent(new OrientationComponent.Params(false, false, false)), shapeTemplates);
    }

    @Override
    protected CrafterComponent.Behavior getBehavior() {
        return new Behavior();
    }

    private final List<EnergyComponent> energyInputs = new ArrayList<>();
    private int maxFluidOutputs = 0;

    @Override
    protected void onSuccessfulMatch(ShapeMatcher shapeMatcher) {
        energyInputs.clear();
        maxFluidOutputs = 0;
        for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
            hatch.appendEnergyInputs(energyInputs);
            if (hatch.getHatchType() == HatchType.FLUID_OUTPUT) {
                maxFluidOutputs++;
            }
        }
    }

    private class Behavior implements CrafterComponent.Behavior {
        @Override
        public long consumeEu(long max, Simulation simulation) {
            long total = 0;

            for (EnergyComponent energyComponent : energyInputs) {
                total += energyComponent.consumeEu(max - total, simulation);
            }

            return total;
        }

        @Override
        public MachineRecipeType recipeType() {
            return MIMachineRecipeTypes.DISTILLATION_TOWER;
        }

        @Override
        public long getBaseRecipeEu() {
            return MachineTier.UNLIMITED.getBaseEu();
        }

        @Override
        public long getMaxRecipeEu() {
            return MachineTier.UNLIMITED.getMaxEu();
        }

        @Override
        public World getWorld() {
            return world;
        }

        @Override
        public int getMaxFluidOutputs() {
            return maxFluidOutputs;
        }
    }

    static {
        int maxHeight = 9;
        shapeTemplates = new ShapeTemplate[maxHeight];

        SimpleMember casing = SimpleMember.forBlock(new MIIdentifier("clean_stainless_steel_machine_casing"));
        HatchFlags hatchFlags = new HatchFlags.Builder().with(HatchType.ENERGY_INPUT, HatchType.FLUID_INPUT, HatchType.FLUID_OUTPUT).build();
        for (int i = 0; i < maxHeight; ++i) {
            ShapeTemplate.Builder builder = new ShapeTemplate.Builder(MachineCasings.CLEAN_STAINLESS_STEEL);
            for (int y = 0; y <= i+1; ++y) {
                builder.add3by3(y, casing, y != 0, hatchFlags);
            }
            shapeTemplates[i] = builder.build();
        }
    }
}
