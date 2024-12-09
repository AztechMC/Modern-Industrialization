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
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.ShapeValidComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class MultiblockMachineBlockEntity extends MachineBlockEntity {
    @Nullable
    private ShapeMatcher shapeMatcher;

    public MultiblockMachineBlockEntity(BEP bep, MachineGuiParameters guiParams, OrientationComponent.Params orientationParams) {
        super(bep, guiParams, orientationParams);
        this.shapeValid = new ShapeValidComponent();
        registerComponents(shapeValid);
    }

    public final ShapeValidComponent shapeValid;

    public boolean isShapeValid() {
        return shapeValid.shapeValid;
    }

    public ShapeMatcher createShapeMatcher() {
        return new ShapeMatcher(level, worldPosition, orientation.facingDirection, getActiveShape());
    }

    protected void onRematch(ShapeMatcher shapeMatcher) {
    }

    protected final void link() {
        if (shapeMatcher == null) {
            shapeMatcher = createShapeMatcher();
            shapeMatcher.registerListeners(level);
        }
        if (shapeMatcher.needsRematch()) {
            shapeValid.shapeValid = false;
            shapeMatcher.rematch(level);
            onRematch(shapeMatcher);

            if (shapeMatcher.isMatchSuccessful()) {
                shapeValid.shapeValid = true;
            }

            if (shapeValid.update()) {
                sync(false);
            }
        }
    }

    public final void unlink() {
        if (shapeMatcher != null) {
            shapeMatcher.unlinkHatches();
            shapeMatcher.unregisterListeners(level);
            shapeMatcher = null;
        }
    }

    @Override
    public boolean useWrench(Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (super.useWrench(player, hand, hitResult)) {
            if (!level.isClientSide) {
                unlink();
            }
            return true;
        }
        return false;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!level.isClientSide) {
            unlink();
        }
    }

    public abstract ShapeTemplate getActiveShape();

    public OrientationComponent getOrientation() {
        return orientation;
    }
}
