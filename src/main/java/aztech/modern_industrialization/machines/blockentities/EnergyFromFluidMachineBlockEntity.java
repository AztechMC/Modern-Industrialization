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

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import aztech.modern_industrialization.machines.components.FluidConsumerComponent;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.sync.EnergyBar;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.helper.EnergyHelper;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.Tickable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

public class EnergyFromFluidMachineBlockEntity extends MachineBlockEntity implements Tickable {

    private final CableTier outputTier;
    private final EnergyExtractable extractable;

    protected final MIInventory inventory;
    protected EnergyComponent energy;
    protected IsActiveComponent isActiveComponent;
    protected FluidConsumerComponent fluidConsumer;

    private EnergyFromFluidMachineBlockEntity(BEP bep, String name, CableTier outputTier, long energyCapacity, long fluidCapacity,
            long maxEnergyOutput, Predicate<Fluid> acceptedFluid, ToLongFunction<Fluid> fluidEUperMb, Fluid locked, boolean lockButton) {
        super(bep, new MachineGuiParameters.Builder(name, lockButton).build(), new OrientationComponent.Params(true, false, false));
        this.outputTier = outputTier;
        this.energy = new EnergyComponent(energyCapacity);
        this.extractable = energy.buildExtractable((CableTier tier) -> tier == outputTier);
        EnergyBar.Parameters energyBarParams = new EnergyBar.Parameters(76, 39);
        registerClientComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));
        this.isActiveComponent = new IsActiveComponent();

        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        SlotPositions itemPositions = SlotPositions.empty();

        List<ConfigurableFluidStack> fluidStacks;
        if (locked == null) {
            fluidStacks = Collections.singletonList(ConfigurableFluidStack.standardInputSlot(81 * fluidCapacity));
        } else {
            fluidStacks = Collections.singletonList(ConfigurableFluidStack.lockedInputSlot(81 * fluidCapacity, locked));
        }

        fluidConsumer = new FluidConsumerComponent(maxEnergyOutput, acceptedFluid, fluidEUperMb);
        SlotPositions fluidPositions = new SlotPositions.Builder().addSlot(25, 38).build();
        inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);

        this.registerComponents(energy, isActiveComponent, inventory, fluidConsumer);

    }

    public EnergyFromFluidMachineBlockEntity(BEP bep, String name, CableTier outputTier, long energyCapacity, long fluidCapacity,
            long maxEnergyOutput, Predicate<Fluid> acceptedFluid, ToLongFunction<Fluid> fluidEUperMb) {
        this(bep, name, outputTier, energyCapacity, fluidCapacity, maxEnergyOutput, acceptedFluid, fluidEUperMb, null, true);
    }

    public EnergyFromFluidMachineBlockEntity(BEP bep, String name, CableTier outputTier, long energyCapacity, long fluidCapacity,
            long maxEnergyOutput, Fluid acceptedFluid, long fluidEUperMb) {
        this(bep, name, outputTier, energyCapacity, fluidCapacity, maxEnergyOutput, (Fluid f) -> (f == acceptedFluid), (Fluid f) -> (fluidEUperMb),
                acceptedFluid, false);
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData();
        data.isActive = isActiveComponent.isActive;
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    @Override
    public void tick() {
        if (world == null || world.isClient)
            return;

        ConfigurableFluidStack stack = inventory.getFluidStacks().get(0);
        long euProduced = fluidConsumer.getEuProduction(Collections.singletonList(stack), energy.getRemainingCapacity());
        energy.insertEu(euProduced, Simulation.ACT);
        isActiveComponent.updateActive(0 != euProduced, this);

        EnergyHelper.autoOuput(this, orientation, outputTier, energy);

        markDirty();
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        EnergyApi.MOVEABLE.registerForBlockEntities((be, direction) -> {
            EnergyFromFluidMachineBlockEntity abe = (EnergyFromFluidMachineBlockEntity) be;
            if (abe.orientation.outputDirection == direction) {
                return abe.extractable;
            } else {
                return null;
            }
        }, bet);
    }
}
