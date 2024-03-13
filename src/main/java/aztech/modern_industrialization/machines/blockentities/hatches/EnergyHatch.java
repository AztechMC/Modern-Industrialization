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

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import java.util.List;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class EnergyHatch extends HatchBlockEntity implements EnergyComponentHolder {

    public EnergyHatch(BEP bep, String name, boolean input, CableTier tier) {
        super(bep, new MachineGuiParameters.Builder(name, false).build(), new OrientationComponent.Params(!input, false, false));

        this.input = input;

        this.energy = new EnergyComponent(this, 30 * 20 * tier.getEu());
        insertable = energy.buildInsertable((CableTier tier2) -> tier2 == tier);
        extractable = energy.buildExtractable((CableTier tier2) -> tier2 == tier);
        EnergyBar.Parameters energyBarParams = new EnergyBar.Parameters(76, 39);
        registerGuiComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));

        this.registerComponents(energy);
    }

    private final boolean input;

    protected final EnergyComponent energy;
    protected final MIEnergyStorage insertable;
    protected final MIEnergyStorage extractable;

    @Override
    public HatchType getHatchType() {
        return input ? HatchType.ENERGY_INPUT : HatchType.ENERGY_OUTPUT;
    }

    @Override
    public boolean upgradesToSteel() {
        return false;
    }

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

    @Override
    public void appendEnergyInputs(List<EnergyComponent> list) {
        if (input) {
            list.add(energy);
        }
    }

    @Override
    public void appendEnergyOutputs(List<EnergyComponent> list) {
        if (!input) {
            list.add(energy);
        }
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(EnergyApi.SIDED, bet, (be, direction) -> {
                EnergyHatch eh = (EnergyHatch) be;
                if (eh.input) {
                    return eh.insertable;
                } else {
                    if (eh.orientation.outputDirection == direction) {
                        return eh.extractable;
                    } else {
                        return null;
                    }
                }
            });
        });
    }
}
