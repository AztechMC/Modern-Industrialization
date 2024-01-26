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
package aztech.modern_industrialization.machines.blockentities.hatches;

import static aztech.modern_industrialization.machines.components.NeutronHistoryComponent.Type.*;
import static net.minecraft.core.Direction.UP;

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.TemperatureBar;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.nuclear.*;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import com.google.common.base.Preconditions;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class NuclearHatch extends HatchBlockEntity implements INuclearTile {

    private final MIInventory inventory;

    public final NeutronHistoryComponent neutronHistory;
    public final TemperatureComponent nuclearReactorComponent;
    public final boolean isFluid;
    public static final long capacity = 64 * FluidType.BUCKET_VOLUME;

    public NuclearHatch(BEP bep, boolean isFluid) {
        super(bep, new MachineGuiParameters.Builder(isFluid ? "nuclear_fluid_hatch" : "nuclear_item_hatch", true).build(),
                new OrientationComponent.Params(false, false, false));

        this.isFluid = isFluid;
        SlotPositions slotPos = new SlotPositions.Builder().addSlot(68, 31).addSlots(98, 22, 1, 2).build();

        if (!isFluid) {
            List<ConfigurableItemStack> itemStack = new ArrayList<>();
            itemStack.add(ConfigurableItemStack.standardInputSlot());
            itemStack.add(ConfigurableItemStack.standardOutputSlot());
            itemStack.add(ConfigurableItemStack.standardOutputSlot());
            inventory = new MIInventory(itemStack, Collections.emptyList(), slotPos, SlotPositions.empty());
            nuclearReactorComponent = new TemperatureComponent(NuclearConstant.MAX_TEMPERATURE);
        } else {

            List<ConfigurableFluidStack> fluidStack = new ArrayList<>();
            fluidStack.add(ConfigurableFluidStack.standardInputSlot(capacity));
            fluidStack.add(ConfigurableFluidStack.standardOutputSlot(capacity));
            fluidStack.add(ConfigurableFluidStack.standardOutputSlot(capacity));
            inventory = new MIInventory(Collections.emptyList(), fluidStack, SlotPositions.empty(), slotPos);
            nuclearReactorComponent = new SteamHeaterComponent(NuclearConstant.MAX_TEMPERATURE, NuclearConstant.MAX_HATCH_EU_PRODUCTION,
                    NuclearConstant.EU_PER_DEGREE, true, true, false);
        }

        neutronHistory = new NeutronHistoryComponent();
        registerComponents(inventory, nuclearReactorComponent, neutronHistory);

        TemperatureBar.Parameters temperatureParams = new TemperatureBar.Parameters(43, 63, NuclearConstant.MAX_TEMPERATURE);
        registerGuiComponent(new TemperatureBar.Server(temperatureParams, () -> (int) nuclearReactorComponent.getTemperature()));

    }

    @Override
    public HatchType getHatchType() {
        return isFluid ? HatchType.NUCLEAR_FLUID : HatchType.NUCLEAR_ITEM;
    }

    @Override
    public boolean upgradesToSteel() {
        return false;
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    public final void tick() {
        super.tick();
        this.clearMachineLock();

        if (isFluid) {
            fluidNeutronProductTick(1, true);
        } else {
            ItemVariant itemVariant = (ItemVariant) this.getVariant();
            if (!itemVariant.isBlank() && itemVariant.getItem() instanceof NuclearAbsorbable abs) {
                if (abs.getNeutronProduct() != null) {
                    try (Transaction tx = Transaction.openOuter()) {
                        this.inventory.itemStorage.insert(abs.getNeutronProduct(), abs.getNeutronProductAmount(), tx,
                                AbstractConfigurableStack::canPipesExtract, true);
                        tx.abort();
                    }
                }
            }
        }

    }

    @Override
    public double getTemperature() {
        return nuclearReactorComponent.getTemperature();
    }

    @Override
    public double getHeatTransferCoeff() {
        @Nullable
        INuclearComponent<?> component = getComponent();

        return Math.max(NuclearConstant.BASE_HEAT_CONDUCTION + (component != null ? component.getHeatConduction() : 0), 0);
    }

    @Override
    public double getMeanNeutronAbsorption(NeutronType type) {
        return neutronHistory.getAverageReceived(type);
    }

    @Override
    public double getMeanNeutronFlux(NeutronType type) {
        return neutronHistory.getAverageFlux(type);
    }

    @Override
    public double getMeanNeutronGeneration() {
        return neutronHistory.getAverageGeneration();
    }

    @Override
    public double getMeanEuGeneration() {
        return neutronHistory.getAverageEuGeneration();
    }

    @Override
    public TransferVariant getVariant() {
        if (isFluid) {
            return this.inventory.getFluidStacks().get(0).getResource();
        } else {
            return this.inventory.getItemStacks().get(0).getResource();
        }
    }

    @Override
    public long getVariantAmount() {
        if (isFluid) {
            return this.inventory.getFluidStacks().get(0).getAmount();
        } else {
            return this.inventory.getItemStacks().get(0).getAmount();
        }
    }

    @Override
    public boolean isFluid() {
        return isFluid;
    }

    @Override
    public void setTemperature(double temp) {
        nuclearReactorComponent.setTemperature(temp);
    }

    @Override
    public void putHeat(double eu) {
        Preconditions.checkArgument(eu >= 0);
        setTemperature(getTemperature() + eu / NuclearConstant.EU_PER_DEGREE);
        neutronHistory.addValue(euGeneration, (int) eu);
    }

    @Override
    public int neutronGenerationTick(NuclearEfficiencyHistoryComponent efficiencyHistory) {
        double meanNeutron = getMeanNeutronAbsorption(NeutronType.BOTH);
        int neutronsProduced = 0;

        if (!isFluid) {
            ItemVariant itemVariant = (ItemVariant) this.getVariant();

            if (!itemVariant.isBlank() && itemVariant.getItem() instanceof NuclearAbsorbable abs) {

                if (itemVariant.getItem() instanceof NuclearFuel) {
                    meanNeutron += NuclearConstant.BASE_NEUTRON;
                }

                ItemStack stack = itemVariant.toStack((int) getVariantAmount());

                var rand = this.level.getRandom();

                if (abs instanceof NuclearFuel fuel) {
                    neutronsProduced = fuel.simulateDesintegration(meanNeutron, stack, this.nuclearReactorComponent.getTemperature(), rand,
                            efficiencyHistory);
                } else {
                    abs.simulateAbsorption(meanNeutron, stack, rand);
                }

                if (abs.getRemainingDesintegrations(stack) == 0) {
                    try (Transaction tx = Transaction.openOuter()) {
                        ConfigurableItemStack absStack = this.inventory.getItemStacks().get(0);
                        absStack.updateSnapshots(tx);
                        absStack.setAmount(0);
                        absStack.setKey(ItemVariant.blank());

                        if (abs.getNeutronProduct() != null) {
                            long inserted = this.inventory.itemStorage.insert(abs.getNeutronProduct(), abs.getNeutronProductAmount(), tx,
                                    AbstractConfigurableStack::canPipesExtract, true);

                            if (inserted == abs.getNeutronProductAmount()) {
                                tx.commit();
                            } else {
                                tx.abort();
                            }
                        } else {
                            tx.commit();
                        }
                    }
                } else {
                    this.getInventory().getItemStacks().get(0).setKey(ItemVariant.of(stack));
                }

            }

            neutronHistory.addValue(neutronGeneration, neutronsProduced);
            return neutronsProduced;
        } else {
            return 0;
        }
    }

    private static int randIntFromDouble(double value, RandomSource rand) {
        return (int) Math.floor(value) + (rand.nextDouble() < (value % 1) ? 1 : 0);
    }

    public void fluidNeutronProductTick(int neutron, boolean simul) {
        if (isFluid) {
            @Nullable
            INuclearComponent<FluidVariant> component = (INuclearComponent<FluidVariant>) this.getComponent();

            if (component == null) {
                return;
            }

            // Divide by 81 because the reactor was balanced back when the base fluid unit was 1/81000, but now it's 1/1000.
            // TODO NEO: maybe reconsider this?
            double fluidConsumption = neutron * component.getNeutronProductProbability() * 1 / 81.0;
            int actualRecipe = randIntFromDouble(fluidConsumption, this.getLevel().getRandom());

            if (simul) {
                actualRecipe = neutron;
            }

            if (simul || actualRecipe > 0) {
                try (Transaction tx = Transaction.openOuter()) {
                    long extracted = this.inventory.fluidStorage.extractAllSlot(component.getVariant(), actualRecipe, tx,
                            AbstractConfigurableStack::canPipesInsert);
                    this.inventory.fluidStorage.insert(component.getNeutronProduct(), extracted * component.getNeutronProductAmount(), tx,
                            AbstractConfigurableStack::canPipesExtract, true);

                    if (!simul) {
                        tx.commit();
                    }
                }
            }
        }
    }

    private void checkComponentMaxTemperature() {
        if (!isFluid) {
            @Nullable
            INuclearComponent<?> component = this.getComponent();

            if (component != null) {
                if (component.getMaxTemperature() < this.getTemperature()) {
                    this.inventory.getItemStacks().get(0).empty();
                }
            }
        }
    }

    @Override
    public void nuclearTick(NuclearEfficiencyHistoryComponent efficiencyHistory) {
        neutronHistory.tick();
        fluidNeutronProductTick(randIntFromDouble(neutronHistory.getAverageReceived(NeutronType.BOTH), this.getLevel().getRandom()), false);

        if (isFluid) {
            double euProduced = ((SteamHeaterComponent) nuclearReactorComponent).tick(Collections.singletonList(inventory.getFluidStacks().get(0)),
                    inventory.getFluidStacks().stream().filter(AbstractConfigurableStack::canPipesExtract).collect(Collectors.toList()));
            efficiencyHistory.registerEuProduction(euProduced);
        }

        checkComponentMaxTemperature();
    }

    @Override
    public void absorbNeutrons(int neutronNumber, NeutronType type) {
        Preconditions.checkArgument(type != NeutronType.BOTH);
        if (type == NeutronType.FAST) {
            neutronHistory.addValue(fastNeutronReceived, neutronNumber);
        } else {
            neutronHistory.addValue(thermalNeutronReceived, neutronNumber);
        }

    }

    @Override
    public void addNeutronsToFlux(int neutronNumber, NeutronType type) {
        Preconditions.checkArgument(type != NeutronType.BOTH);
        if (type == NeutronType.FAST) {
            neutronHistory.addValue(fastNeutronFlux, neutronNumber);
        } else {
            neutronHistory.addValue(thermalNeutronFlux, neutronNumber);
        }
    }

    public static void registerItemApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, bet,
                    (be, direction) -> direction == UP ? ((NuclearHatch) be).getInventory().itemStorage.itemHandler : null);
        });
    }

    public static void registerFluidApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, bet,
                    (be, direction) -> direction == UP ? ((NuclearHatch) be).getInventory().fluidStorage.fluidHandler : null);
        });
    }

    @Override
    public List<Component> getTooltips() {
        if (isFluid) {
            return List.of(new MITooltips.Line(MIText.MaxEuProductionSteam).arg(
                    NuclearConstant.MAX_HATCH_EU_PRODUCTION,
                    MITooltips.EU_PER_TICK_PARSER).arg(MIFluids.STEAM).build(),
                    new MITooltips.Line(MIText.AcceptLowAndHighPressure).arg(Fluids.WATER).arg(MIFluids.HEAVY_WATER)
                            .arg(MIFluids.HIGH_PRESSURE_WATER).arg(MIFluids.HIGH_PRESSURE_HEAVY_WATER).build());
        } else {
            return Collections.emptyList();
        }
    }

}
