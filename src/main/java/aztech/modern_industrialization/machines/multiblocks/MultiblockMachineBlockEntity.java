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

public abstract class MultiblockMachineBlockEntity extends MachineBlockEntity {
    protected ShapeMatcher shapeMatcher;

    public MultiblockMachineBlockEntity(BEP bep, MachineGuiParameters guiParams, OrientationComponent.Params orientationParams) {
        super(bep, guiParams, orientationParams);
        this.shapeValid = new ShapeValidComponent();
        registerComponents(shapeValid);
    }

    public final ShapeValidComponent shapeValid;

    public boolean isShapeValid() {
        return shapeValid.shapeValid;
    }

    public ShapeMatcher getShapeMatcher() {
        return shapeMatcher;
    }

    public ShapeMatcher createShapeMatcher() {
        return new ShapeMatcher(level, worldPosition, orientation.facingDirection, getActiveShape());
    }

    protected void onLink() {
    }

    protected void onUnlink() {
    }

    protected void onRematch() {
    }

    protected void onMatchSuccessful() {
    }

    protected void onMatchFailure() {
    }

    protected void link() {
        if (shapeMatcher == null) {
            shapeMatcher = createShapeMatcher();
            shapeMatcher.registerListeners(level);
            onLink();
        }
        if (shapeMatcher.needsRematch()) {
            shapeValid.shapeValid = false;
            shapeMatcher.rematch(level);
            onRematch();

            if (shapeMatcher.isMatchSuccessful()) {
                onMatchSuccessful();
                shapeValid.shapeValid = true;
            } else {
                onMatchFailure();
            }

            if (shapeValid.update()) {
                sync(false);
            }
        }
    }

    public void unlink() {
        if (shapeMatcher != null) {
            shapeMatcher.unlinkHatches();
            shapeMatcher.unregisterListeners(level);
            shapeMatcher = null;
            onUnlink();
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
    public final void setRemoved() {
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
