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
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.block.entity.BlockEntityType;

public class SteamMachineBlockEntity extends AbstractRegularMachineBlockEntity {
    public SteamMachineBlockEntity(BlockEntityType<?> type, MachineRecipeType recipeType, MachineInventoryComponent inventory,
            MachineGuiParameters guiParams, ProgressBar.Parameters progressBarParams, MachineTier tier) {
        super(type, recipeType, inventory, guiParams, progressBarParams, tier);
    }

    @Override
    public long consumeEu(long max, Simulation simulation) {
        int totalRem = 0;
        for (ConfigurableFluidStack stack : getInventory().fluidStacks) {
            if (stack.getFluid() == MIFluids.STEAM) {
                long amount = stack.getAmount();
                long rem = Math.min(max, amount / 81);
                if (simulation.isActing()) {
                    stack.decrement(rem * 81);
                }
                max -= rem;
                totalRem += rem;
            }
        }
        return totalRem;
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData(tier == MachineTier.BRONZE ? MachineCasings.BRONZE : MachineCasings.STEEL);
        orientation.writeModelData(data);
        data.isActive = isActive;
        return data;
    }
}
