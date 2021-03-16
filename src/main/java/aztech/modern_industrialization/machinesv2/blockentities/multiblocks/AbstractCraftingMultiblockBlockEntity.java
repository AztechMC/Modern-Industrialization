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
package aztech.modern_industrialization.machinesv2.blockentities.multiblocks;

import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machinesv2.components.CrafterComponent;
import aztech.modern_industrialization.machinesv2.components.IsActiveComponent;
import aztech.modern_industrialization.machinesv2.components.MultiblockInventoryComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.components.sync.CraftingMultiblockGUI;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.machinesv2.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tickable;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCraftingMultiblockBlockEntity extends MultiblockMachineBlockEntity implements Tickable {
    public AbstractCraftingMultiblockBlockEntity(BlockEntityType<?> type, String name, OrientationComponent orientation,
            ShapeTemplate[] shapeTemplates) {
        super(type, new MachineGuiParameters.Builder(name, false).backgroundHeight(200).build(), orientation);

        this.shapeTemplates = shapeTemplates;
        this.inventory = new MultiblockInventoryComponent();
        this.crafter = new CrafterComponent(inventory, getBehavior());
        this.isActive = new IsActiveComponent();
        registerClientComponent(new CraftingMultiblockGUI.Server(() -> isShapeValid, crafter::getProgress, crafter));
        registerComponents(crafter, isActive);
    }

    /**
     * Only called once in the constructor to allow for inner classes in subclasses.
     */
    protected abstract CrafterComponent.Behavior getBehavior();

    public final ShapeTemplate[] shapeTemplates;
    protected int activeShape = 0;
    @Nullable
    private ShapeMatcher shapeMatcher = null;
    private boolean allowNormalOperation = false;
    private boolean isShapeValid = false;

    protected final MultiblockInventoryComponent inventory;
    private final CrafterComponent crafter;
    private final IsActiveComponent isActive;

    protected abstract void onSuccessfulMatch(ShapeMatcher shapeMatcher);

    public ShapeTemplate getActiveShape() {
        return shapeTemplates[activeShape];
    }

    @Override
    public final MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    protected final MachineModelClientData getModelData() {
        return new MachineModelClientData(null, orientation.facingDirection).active(isActive.isActive);
    }

    @Override
    public final void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    @Override
    public final void tick() {
        if (!world.isClient) {
            link();

            boolean newActive = false;

            if (allowNormalOperation) {
                if (crafter.tickRecipe()) {
                    newActive = true;
                }
            }

            isActive.updateActive(newActive, this);
        }
        tickExtra();
    }

    public void tickExtra() {

    }

    protected final void link() {
        if (shapeMatcher == null) {
            shapeMatcher = new ShapeMatcher(world, pos, orientation.facingDirection, shapeTemplates[activeShape]);
            shapeMatcher.registerListeners(world);
        }
        if (shapeMatcher.needsRematch()) {
            allowNormalOperation = false;
            isShapeValid = false;
            shapeMatcher.rematch(world);

            if (shapeMatcher.isMatchSuccessful()) {
                inventory.rebuild(shapeMatcher);

                onSuccessfulMatch(shapeMatcher);
                isShapeValid = true;

                // If there was an active recipe, we have to make sure the output fits, and lock
                // the hatches.
                if (crafter.tryContinueRecipe()) {
                    allowNormalOperation = true;
                }
            }
        }
    }

    @Override
    protected final void unlink() {
        if (shapeMatcher != null) {
            shapeMatcher.unlinkHatches();
            shapeMatcher.unregisterListeners(world);
            shapeMatcher = null;
        }
    }
}
