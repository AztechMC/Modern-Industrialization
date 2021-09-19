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

import static net.minecraft.util.math.Direction.UP;

import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.NeutronHistoryComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.SteamHeaterComponent;
import aztech.modern_industrialization.machines.components.TemperatureComponent;
import aztech.modern_industrialization.machines.components.sync.TemperatureBar;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.nuclear.INuclearTile;
import aztech.modern_industrialization.nuclear.NeutronType;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;

public class NuclearHatch extends HatchBlockEntity implements INuclearTile {

    private final MIInventory inventory;

    public final NeutronHistoryComponent neutronHistory;
    public final TemperatureComponent nuclearReactorComponent;
    public final boolean isFluid;

    private int fastNeutronAbsorbedThisTick;
    private int thermalNeutronAbsorbedThisTick;
    private int fastNeutronInFluxThisTick;
    private int thermalNeutronInFluxThisTick;

    private int neutronGeneratedThisTick;

    public static final long capacity = 64000 * 81;

    public NuclearHatch(BEP bep, boolean isFluid) {
        super(bep, new MachineGuiParameters.Builder(isFluid ? "nuclear_fluid_hatch" : "nuclear_item_hatch", true).build(),
                new OrientationComponent.Params(false, false, false));

        this.isFluid = isFluid;
        SlotPositions slotPos = new SlotPositions.Builder().addSlot(68, 31).addSlots(98, 22, 2, 1).build();

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
            nuclearReactorComponent = new SteamHeaterComponent(NuclearConstant.MAX_TEMPERATURE, 4096, NuclearConstant.EU_PER_DEGREE, true, true);
        }

        neutronHistory = new NeutronHistoryComponent();
        registerComponents(inventory, nuclearReactorComponent, neutronHistory);

        TemperatureBar.Parameters temperatureParams = new TemperatureBar.Parameters(43, 63, NuclearConstant.MAX_TEMPERATURE);
        registerClientComponent(new TemperatureBar.Server(temperatureParams, () -> (int) nuclearReactorComponent.getTemperature()));

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
        if (isFluid) {
            ((SteamHeaterComponent) nuclearReactorComponent).tick(Collections.singletonList(inventory.getFluidStacks().get(0)),
                    Collections.singletonList(inventory.getFluidStacks().get(1)));
        } else {
            this.clearMachineLock();
            if (getFuel().isPresent()) {
                ItemStack stack = ((ItemVariant) getVariant()).toStack((int) getVariantAmount());
                NuclearFuel fuel = (NuclearFuel) stack.getItem();
                try (Transaction tx = Transaction.openOuter()) {
                    this.inventory.itemStorage.insert(ItemVariant.of(fuel.getDepleted()), fuel.size, tx, AbstractConfigurableStack::canPipesExtract,
                            true);
                    tx.abort();
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
        return NuclearConstant.BASE_HEAT_CONDUCTION + (getComponent().isPresent() ? getComponent().get().getHeatConduction() : 0);
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
    public int neutronGenerationTick() {
        double meanNeutron = getMeanNeutronAbsorption(NeutronType.BOTH) + NuclearConstant.BASE_NEUTRON;
        int neutronsProduced = 0;

        if (getFuel().isPresent()) {
            ItemStack stack = ((ItemVariant) getVariant()).toStack((int) getVariantAmount());
            neutronsProduced = getFuel().get().simulateDesintegration(meanNeutron, stack, this.world.getRandom());
            NuclearFuel fuel = (NuclearFuel) stack.getItem();
            if (fuel.getRemainingDesintegrations(stack) == 0) {
                try (Transaction tx = Transaction.openOuter()) {
                    ConfigurableItemStack fuelStack = this.inventory.getItemStacks().get(0);
                    long inserted = this.inventory.itemStorage.insert(ItemVariant.of(fuel.getDepleted()), fuel.size, tx,
                            AbstractConfigurableStack::canPipesExtract, true);

                    fuelStack.updateSnapshots(tx);
                    fuelStack.setAmount(0);
                    fuelStack.setKey(ItemVariant.blank());

                    if (inserted == fuel.size) {
                        tx.commit();
                    } else {
                        tx.abort();
                    }
                }
            } else {
                this.getInventory().getItemStacks().get(0).setKey(ItemVariant.of(stack));
            }
        }

        neutronGeneratedThisTick = neutronsProduced;

        return neutronsProduced;
    }

    public void nuclearTick() {

        neutronHistory.tick(fastNeutronAbsorbedThisTick, thermalNeutronAbsorbedThisTick, fastNeutronInFluxThisTick, thermalNeutronInFluxThisTick,
                neutronGeneratedThisTick);

        fastNeutronAbsorbedThisTick = 0;
        thermalNeutronAbsorbedThisTick = 0;
        fastNeutronInFluxThisTick = 0;
        thermalNeutronInFluxThisTick = 0;
        neutronGeneratedThisTick = 0;
    }

    public void absorbNeutrons(int neutronNumber, NeutronType type) {
        Preconditions.checkArgument(type != NeutronType.BOTH);
        if (type == NeutronType.FAST) {
            fastNeutronAbsorbedThisTick += neutronNumber;
        } else {
            thermalNeutronAbsorbedThisTick += neutronNumber;
        }

    }

    public void addNeutronsToFlux(int neutronNumber, NeutronType type) {
        Preconditions.checkArgument(type != NeutronType.BOTH);
        if (type == NeutronType.FAST) {
            fastNeutronInFluxThisTick += neutronNumber;
        } else {
            thermalNeutronInFluxThisTick += neutronNumber;
        }

    }

    public static void registerItemApi(BlockEntityType<?> bet) {
        ItemStorage.SIDED.registerForBlockEntities((be, direction) -> direction == UP ? ((NuclearHatch) be).getInventory().itemStorage : null, bet);
    }

    public static void registerFluidApi(BlockEntityType<?> bet) {
        FluidStorage.SIDED.registerForBlockEntities((be, direction) -> direction == UP ? ((NuclearHatch) be).getInventory().fluidStorage : null, bet);
    }

}
