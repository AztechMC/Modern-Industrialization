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
package aztech.modern_industrialization.machines.blockentities;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.TemperatureBar;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Tickable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;

public class BoilerMachineBlockEntity extends MachineBlockEntity implements Tickable {

    public static final int WATER_SLOT_X = 50;
    public static final int WATER_SLOT_Y = 32;

    public static final int INPUT_SLOT_X = 15;
    public static final int INPUT_SLOT_Y = 32;

    public static final int OUTPUT_SLOT_X = 134;
    public static final int OUTPUT_SLOT_Y = 32;

    private final MIInventory inventory;
    public final boolean bronze;

    private final SteamHeaterComponent steamHeater;
    private final FuelBurningComponent fuelBurning;

    protected IsActiveComponent isActiveComponent;

    public BoilerMachineBlockEntity(BEP bep, boolean bronze) {
        super(bep, new MachineGuiParameters.Builder(bronze ? "bronze_boiler" : "steel_boiler", true).backgroundHeight(180).build(),
                new OrientationComponent.Params(false, false, false));

        int capacity = FluidType.BUCKET_VOLUME * (bronze ? 8 : 16);

        List<ConfigurableItemStack> itemStacks = Collections.singletonList(ConfigurableItemStack.standardInputSlot());
        SlotPositions itemPositions = new SlotPositions.Builder().addSlot(INPUT_SLOT_X, INPUT_SLOT_Y).build();

        List<ConfigurableFluidStack> fluidStacks = Arrays.asList(ConfigurableFluidStack.lockedInputSlot(capacity, Fluids.WATER),
                ConfigurableFluidStack.lockedOutputSlot(capacity, MIFluids.STEAM.asFluid()));
        SlotPositions fluidPositions = new SlotPositions.Builder().addSlot(WATER_SLOT_X, WATER_SLOT_Y).addSlot(OUTPUT_SLOT_X, OUTPUT_SLOT_Y).build();
        inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);

        this.bronze = bronze;
        steamHeater = new SteamHeaterComponent(1500, bronze ? 8 : 16, 8, true, false, false);
        fuelBurning = new FuelBurningComponent(steamHeater, 1);
        this.isActiveComponent = new IsActiveComponent();

        ProgressBar.Parameters progressParams = new ProgressBar.Parameters(133, 50, "furnace", true);
        TemperatureBar.Parameters temperatureParams = new TemperatureBar.Parameters(42, 75, 1500);
        registerGuiComponent(new ProgressBar.Server(progressParams, () -> (float) fuelBurning.getBurningProgress()));
        registerGuiComponent(new TemperatureBar.Server(temperatureParams, () -> (int) steamHeater.getTemperature()));

        this.registerComponents(inventory, isActiveComponent, steamHeater, fuelBurning);

    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    protected MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData(bronze ? MachineCasings.BRICKED_BRONZE : MachineCasings.BRICKED_STEEL);
        data.isActive = isActiveComponent.isActive;
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void tick() {
        if (level.isClientSide)
            return;

        steamHeater.tick(Collections.singletonList(inventory.getFluidStacks().get(0)), Collections.singletonList(inventory.getFluidStacks().get(1)));
        fuelBurning.tick(Collections.singletonList(inventory.getItemStacks().get(0)), Collections.emptyList());

        for (Direction direction : Direction.values()) {
            getInventory().autoExtractFluids(level, worldPosition, direction);
        }

        isActiveComponent.updateActive(fuelBurning.isBurning(), this);

        setChanged();
    }

    @Override
    public List<Component> getTooltips() {
        return fuelBurning.getTooltips();
    }

}
