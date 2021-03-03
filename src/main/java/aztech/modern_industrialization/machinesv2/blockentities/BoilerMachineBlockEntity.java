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

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.IComponent;
import aztech.modern_industrialization.machinesv2.components.IsActiveComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.components.sync.TemperatureBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.helper.OrientationHelper;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.ItemStackHelper;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

public class BoilerMachineBlockEntity extends MachineBlockEntity implements Tickable {

    private static final int BURN_TIME_MULTIPLIER = 5;

    public static final int WATER_SLOT_X = 50;
    public static final int WATER_SLOT_Y = 32;

    public static final int INPUT_SLOT_X = 15;
    public static final int INPUT_SLOT_Y = 32;

    public static final int OUTPUT_SLOT_X = 134;
    public static final int OUTPUT_SLOT_Y = 32;

    private final MIInventory inventory;
    private final boolean bronze;

    private final int temperatureMax;
    protected int burningTick, burningTickProgress, temperature;

    protected final OrientationComponent orientation;
    protected final ProgressBar.Parameters PROGRESS_BAR;
    protected final TemperatureBar.Parameters TEMPERATURE_BAR;

    protected IsActiveComponent isActiveComponent;

    public BoilerMachineBlockEntity(BlockEntityType<?> type, boolean bronze) {
        super(type, new MachineGuiParameters.Builder(bronze ? "bronze_boiler" : "steel_boiler", true).backgroundHeight(180).build());
        orientation = new OrientationComponent(new OrientationComponent.Params(true, false, false));

        int capacity = 81000 * (bronze ? 2 * MITanks.BRONZE.bucketCapacity : 2 * MITanks.STEEL.bucketCapacity);

        List<ConfigurableItemStack> itemStacks = Arrays.asList(ConfigurableItemStack.standardInputSlot());
        SlotPositions itemPositions = new SlotPositions.Builder().addSlot(INPUT_SLOT_X, INPUT_SLOT_Y).build();

        List<ConfigurableFluidStack> fluidStacks = Arrays.asList(ConfigurableFluidStack.lockedInputSlot(capacity, Fluids.WATER),
                ConfigurableFluidStack.lockedOutputSlot(capacity, MIFluids.STEAM));
        SlotPositions fluidPositions = new SlotPositions.Builder().addSlot(WATER_SLOT_X, WATER_SLOT_Y).addSlot(OUTPUT_SLOT_X, OUTPUT_SLOT_Y).build();
        inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);

        this.bronze = bronze;
        this.burningTickProgress = 1;
        this.burningTick = 0;
        this.temperatureMax = bronze ? 1100 : 2100;
        this.isActiveComponent = new IsActiveComponent();

        PROGRESS_BAR = new ProgressBar.Parameters(133, 50, "furnace", true);
        TEMPERATURE_BAR = new TemperatureBar.Parameters(42, 75, temperatureMax);
        registerClientComponent(new ProgressBar.Server(PROGRESS_BAR, () -> (float) burningTick / burningTickProgress));
        registerClientComponent(new TemperatureBar.Server(TEMPERATURE_BAR, () -> temperature));

        this.registerComponents(orientation, inventory, isActiveComponent, new IComponent() {

            @Override
            public void readNbt(CompoundTag tag) {
                burningTick = tag.getInt("burningTick");
                burningTickProgress = tag.getInt("burningTickProgress");
                temperature = tag.getInt("temperature");
            }

            @Override
            public void writeNbt(CompoundTag tag) {
                tag.putInt("burningTick", burningTick);
                tag.putInt("burningTickProgress", burningTickProgress);
                tag.putInt("temperature", temperature);
            }
        });

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
        MachineModelClientData data = new MachineModelClientData(bronze ? MachineCasings.BRICKED_BRONZE : MachineCasings.BRICKED_STEEL);
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
        if (world.isClient)
            return;

        boolean wasActive = isActiveComponent.isActive;

        isActiveComponent.isActive = false;
        if (burningTick == 0) {
            ConfigurableItemStack stack = inventory.itemStacks.get(0);
            Item fuel = stack.getItemKey().getItem();
            if (ItemStackHelper.consumeFuel(stack, true)) {
                Integer fuelTime = FuelRegistryImpl.INSTANCE.get(fuel);
                if (fuelTime != null && fuelTime > 0) {
                    burningTickProgress = fuelTime * BURN_TIME_MULTIPLIER / (bronze ? 1 : 2);
                    burningTick = burningTickProgress;
                    ItemStackHelper.consumeFuel(stack, false);
                }
            }
        }

        if (burningTick > 0) {
            isActiveComponent.isActive = true;
            --burningTick;
        }

        if (isActiveComponent.isActive) {
            temperature = Math.min(temperature + 1, temperatureMax);
        } else {
            temperature = Math.max(temperature - 1, 0);
        }

        if (temperature > 100) {
            int steamProduction = 81 * ((4 * (temperature - 100)) / 1000);
            if (inventory.fluidStacks.get(0).getAmount() > 0) {
                long remSpace = inventory.fluidStacks.get(1).getRemainingSpace();
                long waterAvail = inventory.fluidStacks.get(0).getAmount();
                long actualProduced = Math.min(Math.min(steamProduction, remSpace), waterAvail);
                if (actualProduced > 0) {
                    inventory.fluidStacks.get(1).increment(actualProduced);
                    inventory.fluidStacks.get(0).decrement(actualProduced);
                }
            }
        }

        for (Direction direction : Direction.values()) {
            getInventory().autoExtractFluids(world, pos, direction);
        }

        if (isActiveComponent.isActive != wasActive) {
            sync();
        }
        markDirty();
    }
}
