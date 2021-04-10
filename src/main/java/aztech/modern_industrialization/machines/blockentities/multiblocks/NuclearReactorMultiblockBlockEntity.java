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
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.components.ActiveShapeComponent;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

public class NuclearReactorMultiblockBlockEntity extends MultiblockMachineBlockEntity implements Tickable {

    private static final ShapeTemplate[] shapeTemplates;

    private final ActiveShapeComponent activeShape;
    private final IsActiveComponent isActive;
    private ShapeMatcher shapeMatcher;
    private boolean allowNormalOperation;

    public NuclearReactorMultiblockBlockEntity(BlockEntityType<?> type) {
        super(type, new MachineGuiParameters.Builder("nuclear_reactor", false).backgroundHeight(256).build(),
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
        }
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
            allowNormalOperation = false;
            shapeValid.shapeValid = false;
            shapeMatcher.rematch(world);

            if (shapeMatcher.isMatchSuccessful()) {
                shapeValid.shapeValid = true;
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

        SimpleMember casing = SimpleMember.forBlock(MIBlock.blocks.get("nuclear_casing"));
        SimpleMember pipe = SimpleMember.forBlock(MIBlock.blocks.get("nuclear_alloy_machine_casing_pipe"));
        HatchFlags top = new HatchFlags.Builder().with(HatchType.NUCLEAR_FLUID, HatchType.NUCLEAR_ITEM).build();
        for (int i = 0; i < 4; i++) {
            ShapeTemplate.Builder builder = new ShapeTemplate.Builder(MachineCasings.NUCLEAR);
            for (int x = -2 - i; x <= 2 + i; x++) {
                int minZ;
                int xabs = Math.abs(x);
                if (i != 3) {
                    if (xabs == 0) {
                        minZ = 0;
                    } else {
                        minZ = xabs - 1;
                    }
                } else {
                    if (xabs <= 1) {
                        minZ = 0;
                    } else {
                        minZ = xabs - 2;
                    }
                }

                int maxZ = 2 * (2 + i) - minZ;

                for (int z = minZ; z <= maxZ; z++) {
                    if (!(z == minZ || z == maxZ || xabs == 2 + i)) {
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
