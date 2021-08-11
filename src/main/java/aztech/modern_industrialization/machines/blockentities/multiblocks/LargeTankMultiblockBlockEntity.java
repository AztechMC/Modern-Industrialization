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
package aztech.modern_industrialization.machines.blockentities.multiblocks;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.DynamicShapeComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.util.Tickable;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class LargeTankMultiblockBlockEntity extends MultiblockMachineBlockEntity
        implements Tickable, DynamicShapeComponent.DynamicShapeComponentBlockEntity {

    private ShapeMatcher shapeMatcher;
    private DynamicShapeComponent shapeComponent;

    private static final ShapeTemplate[] shapeTemplates;

    static {
        shapeTemplates = new ShapeTemplate[75];
        for (int i = 0; i < shapeTemplates.length; i++) {
            shapeTemplates[i] = buildShape(i);
        }
    }

    private static ShapeTemplate buildShape(int index) {
        int sizeX = 3 + 2 * (index / 25);
        int sizeY = 3 + (index % 25) / 5;
        int sizeZ = 3 + (index % 25) % 5;

        ShapeTemplate.Builder templateBuilder = new ShapeTemplate.Builder(MachineCasings.STEEL);
        SimpleMember steelCasing = SimpleMember.forBlock(MIBlock.blocks.get("steel_machine_casing"));
        SimpleMember glass = SimpleMember.forBlock(Blocks.GLASS);
        HatchFlags hatchFlags = new HatchFlags.Builder().build();

        for (int x = -sizeX / 2; x <= sizeX / 2; x++) {
            for (int y = -1; y < sizeY - 1; y++) {
                for (int z = 0; z < sizeZ; z++) {

                    int lim = 0;

                    if (x == -sizeX / 2 || x == sizeX / 2) {
                        lim++;
                    }
                    if (y == -1 || y == sizeY - 2) {
                        lim++;
                    }
                    if (z == 0 || z == sizeZ - 1) {
                        lim++;
                    }
                    if (x != 0 || y != 0 || z != 0) {
                        if (lim == 1) {
                            templateBuilder.add(x, y, z, glass, hatchFlags);
                        } else if (lim >= 2) {
                            templateBuilder.add(x, y, z, steelCasing, hatchFlags);
                        }
                    }

                }
            }
        }

        return templateBuilder.build();
    }

    public LargeTankMultiblockBlockEntity(BEP bep) {

        super(bep, new MachineGuiParameters.Builder("large_tank", true).build(),
                new OrientationComponent(new OrientationComponent.Params(false, false, false)));

        shapeComponent = new DynamicShapeComponent(shapeTemplates);

        this.registerComponents(shapeComponent);
    }

    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    public ShapeTemplate getActiveShape() {
        return shapeComponent.getActiveShape();
    }

    @Override
    protected MachineModelClientData getModelData() {
        return new MachineModelClientData(null, orientation.facingDirection);

    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    protected final void link() {
        shapeComponent.link(this);
    }

    @Override
    protected final void unlink() {
        shapeComponent.unlink(this);
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            link();
            markDirty();
        }
    }

    @Override
    public ShapeMatcher getShapeMatcher() {
        return shapeMatcher;
    }

    @Override
    public void setShapeMatcher(ShapeMatcher shapeMatcher) {
        this.shapeMatcher = shapeMatcher;
    }

    @Override
    public MultiblockMachineBlockEntity getMultiblockMachineBlockEntity() {
        return this;
    }

    @Override
    public void onMatchSuccessful() {
        int i = shapeComponent.getActiveShapeIndex();
    }
}
