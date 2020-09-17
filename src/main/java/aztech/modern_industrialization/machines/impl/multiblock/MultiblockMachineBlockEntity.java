package aztech.modern_industrialization.machines.impl.multiblock;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.impl.*;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.stream.Collectors;

import static aztech.modern_industrialization.machines.impl.MachineTier.*;

public class MultiblockMachineBlockEntity extends MachineBlockEntity {
    protected Map<BlockPos, HatchBlockEntity> linkedHatches = new TreeMap<>();
    protected Set<BlockPos> linkedStructureBlocks = new HashSet<>();
    protected final MultiblockShape shape;
    protected boolean ready = false;
    private boolean lateLoaded = false;
    private boolean isBuildingShape = false;
    protected Text errorMessage = null;
    private MachineTier tier = null;
    protected int shapeCheckTicks = 0;
    protected MachineModel hatchCasing;

    public MultiblockMachineBlockEntity(MachineFactory factory, MachineRecipeType recipeType, MultiblockShape shape) {
        super(factory, recipeType);
        itemStacks.clear();
        fluidStacks.clear();
        this.shape = shape;
        this.hatchCasing = factory.machineModel;
    }

    private void lateLoad() {
        loadDelayedActiveRecipe();
        if(lateLoaded) return;
        lateLoaded = true;
        rebuildShape();
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    public void hatchRemoved(BlockPos pos) { // TODO: use this?
        linkedHatches.remove(pos);
        rebuildShape();
    }

    protected void hatchLoaded() {
        lateLoad();
        if(!isBuildingShape) {
            rebuildShape();
        }
    }

    public void hatchUnloaded(BlockPos pos) {
        if(linkedHatches.containsKey(pos)) {
            linkedHatches.put(pos, null);
            ready = false;
        }
    }

    @Override
    public void setFacingDirection(Direction facingDirection) {
        super.setFacingDirection(facingDirection);
        rebuildShape();
    }

    protected void matchShape() {
        ready = shape.matchShape(world, pos, facingDirection, linkedHatches, linkedStructureBlocks);
        this.errorMessage = shape.getErrorMessage();
    }

    public void rebuildShape() {
        isBuildingShape = true;
        for(HatchBlockEntity hatch : linkedHatches.values()) {
            if(hatch != null) {
                hatch.unlink();
            }
        }
        linkedHatches.clear();
        linkedStructureBlocks.clear();

        matchShape();
        if(ready) {
            for(HatchBlockEntity hatch : linkedHatches.values()) {
                hatch.link(this);
            }
        } else {
            linkedHatches.clear();
            linkedStructureBlocks.clear();
        }

        // If there is an active recipe, we must check that there is enough room available in the output hatches
        clearLocks();
        if(activeRecipe != null) {
            if(putItemOutputs(activeRecipe, true, false) && putFluidOutputs(activeRecipe, true, false)) {
                // Relock stacks
                putItemOutputs(activeRecipe, true, true);
                putFluidOutputs(activeRecipe, true, true);
            } else {
                ready = false;
            }
        }

        if(ready) updateTier();

        isBuildingShape = false;
    }

    @Override
    protected boolean canRecipeProgress() {
        lateLoad();
        return ready;
    }

    @Override
    protected boolean canRecipeStart() {
        lateLoad();
        rebuildShape();
        return ready && super.canRecipeStart();
    }

    @Override
    protected List<ConfigurableFluidStack> getSteamInputStacks() {
        return getFluidInputStacks();
    }

    @Override
    public List<ConfigurableItemStack> getItemInputStacks() {
        return linkedHatches.values().stream().filter(Objects::nonNull).map(MachineBlockEntity::getItemInputStacks).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<ConfigurableItemStack> getItemOutputStacks() {
        return linkedHatches.values().stream().filter(Objects::nonNull).map(MachineBlockEntity::getItemOutputStacks).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<ConfigurableFluidStack> getFluidInputStacks() {
        return linkedHatches.values().stream().filter(Objects::nonNull).map(MachineBlockEntity::getFluidInputStacks).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<ConfigurableFluidStack> getFluidOutputStacks() {
        return linkedHatches.values().stream().filter(Objects::nonNull).map(MachineBlockEntity::getFluidOutputStacks).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        for(HatchBlockEntity hbe : linkedHatches.values()) {
            hbe.unlink();
        }
        clearLocks();
    }

    @Override
    public void tick() {
        if(shapeCheckTicks == 0) {
            rebuildShape();
            shapeCheckTicks = 20;
        }
        --shapeCheckTicks;
        super.tick();
    }

    public Text getErrorMessage() {
        return errorMessage;
    }

    /**
     * Calculate the multiblock tier.
     * If the multiblock has a steel hatch, it is steel tier. Otherwise, it is bronze tier.
     */
    private void updateTier() {
        // TODO: electric hatches
        for(HatchBlockEntity hatch : linkedHatches.values()) {
            if(hatch instanceof EnergyInputHatchBlockEntity) {
                tier = UNLIMITED;
                return;
            } else if(hatch.getFactory().tier == STEEL && factory instanceof SteamMachineFactory) {
                tier = STEEL;
                return;
            }
        }
        tier = BRONZE;
    }

    @Override
    public int getEu(int maxEu, boolean simulate) {
        if(factory instanceof SteamMachineFactory) {
            return super.getEu(maxEu, simulate);
        } else {
            int total = 0;
            for(HatchBlockEntity hatch : linkedHatches.values()) {
                if(hatch instanceof EnergyInputHatchBlockEntity) {
                    total += hatch.getEu(maxEu - total, simulate);
                }
            }
            return total;
        }
    }

    @Override
    public MachineTier getTier() {
        return tier;
    }
}
