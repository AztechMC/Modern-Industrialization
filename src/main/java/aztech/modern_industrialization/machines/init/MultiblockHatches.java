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
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class MultiblockHatches {
    public static void init() {
        registerItemHatches("bronze", MachineCasings.BRONZE, 1, 1, 80, 40);
        registerItemHatches("steel", MachineCasings.STEEL, 2, 1, 80, 30);
        registerItemHatches("advanced", MachineCasings.MV, 2, 2, 71, 30);
        registerItemHatches("turbo", MachineCasings.HV, 3, 3, 62, 21);

        registerFluidHatches("bronze", MachineCasings.BRONZE, 4);
        registerFluidHatches("steel", MachineCasings.STEEL, 8);
        registerFluidHatches("advanced", MachineCasings.MV, 16);
        registerFluidHatches("turbo", MachineCasings.HV, 32);

        registerEnergyHatches(CableTier.LV);
        registerEnergyHatches(CableTier.MV);
        registerEnergyHatches(CableTier.HV);
        registerEnergyHatches(CableTier.EV);
        registerEnergyHatches(CableTier.SUPRACONDUCTOR);
    }

    private static void registerItemHatches(String prefix, MachineCasing casing, int rows, int columns, int xStart, int yStart) {
        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = prefix + "_item_" + (input ? "input" : "output") + "_hatch";
            MachineRegistrationHelper.registerMachine(machine, bet -> {
                List<ConfigurableItemStack> itemStacks = new ArrayList<>();
                for (int i = 0; i < rows * columns; ++i) {
                    if (input) {
                        itemStacks.add(ConfigurableItemStack.standardInputSlot());
                    } else {
                        itemStacks.add(ConfigurableItemStack.standardOutputSlot());
                    }
                }
                MIInventory inventory = new MIInventory(itemStacks, Collections.emptyList(),
                        new SlotPositions.Builder().addSlots(xStart, yStart, rows, columns).build(), SlotPositions.empty());
                return new ItemHatch(bet, new MachineGuiParameters.Builder(machine, true).build(), input, !prefix.equals("bronze"), inventory);
            }, MachineBlockEntity::registerItemApi);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                MachineModels.addTieredMachine(machine, "hatch_item", casing, true, false, true);
            }
        }
    }

    public static final int FLUID_HATCH_SLOT_X = 80;
    public static final int FLUID_HATCH_SLOT_Y = 40;

    private static void registerFluidHatches(String prefix, MachineCasing casing, int bucketCapacity) {
        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = prefix + "_fluid_" + (input ? "input" : "output") + "_hatch";
            MachineRegistrationHelper.registerMachine(machine, bet -> {
                List<ConfigurableFluidStack> fluidStacks = Collections
                        .singletonList(input ? ConfigurableFluidStack.standardInputSlot(bucketCapacity * 81000)
                                : ConfigurableFluidStack.standardOutputSlot(bucketCapacity * 81000));
                MIInventory inventory = new MIInventory(Collections.emptyList(), fluidStacks, SlotPositions.empty(),
                        new SlotPositions.Builder().addSlot(FLUID_HATCH_SLOT_X, FLUID_HATCH_SLOT_Y).build());
                return new FluidHatch(bet, new MachineGuiParameters.Builder(machine, true).build(), input, !prefix.equals("bronze"), inventory);
            }, MachineBlockEntity::registerFluidApi);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                MachineModels.addTieredMachine(machine, "hatch_fluid", casing, true, false, true);
            }
        }
    }

    private static void registerEnergyHatches(CableTier tier) {
        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = tier.name + "_energy_" + (input ? "input" : "output") + "_hatch";
            MachineRegistrationHelper.registerMachine(machine, bet -> new EnergyHatch(bet, machine, input, tier), EnergyHatch::registerEnergyApi);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                MachineModels.addTieredMachine(machine, "hatch_energy", MachineCasings.casingFromCableTier(tier), true, false, true);
            }
        }
    }
}
