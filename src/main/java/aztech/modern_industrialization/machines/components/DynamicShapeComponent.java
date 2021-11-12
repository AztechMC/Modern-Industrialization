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
import net.minecraft.world.World;

public class DynamicShapeComponent implements IComponent {

    public final ShapeTemplate[] shapeTemplates;
    private int activeShape = 0;
    private ShapeMatcher shapeMatcher;
    /**
     * Not actually a shape matcher, it's just used to listen to block updates in
     * the union of all shapes.
     */
    private ShapeMatcher unionShapeMatcher;

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

        if (unionShapeMatcher == null) {
            unionShapeMatcher = new ShapeMatcher(blockEntity.getWorld(), blockEntity.getPos(), blockEntity.getOrientation().facingDirection,
                    ShapeTemplate.computeDummyUnion(shapeTemplates));
            unionShapeMatcher.registerListeners(blockEntity.getWorld());
        }

        ShapeMatcher currentShapeMatcher = shapeMatcher;
        ShapeMatcher oldShapeMatcher = currentShapeMatcher;

        for (int j = 0; j < shapeTemplates.length; j++) {

            int shapeIndex = (activeShape + j) % shapeTemplates.length;

            ShapeTemplate currentShapeAttempt = shapeTemplates[shapeIndex];

            if (j != 0 || currentShapeMatcher == null) {
                currentShapeMatcher = new ShapeMatcher(blockEntity.getWorld(), blockEntity.getPos(), blockEntity.getOrientation().facingDirection,
                        currentShapeAttempt);
            }

            if (currentShapeMatcher.needsRematch() || unionShapeMatcher.needsRematch()) {
                blockEntity.shapeValid.shapeValid = false;
                currentShapeMatcher.rematch(blockEntity.getWorld());

                if (currentShapeMatcher.isMatchSuccessful()) {

                    if (currentShapeMatcher != oldShapeMatcher) {
                        unlink(dynamicShapeBlockEntity);
                        shapeMatcher = currentShapeMatcher;
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
        World world = dynamicShapeBlockEntity.getMultiblockMachineBlockEntity().getWorld();

        if (shapeMatcher != null) {
            shapeMatcher.unlinkHatches();
            shapeMatcher.unregisterListeners(world);
            shapeMatcher = null;
        }
        if (unionShapeMatcher != null) {
            unionShapeMatcher.unregisterListeners(world);
            unionShapeMatcher = null;
        }
    }

    public interface DynamicShapeComponentBlockEntity {

        MultiblockMachineBlockEntity getMultiblockMachineBlockEntity();

        void onMatchSuccessful();
    }

}
