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

import static aztech.modern_industrialization.nuclear.NuclearConstant.BASE_HEAT_CONDUCTION;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.hatches.NuclearHatch;
import aztech.modern_industrialization.machines.components.ActiveShapeComponent;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.*;
import aztech.modern_industrialization.nuclear.*;
import aztech.modern_industrialization.util.Tickable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class NuclearReactorMultiblockBlockEntity extends MultiblockMachineBlockEntity implements Tickable {

    private static final ShapeTemplate[] shapeTemplates;
    /**
     * For every possible shape, contains true if the position is within the bounds
     * for the shape.
     */
    private static final boolean[][][] gridLayout;

    private final ActiveShapeComponent activeShape;
    private final IsActiveComponent isActive;
    private ShapeMatcher shapeMatcher;

    private INuclearGrid nuclearGrid;

    public NuclearReactorMultiblockBlockEntity(BEP bep) {
        super(bep, new MachineGuiParameters.Builder("nuclear_reactor", false).backgroundHeight(256).build(),
                new OrientationComponent(new OrientationComponent.Params(false, false, false)));

        this.activeShape = new ActiveShapeComponent(shapeTemplates);
        isActive = new IsActiveComponent();
        registerComponents(activeShape, isActive);

    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        ActionResult result = activeShape.onUse(player, hand, face);
        if (result.isAccepted()) {
            if (!player.getEntityWorld().isClient()) {
                unlink();
                sync(false);
            }
            return result;
        }
        return super.onUse(player, hand, face);
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
                NuclearGridHelper.simulate(nuclearGrid);
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

            public ItemStack getNuclearComponent(int x, int y) {
                if (hatchesGrid[x][y] != null) {
                    if (!hatchesGrid[x][y].isFluid) {
                        ItemStack itemStack = hatchesGrid[x][y].getInventory().getItemStacks().get(0).getResource().toStack();
                        if (!itemStack.isEmpty() && itemStack.getItem() instanceof NuclearComponent) {
                            return itemStack;
                        }
                    }
                }
                return null;
            }

            public ItemStack getNuclearFuel(int x, int y) {
                ItemStack component = getNuclearComponent(x, y);
                if (component != null && component.getItem() instanceof NuclearFuel) {
                    return component;
                }
                return null;
            }

            @Override
            public int getSizeX() {
                return size;
            }

            @Override
            public int getSizeY() {
                return size;
            }

            @Override
            public boolean isFuel(int x, int y) {
                return getNuclearFuel(x, y) != null;
            }

            @Override
            public double getTemperature(int x, int y) {
                if (hatchesGrid[x][y] != null) {
                    return hatchesGrid[x][y].nuclearReactorComponent.getTemperature();
                }
                return 0;
            }

            @Override
            public boolean ok(int x, int y) {
                if (x >= 0 && y >= 0 & x < getSizeX() & y < getSizeY()) {
                    return gridLayout[activeShape.getActiveShapeIndex()][x][y];
                }
                return false;
            }

            @Override
            public double getHeatTransferCoeff(int x, int y) {
                if (hatchesGrid[x][y] == null) {
                    return 0;
                } else {
                    ItemStack stack = getNuclearComponent(x, y);
                    if (stack != null) {
                        return BASE_HEAT_CONDUCTION + ((NuclearComponent) stack.getItem()).heatConduction;
                    } else {
                        return BASE_HEAT_CONDUCTION;
                    }
                }
            }

            @Override
            public void setTemperature(int x, int y, double temp) {
                if (hatchesGrid[x][y] != null) {
                    hatchesGrid[x][y].nuclearReactorComponent.setTemperature(temp);
                }
            }

            @Override
            public int neutronProducedFromSimulation(int x, int y) {
                ItemStack nuclearFuel = getNuclearComponent(x, y);
                if (nuclearFuel != null) {
                    NuclearFuel item = (NuclearFuel) nuclearFuel.getItem();
                    int value = item.simulateDesintegration(hatchesGrid[x][y].neutronHistory.getAverage() + NuclearConstant.BASE_NEUTRON, nuclearFuel,
                            getWorld().random);

                    hatchesGrid[x][y].getInventory().getItemStacks().get(0).setKey(ItemVariant.of(nuclearFuel));

                    return value;
                }
                return 0;
            }

            @Override
            public double interactionTotalProbability(int x, int y, NeutronType type) {
                ItemStack component = getNuclearComponent(x, y);
                if (component != null) {
                    return ((NuclearComponent) component.getItem()).neutronBehaviour.interactionTotalProbability(type);
                }
                return 0;
            }

            @Override
            public double interactionRelativeProbability(int x, int y, NeutronType type, NeutronInteraction interaction) {
                ItemStack component = getNuclearComponent(x, y);
                if (component != null) {
                    return ((NuclearComponent) component.getItem()).neutronBehaviour.interactionRelativeProbability(type, interaction);
                }
                return 0;
            }

            @Override
            public void absorbNeutrons(int x, int y, NeutronType type, int neutronNumber) {
                if (hatchesGrid[x][y] != null) {
                    hatchesGrid[x][y].receiveNeutron(neutronNumber);
                }
            }

            @Override
            public void putHeat(int x, int y, int eu) {
                if (hatchesGrid[x][y] != null) {
                    hatchesGrid[x][y].nuclearReactorComponent.increaseTemperature((double) eu / NuclearHatch.EU_PER_DEGREE);
                }
            }

            @Override
            public void registerNeutronFate(int neutronNumber, NeutronType type, NeutronFate escape) {
            }

            @Override
            public void registerNeutronCreation(int neutronNumber, NeutronType type) {
            }

            @Override
            public int getSupplementaryHeatByNeutronGenerated(int x, int y) {
                ItemStack nuclearFuel = getNuclearComponent(x, y);
                if (nuclearFuel != null) {
                    NuclearFuel item = (NuclearFuel) nuclearFuel.getItem();
                    return item.directEUbyDesintegration;
                }
                return 0;
            }

            public void nuclearTick(int x, int y) {
                if (hatchesGrid[x][y] != null) {
                    hatchesGrid[x][y].nuclearTick();
                }
            }
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
