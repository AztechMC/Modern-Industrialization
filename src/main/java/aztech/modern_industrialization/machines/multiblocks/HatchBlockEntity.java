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
package aztech.modern_industrialization.machines.multiblocks;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Tickable;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public abstract class HatchBlockEntity extends MachineBlockEntity implements Tickable {
    public HatchBlockEntity(BEP bep, MachineGuiParameters guiParams, OrientationComponent.Params orientationParams) {
        super(bep, guiParams, orientationParams);

        registerComponents(new IComponent.ClientOnly() {
            @Override
            public void writeClientNbt(CompoundTag tag) {
                if (matchedCasing != null) {
                    tag.putString("matchedCasing", matchedCasing);
                }
            }

            @Override
            public void readClientNbt(CompoundTag tag) {
                matchedCasing = tag.contains("matchedCasing") ? tag.getString("matchedCasing") : null;
            }
        });
    }

    private String lastSyncedMachineCasing = null;
    private String matchedCasing = null;

    public abstract HatchType getHatchType();

    /**
     * Return true if this hatch upgrades steam multiblocks to steel tier.
     */
    public abstract boolean upgradesToSteel();

    public boolean isMatched() {
        return matchedCasing != null;
    }

    // This amazing function is called when the block entity is loaded.
    @Override
    public void clearRemoved() {
        super.clearRemoved();
        clearMachineLock();
    }

    public void unlink() {
        matchedCasing = null;
        clearMachineLock();
    }

    public void link(MachineCasing casing) {
        matchedCasing = casing.name;
    }

    protected void clearMachineLock() {
        for (ConfigurableItemStack itemStack : getInventory().getItemStacks()) {
            itemStack.disableMachineLock();
        }
        for (ConfigurableFluidStack fluidStack : getInventory().getFluidStacks()) {
            fluidStack.disableMachineLock();
        }
    }

    @Override
    protected MachineModelClientData getMachineModelData() {
        MachineCasing casing = isMatched() ? MachineCasings.get(matchedCasing) : null;
        MachineModelClientData data = new MachineModelClientData(casing);
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(placer, itemStack);
        if (orientation.params.hasOutput) {
            orientation.outputDirection = orientation.outputDirection.getOpposite();
        }
    }

    @Override
    public void tick() {
        if (level.isClientSide()) {
            return;
        }

        if (!Objects.equals(lastSyncedMachineCasing, matchedCasing)) {
            lastSyncedMachineCasing = matchedCasing;
            sync();
        }

        tickTransfer();
        setChanged();
    }

    protected void tickTransfer() {
    }

    public void appendItemInputs(List<ConfigurableItemStack> list) {
    }

    public void appendItemOutputs(List<ConfigurableItemStack> list) {
    }

    public void appendFluidInputs(List<ConfigurableFluidStack> list) {
    }

    public void appendFluidOutputs(List<ConfigurableFluidStack> list) {
    }

    public void appendEnergyInputs(List<EnergyComponent> list) {
    }

    public void appendEnergyOutputs(List<EnergyComponent> list) {

    }
}
