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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiblockHatches {
    public static void init() {
        registerItemHatches("Bronze", "bronze", MachineCasings.BRONZE, 1, 1, 80, 40);
        registerItemHatches("Steel", "steel", MachineCasings.STEEL, 2, 1, 80, 30);
        registerItemHatches("Advanced", "advanced", CableTier.MV.casing, 2, 2, 71, 30);
        registerItemHatches("Turbo", "turbo", CableTier.HV.casing, 3, 3, 62, 21);
        registerItemHatches("Highly Advanced", "highly_advanced", CableTier.EV.casing, 3, 5, 44, 18);

        registerFluidHatches("Bronze", "bronze", MachineCasings.BRONZE, 4);
        registerFluidHatches("Steel", "steel", MachineCasings.STEEL, 8);
        registerFluidHatches("Advanced", "advanced", CableTier.MV.casing, 16);
        registerFluidHatches("Turbo", "turbo", CableTier.HV.casing, 32);
        registerFluidHatches("Highly Advanced", "highly_advanced", CableTier.EV.casing, 64);

        registerEnergyHatches(CableTier.LV);
        registerEnergyHatches(CableTier.MV);
        registerEnergyHatches(CableTier.HV);
        registerEnergyHatches(CableTier.EV);
        registerEnergyHatches(CableTier.SUPERCONDUCTOR);

        MachineRegistrationHelper.registerMachine(
                "Nuclear Item Hatch",
                "nuclear_item_hatch", bet -> new NuclearHatch(bet, false), NuclearHatch::registerItemApi);
        MachineRegistrationHelper.registerMachine("Nuclear Fluid Hatch", "nuclear_fluid_hatch", bet -> new NuclearHatch(bet, true),
                NuclearHatch::registerFluidApi);

        MachineRegistrationHelper.addMachineModel("nuclear_item_hatch", "hatch_nuclear", MachineCasings.NUCLEAR, false, true, false, false);
        MachineRegistrationHelper.addMachineModel("nuclear_fluid_hatch", "hatch_nuclear", MachineCasings.NUCLEAR, false, true, false, false);

        MachineRegistrationHelper.registerMachine("Large Tank Hatch", "large_tank_hatch", LargeTankHatch::new, LargeTankHatch::registerFluidApi);
        MachineRegistrationHelper.addMachineModel("large_tank_hatch", "hatch_fluid", MachineCasings.STEEL, false, false, true, false);
    }

    private static void registerItemHatches(
            String englishPrefix, String prefix, MachineCasing casing, int rows, int columns, int xStart, int yStart) {
        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = prefix + "_item_" + (input ? "input" : "output") + "_hatch";
            String englishName = englishPrefix + " Item" + (input ? " Input" : " Output") + " Hatch";
            MachineRegistrationHelper.registerMachine(englishName, machine, bet -> {
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
            MachineRegistrationHelper.addMachineModel(machine, "hatch_item", casing, true, false, true, false);
        }
    }

    public static final int FLUID_HATCH_SLOT_X = 80;
    public static final int FLUID_HATCH_SLOT_Y = 40;

    private static void registerFluidHatches(String englishPrefix, String prefix, MachineCasing casing, int bucketCapacity) {
        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = prefix + "_fluid_" + (input ? "input" : "output") + "_hatch";
            String englishName = englishPrefix + " Fluid" + (input ? " Input" : " Output") + " Hatch";
            MachineRegistrationHelper.registerMachine(englishName, machine, bet -> {
                List<ConfigurableFluidStack> fluidStacks = Collections
                        .singletonList(input ? ConfigurableFluidStack.standardInputSlot(bucketCapacity * 1000L)
                                : ConfigurableFluidStack.standardOutputSlot(bucketCapacity * 1000L));
                MIInventory inventory = new MIInventory(Collections.emptyList(), fluidStacks, SlotPositions.empty(),
                        new SlotPositions.Builder().addSlot(FLUID_HATCH_SLOT_X, FLUID_HATCH_SLOT_Y).build());
                return new FluidHatch(bet, new MachineGuiParameters.Builder(machine, true).build(), input, !prefix.equals("bronze"), inventory);
            }, MachineBlockEntity::registerFluidApi);
            MachineRegistrationHelper.addMachineModel(machine, "hatch_fluid", casing, true, false, true, false);
        }
    }

    private static void registerEnergyHatches(CableTier tier) {
        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = tier.name + "_energy_" + (input ? "input" : "output") + "_hatch";
            String englishName = tier.shortEnglishName + " Energy" + (input ? " Input" : " Output") + " Hatch";
            MachineRegistrationHelper.registerMachine(englishName, machine, bet -> new EnergyHatch(bet, machine, input, tier),
                    EnergyHatch::registerEnergyApi);
            MachineRegistrationHelper.addMachineModel(machine, "hatch_energy", tier.casing, true, false, true, false);
        }
    }
}
