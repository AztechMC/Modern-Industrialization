/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machines.impl.multiblock;

import static aztech.modern_industrialization.machines.impl.MachineTier.*;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.impl.*;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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

    public MultiblockMachineBlockEntity(MachineFactory factory, MultiblockShape shape, boolean clear) {
        super(factory);
        if (clear) {
            itemStacks.clear();
            fluidStacks.clear();
        }
        this.shape = shape;
        this.hatchCasing = factory.machineModel;
    }

    public MultiblockMachineBlockEntity(MachineFactory factory, MultiblockShape shape) {
        this(factory, shape, true);
    }

    private void lateLoad() {
        loadDelayedActiveRecipe();
        if (lateLoaded)
            return;
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
        if (!isBuildingShape) {
            rebuildShape();
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
        for (HatchBlockEntity hatch : linkedHatches.values()) {
            if (hatch != null) {
                hatch.unlink();
            }
        }
        linkedHatches.clear();
        linkedStructureBlocks.clear();

        matchShape();
        if (ready) {
            for (HatchBlockEntity hatch : linkedHatches.values()) {
                hatch.link(this);
            }
        } else {
            linkedHatches.clear();
            linkedStructureBlocks.clear();
        }

        // If there is an active recipe, we must check that there is enough room
        // available in the output hatches
        clearLocks();
        if (activeRecipe != null) {
            if (putItemOutputs(activeRecipe, true, false) && putFluidOutputs(activeRecipe, true, false)) {
                // Relock stacks
                putItemOutputs(activeRecipe, true, true);
                putFluidOutputs(activeRecipe, true, true);
            } else {
                ready = false;
            }
        }

        if (ready)
            updateTier();

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
        return ready && super.canRecipeStart();
    }

    @Override
    protected List<ConfigurableFluidStack> getSteamInputStacks() {
        return getFluidInputStacks();
    }

    @Override
    public List<ConfigurableItemStack> getItemInputStacks() {
        return linkedHatches.values().stream().filter(Objects::nonNull).map(MachineBlockEntity::getItemInputStacks).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurableItemStack> getItemOutputStacks() {
        return linkedHatches.values().stream().filter(Objects::nonNull).map(MachineBlockEntity::getItemOutputStacks).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurableFluidStack> getFluidInputStacks() {
        return linkedHatches.values().stream().filter(Objects::nonNull).map(MachineBlockEntity::getFluidInputStacks).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurableFluidStack> getFluidOutputStacks() {
        return linkedHatches.values().stream().filter(Objects::nonNull).map(MachineBlockEntity::getFluidOutputStacks).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        for (HatchBlockEntity hbe : linkedHatches.values()) {
            hbe.unlink();
        }
        clearLocks();
    }

    public void tickCheckShape() {
        if (shapeCheckTicks == 0) {
            rebuildShape();
            shapeCheckTicks = 20;
        }
        --shapeCheckTicks;
    }

    @Override
    public void tick() {
        this.tickCheckShape();
        super.tick();
    }

    public Text getErrorMessage() {
        return errorMessage;
    }

    /**
     * Calculate the multiblock tier. If the multiblock has a steel hatch, it is
     * steel tier. Otherwise, it is bronze tier.
     */
    private void updateTier() {
        // TODO: electric hatches
        for (HatchBlockEntity hatch : linkedHatches.values()) {
            if (hatch instanceof EnergyInputHatchBlockEntity) {
                tier = UNLIMITED;
                return;
            } else if (hatch.getFactory().tier != BRONZE && factory instanceof SteamMachineFactory) {
                tier = STEEL;
                return;
            }
        }
        tier = BRONZE;
    }

    @Override
    public int getEu(int maxEu, boolean simulate) {
        if (factory instanceof SteamMachineFactory) {
            return super.getEu(maxEu, simulate);
        } else {
            int total = 0;
            for (HatchBlockEntity hatch : linkedHatches.values()) {
                if (hatch instanceof EnergyInputHatchBlockEntity) {
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
