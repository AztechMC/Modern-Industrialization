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
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import net.minecraft.nbt.NbtCompound;

public class DynamicShapeComponent implements IComponent {

    public final ShapeTemplate[] shapeTemplates;
    private int activeShape = 0;

    public DynamicShapeComponent(ShapeTemplate[] shapeTemplates) {
        this.shapeTemplates = shapeTemplates;
    }

    public int getActiveShapeIndex() {
        return activeShape;
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putInt("activeShape", activeShape);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        activeShape = tag.getInt("activeShape");
    }

    public ShapeTemplate getActiveShape() {
        return shapeTemplates[activeShape];
    }

    public void link(DynamicShapeComponentBlockEntity dynamicShapeBlockEntity) {

        MultiblockMachineBlockEntity blockEntity = dynamicShapeBlockEntity.getMultiblockMachineBlockEntity();

        ShapeMatcher currentShapeMatcher = dynamicShapeBlockEntity.getShapeMatcher();
        ShapeMatcher oldShapeMatcher = currentShapeMatcher;

        for (int j = 0; j < shapeTemplates.length; j++) {

            int shapeIndex = (activeShape + j) % shapeTemplates.length;

            ShapeTemplate currentShapeAttempt = shapeTemplates[shapeIndex];

            if (j != 0 || currentShapeMatcher == null) {
                currentShapeMatcher = new ShapeMatcher(blockEntity.getWorld(), blockEntity.getPos(), blockEntity.orientation.facingDirection,
                        currentShapeAttempt);
            }

            if (currentShapeMatcher.needsRematch()) {
                blockEntity.shapeValid.shapeValid = false;
                currentShapeMatcher.rematch(blockEntity.getWorld());

                if (currentShapeMatcher.isMatchSuccessful()) {

                    if (currentShapeMatcher != oldShapeMatcher) {
                        unlink(dynamicShapeBlockEntity);
                        dynamicShapeBlockEntity.setShapeMatcher(currentShapeMatcher);
                        currentShapeMatcher.registerListeners(blockEntity.getWorld());
                    }

                    blockEntity.shapeValid.shapeValid = true;

                    boolean sync = true;

                    if (activeShape != shapeIndex) {
                        sync = true;
                    }
                    this.activeShape = shapeIndex;

                    if (blockEntity.shapeValid.update()) {
                        sync = true;
                    }
                    if (sync) {
                        blockEntity.sync(false);
                    }

                    dynamicShapeBlockEntity.onMatchSuccessful();

                    return;
                } else {
                    if (blockEntity.shapeValid.update()) {
                        blockEntity.sync(false);
                    }
                }
            } else {
                return;
            }
        }
    }

    public void unlink(DynamicShapeComponentBlockEntity dynamicShapeBlockEntity) {
        if (dynamicShapeBlockEntity.getShapeMatcher() != null) {
            dynamicShapeBlockEntity.getShapeMatcher().unlinkHatches();
            dynamicShapeBlockEntity.getShapeMatcher().unregisterListeners(dynamicShapeBlockEntity.getMultiblockMachineBlockEntity().getWorld());
            dynamicShapeBlockEntity.setShapeMatcher(null);
        }
    }

    public interface DynamicShapeComponentBlockEntity {

        ShapeMatcher getShapeMatcher();

        void setShapeMatcher(ShapeMatcher shapeMatcher);

        MultiblockMachineBlockEntity getMultiblockMachineBlockEntity();

        void onMatchSuccessful();
    }

}
