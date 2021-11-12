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

import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.components.sync.ProgressBar;
import aztech.modern_industrialization.machines.components.sync.TemperatureBar;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.util.Tickable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class SteamBoilerMultiblockBlockEntity extends MultiblockMachineBlockEntity implements Tickable {

    private ShapeMatcher shapeMatcher;
    private final ShapeTemplate shapeTemplate;
    private final IsActiveComponent isActiveComponent;

    private final MultiblockInventoryComponent inventory;

    private final SteamHeaterComponent steamHeater;
    private final FuelBurningComponent fuelBurning;

    public SteamBoilerMultiblockBlockEntity(BEP bep, ShapeTemplate shapeTemplate, String name, long maxEuProduction, boolean highPressure) {
        super(bep, new MachineGuiParameters.Builder(name, false).build(), new OrientationComponent.Params(false, false, false));

        isActiveComponent = new IsActiveComponent();
        this.shapeTemplate = shapeTemplate;
        this.inventory = new MultiblockInventoryComponent();

        ProgressBar.Parameters PROGRESS_BAR = new ProgressBar.Parameters(82, 30, "furnace", true);
        TemperatureBar.Parameters TEMPERATURE_BAR = new TemperatureBar.Parameters(42, 55, 2500);

        steamHeater = new SteamHeaterComponent(2500, maxEuProduction, maxEuProduction / 32, !highPressure, highPressure);
        fuelBurning = new FuelBurningComponent(steamHeater, 2);

        registerClientComponent(new ProgressBar.Server(PROGRESS_BAR, () -> (float) fuelBurning.getBurningProgress()));
        registerClientComponent(new TemperatureBar.Server(TEMPERATURE_BAR, () -> (int) steamHeater.getTemperature()));

        this.registerComponents(isActiveComponent, steamHeater, fuelBurning);

    }

    protected final void link() {
        if (shapeMatcher == null) {
            shapeMatcher = new ShapeMatcher(world, pos, orientation.facingDirection, shapeTemplate);
            shapeMatcher.registerListeners(world);
        }
        if (shapeMatcher.needsRematch()) {
            shapeValid.shapeValid = false;
            shapeMatcher.rematch(world);

            if (shapeMatcher.isMatchSuccessful()) {
                inventory.rebuild(shapeMatcher);
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

    @Override
    public ShapeTemplate getActiveShape() {
        return shapeTemplate;
    }

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    protected MachineModelClientData getModelData() {
        return new MachineModelClientData(null, orientation.facingDirection).active(isActiveComponent.isActive);
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            link();

            if (shapeValid.shapeValid) {
                steamHeater.tick(inventory.getFluidInputs(), inventory.getFluidOutputs());
                fuelBurning.tick(inventory.getItemInputs(), inventory.getFluidInputs());
                this.isActiveComponent.updateActive(fuelBurning.isBurning(), this);
            } else {
                fuelBurning.disable();
                steamHeater.decreaseTemperature(1);
                this.isActiveComponent.updateActive(false, this);
            }
            markDirty();
        }
    }
}
