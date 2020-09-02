package aztech.modern_industrialization.machines.impl.multiblock;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineTier;
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

import static aztech.modern_industrialization.machines.impl.MachineTier.BRONZE;
import static aztech.modern_industrialization.machines.impl.MachineTier.STEEL;

public class MultiblockMachineBlockEntity extends MachineBlockEntity {
    protected Map<BlockPos, HatchBlockEntity> linkedHatches = new TreeMap<>();
    protected Set<BlockPos> linkedStructureBlocks = new HashSet<>();
    protected final MultiblockShape shape;
    protected boolean ready = false;
    private boolean lateLoaded = false;
    private boolean isBuildingShape = false;
    private Text errorMessage = null;
    private MachineTier tier = null;
    private int shapeCheckTicks = 0;

    public MultiblockMachineBlockEntity(MachineFactory factory, MachineRecipeType recipeType, MultiblockShape shape) {
        super(factory, recipeType);
        itemStacks.clear();
        fluidStacks.clear();
        this.shape = shape;
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

    public void hatchRemoved(BlockPos pos) {
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

    public void rebuildShape() {
        isBuildingShape = true;
        for(HatchBlockEntity hatch : linkedHatches.values()) {
            if(hatch != null) {
                hatch.controllerPos = null;
            }
        }
        linkedHatches.clear();
        linkedStructureBlocks.clear();

        ready = shape.matchShape(world, pos, facingDirection, linkedHatches, linkedStructureBlocks);
        this.errorMessage = shape.errorMessage;
        if(ready) {
            for(HatchBlockEntity hatch : linkedHatches.values()) {
                hatch.lateLoad();
                hatch.controllerPos = pos;
                hatch.markDirty();
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
        return ready;
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
            if(hatch.getTier() == STEEL) {
                tier = STEEL;
                return;
            }
        }
        tier = BRONZE;
    }

    @Override
    public MachineTier getTier() {
        return tier;
    }
}
