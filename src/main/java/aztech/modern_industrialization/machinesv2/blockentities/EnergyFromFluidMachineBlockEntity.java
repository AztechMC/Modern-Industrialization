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
package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.EnergyComponent;
import aztech.modern_industrialization.machinesv2.components.IsActiveComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.components.sync.EnergyBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.helper.EnergyHelper;
import aztech.modern_industrialization.machinesv2.helper.OrientationHelper;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

public class EnergyFromFluidMachineBlockEntity extends MachineBlockEntity implements Tickable {

    private final CableTier outputTier;
    private final long fluidConsumption;
    private final EnergyExtractable extractable;

    private final Predicate<Fluid> acceptedFluid;
    private final ToLongFunction<Fluid> fluidEUperMb;

    protected final MIInventory inventory;
    protected EnergyComponent energy;
    protected OrientationComponent orientation;
    protected IsActiveComponent isActiveComponent;

    private EnergyFromFluidMachineBlockEntity(BlockEntityType<?> type, String name, CableTier outputTier, long energyCapacity, long fluidCapacity,
            long fluidConsumption, Predicate<Fluid> acceptedFluid, ToLongFunction<Fluid> fluidEUperMb, Fluid locked) {
        super(type, new MachineGuiParameters.Builder(name, false).build());
        this.outputTier = outputTier;
        this.energy = new EnergyComponent(energyCapacity);
        this.extractable = energy.buildExtractable((CableTier tier) -> tier == outputTier);
        this.fluidConsumption = fluidConsumption;
        EnergyBar.Parameters energyBarParams = new EnergyBar.Parameters(76, 39);
        registerClientComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));
        this.orientation = new OrientationComponent(new OrientationComponent.Params(true, false, false));
        this.isActiveComponent = new IsActiveComponent();

        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        SlotPositions itemPositions = SlotPositions.empty();

        this.acceptedFluid = acceptedFluid;
        this.fluidEUperMb = fluidEUperMb;

        List<ConfigurableFluidStack> fluidStacks;
        if (locked == null) {
            fluidStacks = Collections.singletonList(ConfigurableFluidStack.standardInputSlot(81 * fluidCapacity));
        } else {
            fluidStacks = Collections.singletonList(ConfigurableFluidStack.lockedInputSlot(81 * fluidCapacity, locked));
        }

        SlotPositions fluidPositions = new SlotPositions.Builder().addSlot(25, 38).build();
        inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);

        this.registerComponents(energy, orientation, isActiveComponent, inventory);

    }

    public EnergyFromFluidMachineBlockEntity(BlockEntityType<?> type, String name, CableTier outputTier, long energyCapacity, long fluidCapacity,
            long fluidConsumption, Predicate<Fluid> acceptedFluid, ToLongFunction<Fluid> fluidEUperMb) {
        this(type, name, outputTier, energyCapacity, fluidCapacity, fluidConsumption, acceptedFluid, fluidEUperMb, null);
    }

    public EnergyFromFluidMachineBlockEntity(BlockEntityType<?> type, String name, CableTier outputTier, long energyCapacity, long fluidCapacity,
            long fluidConsumption, Fluid acceptedFluid, long fluidEUperMb) {
        this(type, name, outputTier, energyCapacity, fluidCapacity, fluidConsumption, (Fluid f) -> (f == acceptedFluid), (Fluid f) -> (fluidEUperMb),
                acceptedFluid);
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        return OrientationHelper.onUse(player, hand, face, orientation, this);
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData(MachineCasings.casingFromCableTier(outputTier));
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

        boolean wasActive = isActiveComponent.isActive;
        ConfigurableFluidStack stack = inventory.fluidStacks.get(0);

        if (acceptedFluid.test(stack.getFluid())) {
            long fuelEu = fluidEUperMb.applyAsLong(stack.getFluid());
            long fluidConsumed = Math.min(Math.min(energy.getRemainingCapacity() / fuelEu, stack.getAmount() / 81), this.fluidConsumption);
            if (fluidConsumed > 0) {
                stack.decrement(81 * fluidConsumed);
                energy.insertEu(fluidConsumed * fuelEu, Simulation.ACT);
                isActiveComponent.isActive = true;
            } else {
                isActiveComponent.isActive = false;
            }

        } else {
            isActiveComponent.isActive = false;
        }

        EnergyHelper.autoOuput(this, orientation, outputTier, energy);

        if (wasActive != isActiveComponent.isActive) {
            sync();
        }
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
