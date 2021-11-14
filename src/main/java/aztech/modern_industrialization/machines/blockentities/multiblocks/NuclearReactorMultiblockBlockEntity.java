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
import aztech.modern_industrialization.api.ScrewdriverableBlockEntity;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.hatches.NuclearHatch;
import aztech.modern_industrialization.machines.components.ActiveShapeComponent;
import aztech.modern_industrialization.machines.components.IntegerHistoryComponent;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.sync.NuclearReactorGui;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.nuclear.*;
import aztech.modern_industrialization.util.Tickable;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class NuclearReactorMultiblockBlockEntity extends MultiblockMachineBlockEntity implements Tickable, ScrewdriverableBlockEntity {

    private static final ShapeTemplate[] shapeTemplates;
    /**
     * For every possible shape, contains true if the position is within the bounds
     * for the shape.
     */
    private static final boolean[][][] gridLayout;

    private final ActiveShapeComponent activeShape;
    private final IsActiveComponent isActive;
    private final IntegerHistoryComponent efficiencyHistory;
    private ShapeMatcher shapeMatcher;

    private INuclearGrid nuclearGrid;
    private Supplier<NuclearReactorGui.Data> dataSupplier;

    public NuclearReactorMultiblockBlockEntity(BEP bep) {
        super(bep, new MachineGuiParameters.Builder("nuclear_reactor", false).backgroundHeight(256).build(),
                new OrientationComponent.Params(false, false, false));

        this.activeShape = new ActiveShapeComponent(shapeTemplates);
        efficiencyHistory = new IntegerHistoryComponent(new String[] { "euProduction", "euFuelConsumption" }, 300);
        isActive = new IsActiveComponent();
        registerComponents(activeShape, isActive, efficiencyHistory);
        this.registerClientComponent(new NuclearReactorGui.Server(this::sendData));

    }

    public NuclearReactorGui.Data sendData() {
        if (shapeValid.shapeValid) {
            return dataSupplier.get();
        } else {
            return new NuclearReactorGui.Data(false, 0, 0, null, 0, 0);
        }
    }

    @Override
    public boolean useScrewdriver(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        return useScrewdriver(activeShape, player, hand, hitResult);
    }

    @Override
    public MIInventory getInventory() {
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
    public void tick() {
        if (!world.isClient) {
            link();
            if (shapeValid.shapeValid) {
                isActive.updateActive(NuclearGridHelper.simulate(nuclearGrid), this);
                efficiencyHistory.tick();
            } else {
                isActive.updateActive(false, this);
                efficiencyHistory.clear();
            }
        }
    }

    protected void onSuccessfulMatch(ShapeMatcher shapeMatcher) {
        shapeValid.shapeValid = true;
        int size = gridLayout[activeShape.getActiveShapeIndex()].length;
        NuclearHatch[][] hatchesGrid = new NuclearHatch[size][size];

        for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
            int x0 = hatch.getPos().getX() - getPos().getX();
            int z0 = hatch.getPos().getZ() - getPos().getZ();

            int x, y;

            if (orientation.facingDirection == Direction.NORTH) {
                x = size / 2 + x0;
                y = z0;
            } else if (orientation.facingDirection == Direction.SOUTH) {
                x = size / 2 - x0;
                y = -z0;
            } else if (orientation.facingDirection == Direction.EAST) {
                x = size / 2 + z0;
                y = -x0;

            } else {
                x = size / 2 - z0;
                y = x0;
            }

            hatchesGrid[x][y] = (NuclearHatch) hatch;
        }

        nuclearGrid = new INuclearGrid() {

            @Override
            public int getSizeX() {
                return size;
            }

            @Override
            public int getSizeY() {
                return size;
            }

            @Override
            public Optional<INuclearTile> getNuclearTile(int x, int y) {
                return Optional.ofNullable(hatchesGrid[x][y]);
            }

            @Override
            public void registerNeutronFate(int neutronNumber, NeutronType type, NeutronFate escape) {
            }

            @Override
            public void registerNeutronCreation(int neutronNumber, NeutronType type) {
            }

            @Override
            public void registerEuFuelConsumption(double eu) {
                efficiencyHistory.addValue("euFuelConsumption", (int) eu);

            }

            @Override
            public void registerEuProduction(double eu) {
                efficiencyHistory.addValue("euProduction", (int) eu);

            }

        };

        dataSupplier = () -> {
            Optional<INuclearTileData>[] tilesData = new Optional[size * size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {

                    final int x = size - 1 - i;
                    final int y = size - 1 - j;

                    int index = NuclearReactorGui.Data.toIndex(i, j, size);
                    tilesData[index] = Optional.ofNullable(hatchesGrid[x][y]);
                }
            }
            return new NuclearReactorGui.Data(true, size, size, tilesData, efficiencyHistory.getAverage("euProduction"),
                    efficiencyHistory.getAverage("euFuelConsumption"));
        };
    }

    @Override
    public ShapeTemplate getActiveShape() {
        return activeShape.getActiveShape();
    }

    protected final void link() {
        if (shapeMatcher == null) {
            shapeMatcher = new ShapeMatcher(world, pos, orientation.facingDirection, getActiveShape());
            shapeMatcher.registerListeners(world);
        }
        if (shapeMatcher.needsRematch()) {
            shapeValid.shapeValid = false;
            nuclearGrid = null;
            shapeMatcher.rematch(world);

            if (shapeMatcher.isMatchSuccessful()) {
                shapeValid.shapeValid = true;
                onSuccessfulMatch(shapeMatcher);
            }

            if (shapeValid.update()) {
                sync(false);
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

    public static void registerReiShapes() {
        for (ShapeTemplate shapeTemplate : shapeTemplates) {
            ReiMachineRecipes.registerMultiblockShape("nuclear_reactor", shapeTemplate);
        }
    }

    static {

        shapeTemplates = new ShapeTemplate[4];
        gridLayout = new boolean[4][][];

        SimpleMember casing = SimpleMember.forBlock(MIBlock.blocks.get("nuclear_casing"));
        SimpleMember pipe = SimpleMember.forBlock(MIBlock.blocks.get("nuclear_alloy_machine_casing_pipe"));
        HatchFlags top = new HatchFlags.Builder().with(HatchType.NUCLEAR_FLUID, HatchType.NUCLEAR_ITEM).build();
        for (int i = 0; i < 4; i++) {
            ShapeTemplate.Builder builder = new ShapeTemplate.Builder(MachineCasings.NUCLEAR);
            gridLayout[i] = new boolean[5 + 2 * i][5 + 2 * i];
            for (int x = -2 - i; x <= 2 + i; x++) {
                int minZ;
                int xAbs = Math.abs(x);
                if (i != 3) {
                    if (xAbs == 0) {
                        minZ = 0;
                    } else {
                        minZ = xAbs - 1;
                    }
                } else {
                    if (xAbs <= 1) {
                        minZ = 0;
                    } else {
                        minZ = xAbs - 2;
                    }
                }

                int maxZ = 2 * (2 + i) - minZ;

                for (int z = minZ; z <= maxZ; z++) {
                    gridLayout[i][2 + i + x][z] = true;
                    if (!(z == minZ || z == maxZ || xAbs == 2 + i)) {
                        builder.add(x, -1, z, casing, null);
                        builder.add(x, 0, z, pipe, null);
                        builder.add(x, 1, z, pipe, null);
                        builder.add(x, 2, z, pipe, null);
                        builder.add(x, 3, z, casing, top);

                    } else {
                        builder.add(x, -1, z, casing, null);
                        if (x != 0 || z != 0)
                            builder.add(x, 0, z, casing, null);
                        builder.add(x, 1, z, casing, null);
                        builder.add(x, 2, z, casing, null);
                        builder.add(x, 3, z, casing, null);
                    }

                }
            }
            shapeTemplates[i] = builder.build();
        }
    }
}
