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

package aztech.modern_industrialization.machines.init;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.hatches.EnergyHatch;
import aztech.modern_industrialization.machines.blockentities.hatches.FluidHatch;
import aztech.modern_industrialization.machines.blockentities.hatches.ItemHatch;
import aztech.modern_industrialization.machines.blockentities.hatches.LargeTankHatch;
import aztech.modern_industrialization.machines.blockentities.hatches.NuclearHatch;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiblockHatches {
    public record HatchPair<T extends HatchBlockEntity>(MachineDefinition<T> input, MachineDefinition<T> output) {
    }

    public static final HatchPair<ItemHatch> BRONZE_ITEM = registerItemHatches("Bronze", "bronze", MachineCasings.BRONZE, 1, 1, 80, 40);
    public static final HatchPair<ItemHatch> STEEL_ITEM = registerItemHatches("Steel", "steel", MachineCasings.STEEL, 2, 1, 80, 30);
    public static final HatchPair<ItemHatch> ADVANCED_ITEM = registerItemHatches("Advanced", "advanced", CableTier.MV.casing, 2, 2, 71, 30);
    public static final HatchPair<ItemHatch> TURBO_ITEM = registerItemHatches("Turbo", "turbo", CableTier.HV.casing, 3, 3, 62, 21);
    public static final HatchPair<ItemHatch> HIGHLY_ADVANCED_ITEM = registerItemHatches("Highly Advanced", "highly_advanced", CableTier.EV.casing, 3,
            5, 44, 18);

    public static final HatchPair<FluidHatch> BRONZE_FLUID = registerFluidHatches("Bronze", "bronze", MachineCasings.BRONZE, 4);
    public static final HatchPair<FluidHatch> STEEL_FLUID = registerFluidHatches("Steel", "steel", MachineCasings.STEEL, 16);
    public static final HatchPair<FluidHatch> ADVANCED_FLUID = registerFluidHatches("Advanced", "advanced", CableTier.MV.casing, 64);
    public static final HatchPair<FluidHatch> TURBO_FLUID = registerFluidHatches("Turbo", "turbo", CableTier.HV.casing, 256);
    public static final HatchPair<FluidHatch> HIGHLY_ADVANCED_FLUID = registerFluidHatches("Highly Advanced", "highly_advanced", CableTier.EV.casing,
            1024);

    public static final HatchPair<EnergyHatch> LV_ENERGY = registerEnergyHatches(CableTier.LV);
    public static final HatchPair<EnergyHatch> MV_ENERGY = registerEnergyHatches(CableTier.MV);
    public static final HatchPair<EnergyHatch> HV_ENERGY = registerEnergyHatches(CableTier.HV);
    public static final HatchPair<EnergyHatch> EV_ENERGY = registerEnergyHatches(CableTier.EV);
    public static final HatchPair<EnergyHatch> SUPERCONDUCTOR_ENERGY = registerEnergyHatches(CableTier.SUPERCONDUCTOR);

    public static final MachineDefinition<NuclearHatch> NUCLEAR_ITEM = MachineRegistrationHelper.registerMachine(
            "Nuclear Item Hatch", "nuclear_item_hatch",
            bet -> new NuclearHatch(bet, false), NuclearHatch::registerItemApi);
    public static final MachineDefinition<NuclearHatch> NUCLEAR_FLUID = MachineRegistrationHelper.registerMachine(
            "Nuclear Fluid Hatch", "nuclear_fluid_hatch",
            bet -> new NuclearHatch(bet, true), NuclearHatch::registerFluidApi);

    public static final MachineDefinition<LargeTankHatch> LARGE_TANK = MachineRegistrationHelper.registerMachine(
            "Large Tank Hatch", "large_tank_hatch",
            LargeTankHatch::new, LargeTankHatch::registerFluidApi);

    public static void init() {
        // Triggers static init

        MachineRegistrationHelper.addMachineModel("nuclear_item_hatch", "hatch_nuclear", MachineCasings.NUCLEAR, false, true, false, false);
        MachineRegistrationHelper.addMachineModel("nuclear_fluid_hatch", "hatch_nuclear", MachineCasings.NUCLEAR, false, true, false, false);

        MachineRegistrationHelper.addMachineModel("large_tank_hatch", "hatch_fluid", MachineCasings.STEEL, false, false, true, false);

        KubeJSProxy.instance.fireRegisterHatchesEvent();
    }

    public static HatchPair<ItemHatch> registerItemHatches(
            String englishPrefix, String prefix, MachineCasing casing, int rows, int columns, int xStart, int yStart) {
        List<MachineDefinition<ItemHatch>> definitions = new ArrayList<>(2);

        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = prefix + "_item_" + (input ? "input" : "output") + "_hatch";
            String englishName = englishPrefix + " Item" + (input ? " Input" : " Output") + " Hatch";
            var def = MachineRegistrationHelper.registerMachine(englishName, machine, bet -> {
                List<ConfigurableItemStack> itemStacks = new ArrayList<>();
                for (int i = 0; i < rows * columns; ++i) {
                    if (input) {
                        itemStacks.add(ConfigurableItemStack.standardInputSlot());
                    } else {
                        itemStacks.add(ConfigurableItemStack.standardOutputSlot());
                    }
                }
                MIInventory inventory = new MIInventory(itemStacks, Collections.emptyList(),
                        new SlotPositions.Builder().addSlots(xStart, yStart, columns, rows).build(), SlotPositions.empty());
                return new ItemHatch(bet, new MachineGuiParameters.Builder(machine, true).build(), input, !prefix.equals("bronze"), inventory);
            }, MachineBlockEntity::registerItemApi);
            definitions.add(def);
            MachineRegistrationHelper.addMachineModel(machine, "hatch_item", casing, true, false, true, false);
        }

        return new HatchPair<>(definitions.get(0), definitions.get(1));
    }

    private static final int FLUID_HATCH_SLOT_X = 80;
    private static final int FLUID_HATCH_SLOT_Y = 40;

    public static HatchPair<FluidHatch> registerFluidHatches(String englishPrefix, String prefix, MachineCasing casing, int bucketCapacity) {
        List<MachineDefinition<FluidHatch>> definitions = new ArrayList<>(2);

        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = prefix + "_fluid_" + (input ? "input" : "output") + "_hatch";
            String englishName = englishPrefix + " Fluid" + (input ? " Input" : " Output") + " Hatch";
            var def = MachineRegistrationHelper.registerMachine(englishName, machine, bet -> {
                List<ConfigurableFluidStack> fluidStacks = Collections
                        .singletonList(input ? ConfigurableFluidStack.standardInputSlot(bucketCapacity * 1000L)
                                : ConfigurableFluidStack.standardOutputSlot(bucketCapacity * 1000L));
                MIInventory inventory = new MIInventory(Collections.emptyList(), fluidStacks, SlotPositions.empty(),
                        new SlotPositions.Builder().addSlot(FLUID_HATCH_SLOT_X, FLUID_HATCH_SLOT_Y).build());
                return new FluidHatch(bet, new MachineGuiParameters.Builder(machine, true).build(), input, !prefix.equals("bronze"), inventory);
            }, MachineBlockEntity::registerFluidApi);
            definitions.add(def);
            MachineRegistrationHelper.addMachineModel(machine, "hatch_fluid", casing, true, false, true, false);
        }

        return new HatchPair<>(definitions.get(0), definitions.get(1));
    }

    public static HatchPair<EnergyHatch> registerEnergyHatches(CableTier tier) {
        List<MachineDefinition<EnergyHatch>> definitions = new ArrayList<>(2);

        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = tier.name + "_energy_" + (input ? "input" : "output") + "_hatch";
            String englishName = tier.shortEnglishName + " Energy" + (input ? " Input" : " Output") + " Hatch";
            var def = MachineRegistrationHelper.registerMachine(englishName, machine,
                    bet -> new EnergyHatch(bet, new MachineGuiParameters.Builder(machine, false).build(), input, tier),
                    EnergyHatch::registerEnergyApi);
            definitions.add(def);
            MachineRegistrationHelper.addMachineModel(machine, "hatch_energy", tier.casing, true, false, true, false);
        }

        return new HatchPair<>(definitions.get(0), definitions.get(1));
    }
}
