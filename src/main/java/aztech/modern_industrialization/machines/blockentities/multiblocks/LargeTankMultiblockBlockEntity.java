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
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.DynamicShapeComponent;
import aztech.modern_industrialization.machines.components.FluidStorageComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.sync.FluidGUIComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.util.Tickable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class LargeTankMultiblockBlockEntity extends MultiblockMachineBlockEntity
        implements Tickable, DynamicShapeComponent.DynamicShapeComponentBlockEntity {

    private static final ShapeTemplate[] shapeTemplates;

    static {
        shapeTemplates = new ShapeTemplate[75];
        for (int i = 0; i < shapeTemplates.length; i++) {
            shapeTemplates[i] = buildShape(i);
        }

    }

    public static void registerFluidAPI(BlockEntityType<?> bet) {
        FluidStorage.SIDED.registerForBlockEntities((be, direction) -> {
            LargeTankMultiblockBlockEntity tank = ((LargeTankMultiblockBlockEntity) be);
            if (tank.isShapeValid()) {
                return tank.fluidStorage.getFluidStorage();
            } else {
                return Storage.empty();
            }
        }, bet);
    }

    private static ShapeTemplate buildShape(int index) {
        int sizeX = 3 + 2 * (index / 25);
        int sizeY = 3 + (index % 25) / 5;
        int sizeZ = 3 + (index % 25) % 5;

        ShapeTemplate.Builder templateBuilder = new ShapeTemplate.Builder(MachineCasings.STEEL);
        SimpleMember steelCasing = SimpleMember.forBlock(MIBlock.BLOCKS.get(new MIIdentifier("steel_machine_casing")).asBlock());
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
                    // TODO ADD AIR EMPTY CONDITION

                }
            }
        }

        return templateBuilder.build();
    }

    private final DynamicShapeComponent shapeComponent;
    private final FluidStorageComponent fluidStorage;

    private FluidGUIComponent.Data oldFluidData;

    public LargeTankMultiblockBlockEntity(BEP bep) {

        super(bep, new MachineGuiParameters.Builder("large_tank", false).build(), new OrientationComponent.Params(false, false, false));

        shapeComponent = new DynamicShapeComponent(shapeTemplates);
        fluidStorage = new FluidStorageComponent();

        this.registerComponents(shapeComponent);
        this.registerComponents(fluidStorage);
        this.registerClientComponent(new FluidGUIComponent.Server(this::getFluidData));
    }

    public FluidGUIComponent.Data getFluidData() {
        return new FluidGUIComponent.Data(fluidStorage.getFluid(), fluidStorage.getAmount(), fluidStorage.getCapacity());
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

    protected final void link() {
        shapeComponent.link(this);
    }

    @Override
    protected final void unlink() {
        shapeComponent.unlink(this);
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            link();
            setChanged();
            if (!this.getFluidData().equals(oldFluidData)) {
                oldFluidData = this.getFluidData();
                sync(false);
            }
        }
    }

    @Override
    public MultiblockMachineBlockEntity getMultiblockMachineBlockEntity() {
        return this;
    }

    @Override
    public void onMatchSuccessful() {
        int index = shapeComponent.getActiveShapeIndex();
        int sizeX = 3 + 2 * (index / 25);
        int sizeY = 3 + (index % 25) / 5;
        int sizeZ = 3 + (index % 25) % 5;
        int volume = sizeX * sizeY * sizeZ;
        long capacity = (long) volume * 64 * FluidConstants.BUCKET; // 64 Bucket / Block
        fluidStorage.setCapacity(capacity);
    }

    public FluidVariant getFluid() {
        return fluidStorage.getFluid();
    }

    public double getFullnessFraction() {
        return (double) fluidStorage.getAmount() / fluidStorage.getCapacity();
    }

    public int[] getCornerPosition() {

        int index = shapeComponent.getActiveShapeIndex();
        int sizeX = 3 + 2 * (index / 25);
        int sizeY = 3 + (index % 25) / 5;
        int sizeZ = 3 + (index % 25) % 5;

        BlockPos[] corners = new BlockPos[] { ShapeMatcher.toWorldPos(getBlockPos(), orientation.facingDirection, new BlockPos(-sizeX / 2 + 1, 0, 1)),
                ShapeMatcher.toWorldPos(getBlockPos(), orientation.facingDirection, new BlockPos(sizeX / 2 - 1, sizeY - 3, sizeZ - 2)), };

        int[] cornerPosition = new int[6];
        for (int i = 0; i < 3; i++) {
            cornerPosition[i] = Integer.MIN_VALUE;
            cornerPosition[i + 3] = Integer.MAX_VALUE;
        }

        for (int i = 0; i < 2; i++) {
            cornerPosition[0] = Math.max(corners[i].getX() - this.getBlockPos().getX(), cornerPosition[0]);
            cornerPosition[1] = Math.max(corners[i].getY() - this.getBlockPos().getY(), cornerPosition[1]);
            cornerPosition[2] = Math.max(corners[i].getZ() - this.getBlockPos().getZ(), cornerPosition[2]);

            cornerPosition[3] = Math.min(corners[i].getX() - this.getBlockPos().getX(), cornerPosition[3]);
            cornerPosition[4] = Math.min(corners[i].getY() - this.getBlockPos().getY(), cornerPosition[4]);
            cornerPosition[5] = Math.min(corners[i].getZ() - this.getBlockPos().getZ(), cornerPosition[5]);
        }

        return cornerPosition;

    }

}
