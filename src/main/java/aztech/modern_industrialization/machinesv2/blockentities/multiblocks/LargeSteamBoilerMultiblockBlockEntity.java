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
package aztech.modern_industrialization.machinesv2.blockentities.multiblocks;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machinesv2.IComponent;
import aztech.modern_industrialization.machinesv2.blockentities.BoilerMachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.IsActiveComponent;
import aztech.modern_industrialization.machinesv2.components.MultiblockInventoryComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.components.sync.TemperatureBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.machinesv2.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.util.ItemStackHelper;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;

public class LargeSteamBoilerMultiblockBlockEntity extends MultiblockMachineBlockEntity implements Tickable {

    private ShapeMatcher shapeMatcher;
    private final ShapeTemplate shapeTemplate;
    private boolean isShapeValid;
    private final IsActiveComponent isActiveComponent;

    private final MultiblockInventoryComponent inventory;

    private final int temperatureMax = 2100;
    protected float burningTick, burningTickProgress;
    protected int temperature;

    public static final int SPEED_FACTOR = 16;

    public LargeSteamBoilerMultiblockBlockEntity(BlockEntityType<?> type, ShapeTemplate shapeTemplate) {
        super(type, new MachineGuiParameters.Builder("large_steam_boiler", false).build(),
                new OrientationComponent(new OrientationComponent.Params(false, false, false)));

        isActiveComponent = new IsActiveComponent();
        this.shapeTemplate = shapeTemplate;
        this.inventory = new MultiblockInventoryComponent();

        ProgressBar.Parameters PROGRESS_BAR = new ProgressBar.Parameters(82, 20, "furnace", true);
        TemperatureBar.Parameters TEMPERATURE_BAR = new TemperatureBar.Parameters(42, 55, temperatureMax);

        registerClientComponent(new ProgressBar.Server(PROGRESS_BAR, () -> burningTick / burningTickProgress));
        registerClientComponent(new TemperatureBar.Server(TEMPERATURE_BAR, () -> temperature));

        this.registerComponents(isActiveComponent, new IComponent() {

            @Override
            public void readNbt(CompoundTag tag) {
                burningTick = tag.getFloat("burningTick");
                burningTickProgress = tag.getFloat("burningTickProgress");
                temperature = tag.getInt("temperature");
            }

            @Override
            public void writeNbt(CompoundTag tag) {
                tag.putFloat("burningTick", burningTick);
                tag.putFloat("burningTickProgress", burningTickProgress);
                tag.putInt("temperature", temperature);
            }
        });

    }

    protected final void link() {
        if (shapeMatcher == null) {
            shapeMatcher = new ShapeMatcher(world, pos, orientation.facingDirection, shapeTemplate);
            shapeMatcher.registerListeners(world);
        }
        if (shapeMatcher.needsRematch()) {
            isShapeValid = false;
            shapeMatcher.rematch(world);

            if (shapeMatcher.isMatchSuccessful()) {
                inventory.rebuild(shapeMatcher);
                isShapeValid = true;
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

            if (isShapeValid) {
                boolean empty = false;
                while (burningTick < 12.5 * 20 & !empty) {
                    empty = true;
                    for (ConfigurableItemStack stack : inventory.getItemInputs()) {
                        Item fuel = stack.getItemKey().getItem();

                        if (ItemStackHelper.consumeFuel(stack, true)) {
                            Integer fuelTime = FuelRegistryImpl.INSTANCE.get(fuel);
                            if (fuelTime != null && fuelTime > 0) {
                                burningTickProgress = burningTick + (fuelTime * BoilerMachineBlockEntity.BURN_TIME_MULTIPLIER) / (2f * SPEED_FACTOR);
                                burningTick = burningTickProgress;
                                empty = false;
                                ItemStackHelper.consumeFuel(stack, false);
                                break;
                            }
                        }
                    }
                }
                empty = false;
                while (burningTick < 12.5 * 20 & !empty) {
                    empty = true;
                    for (ConfigurableFluidStack stack : inventory.getFluidInputs()) {
                        if (!stack.isEmpty()) {
                            long euPerMb = FluidFuelRegistry.getEu(stack.getFluid());
                            if (euPerMb != 0) {
                                float burningTickProgressPerMb = euPerMb / (SPEED_FACTOR * 8f);
                                long mbConsumedMax = (long) Math.ceil((12.5 * 20 - burningTick) / burningTickProgressPerMb);
                                long dropletConsumedMax = Math.min(mbConsumedMax * 81, stack.getAmount());
                                stack.decrement(dropletConsumedMax);
                                burningTickProgress = burningTick + (dropletConsumedMax * burningTickProgressPerMb / 81f);
                                burningTick = burningTickProgress;
                                empty = false;
                                break;
                            }
                        }
                    }

                }

                boolean newActive = burningTick >= 1;
                burningTick = Math.max(burningTick - 1, 0);

                if (newActive) {
                    temperature = Math.min(temperature + 1, temperatureMax);
                } else {
                    temperature = Math.max(temperature - 1, 0);
                }

                if (temperature > 100) {
                    long steamProduction = 81 * ((SPEED_FACTOR * 8 * (temperature - 100)) / 1000);
                    long maxWaterExtract = 0;

                    for (ConfigurableFluidStack fluidStack : inventory.getFluidInputs()) {
                        if (fluidStack.getFluid() == Fluids.WATER) {
                            maxWaterExtract += fluidStack.getAmount();
                        }
                    }

                    long maxInsertSteam = 0;

                    for (ConfigurableFluidStack fluidStack : inventory.getFluidOutputs()) {
                        if (fluidStack.isValid(MIFluids.STEAM)) {
                            maxInsertSteam += fluidStack.getRemainingSpace();
                        }
                    }

                    long effSteamProduced = Math.min(Math.min(steamProduction, maxWaterExtract), maxInsertSteam);

                    long waterExtract = effSteamProduced;
                    for (ConfigurableFluidStack fluidStack : inventory.getFluidInputs()) {
                        if (fluidStack.getFluid() == Fluids.WATER) {
                            long decrement = Math.min(fluidStack.getAmount(), waterExtract);
                            waterExtract -= decrement;
                            fluidStack.decrement(decrement);
                        }
                    }

                    long steamInsert = effSteamProduced;

                    for (ConfigurableFluidStack fluidStack : inventory.getFluidOutputs()) {
                        if (fluidStack.isValid(MIFluids.STEAM)) {
                            long increment = Math.min(fluidStack.getRemainingSpace(), steamInsert);
                            steamInsert -= increment;
                            fluidStack.setFluid(MIFluids.STEAM);
                            fluidStack.increment(increment);

                        }
                    }
                }

                isActiveComponent.updateActive(newActive, this);
            } else {
                this.burningTick = 0;
                temperature = Math.max(temperature - 1, 0);
            }
            markDirty();
        }
    }
}
