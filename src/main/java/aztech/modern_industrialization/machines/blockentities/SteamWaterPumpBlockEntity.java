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

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.helper.SteamHelper;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;

public class SteamWaterPumpBlockEntity extends AbstractWaterPumpBlockEntity {
    public SteamWaterPumpBlockEntity(BEP bep, boolean bronze) {
        super(bep, bronze ? "bronze_water_pump" : "steel_water_pump");
        this.bronze = bronze;

        long capacity = FluidType.BUCKET_VOLUME * (bronze ? 8 : 16);
        List<ConfigurableFluidStack> fluidStacks = Arrays.asList(ConfigurableFluidStack.lockedInputSlot(capacity, MIFluids.STEAM.asFluid()),
                ConfigurableFluidStack.lockedOutputSlot(capacity, Fluids.WATER));
        SlotPositions fluidPositions = new SlotPositions.Builder().addSlot(21, 30).addSlot(OUTPUT_SLOT_X, OUTPUT_SLOT_Y).build();
        this.inventory = new MIInventory(Collections.emptyList(), fluidStacks, SlotPositions.empty(), fluidPositions);
        this.registerComponents(inventory);
    }

    public final boolean bronze;
    private final MIInventory inventory;

    @Override
    protected long consumeEu(long max) {
        return SteamHelper.consumeSteamEu(inventory.getFluidStacks(), max, Simulation.ACT);
    }

    @Override
    protected int getWaterMultiplier() {
        return bronze ? 1 : 2;
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    protected MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData();
        data.isActive = isActiveComponent.isActive;
        orientation.writeModelData(data);
        return data;
    }
}
