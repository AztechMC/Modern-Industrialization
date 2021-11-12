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
package aztech.modern_industrialization.machines.multiblocks;

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.MachineOverlay;
import aztech.modern_industrialization.machines.components.ActiveShapeComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.ShapeValidComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public abstract class MultiblockMachineBlockEntity extends MachineBlockEntity {
    public MultiblockMachineBlockEntity(BEP bep, MachineGuiParameters guiParams, OrientationComponent.Params orientationParams) {
        super(bep, guiParams, orientationParams);
        this.shapeValid = new ShapeValidComponent();
        registerComponents(shapeValid);
    }

    public final ShapeValidComponent shapeValid;

    public boolean isShapeValid() {
        return shapeValid.shapeValid;
    }

    protected abstract void unlink();

    @Override
    public boolean useWrench(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (super.useWrench(player, hand, hitResult)) {
            if (!world.isClient) {
                unlink();
            }
            return true;
        }
        return false;
    }

    @Override
    public final void markRemoved() {
        super.markRemoved();
        if (!world.isClient) {
            unlink();
        }
    }

    public abstract ShapeTemplate getActiveShape();

    /**
     * Helper method for subclasses that want standard screwdriver handling.
     */
    protected boolean useScrewdriver(ActiveShapeComponent activeShape, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (activeShape.useScrewdriver(player, hand, MachineOverlay.findHitSide(hitResult))) {
            if (!player.getEntityWorld().isClient()) {
                unlink();
                sync(false);
            }
            return true;
        }
        return false;
    }

    public OrientationComponent getOrientation() {
        return orientation;
    }
}
