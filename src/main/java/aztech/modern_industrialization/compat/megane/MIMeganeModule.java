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
package aztech.modern_industrialization.compat.megane;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankBlockEntity;
import aztech.modern_industrialization.blocks.storage.AbstractStorageBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.TankBlockEntity;
import aztech.modern_industrialization.compat.megane.holder.CrafterComponentHolder;
import aztech.modern_industrialization.compat.megane.holder.EnergyComponentHolder;
import aztech.modern_industrialization.compat.megane.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.compat.megane.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.compat.megane.provider.*;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import lol.bai.megane.api.MeganeModule;
import lol.bai.megane.api.registry.ClientRegistrar;
import lol.bai.megane.api.registry.CommonRegistrar;

public class MIMeganeModule implements MeganeModule {
    @Override
    public void registerCommon(CommonRegistrar registrar) {
        registrar.addEnergy(EnergyListComponentHolder.class, new ComponentListEnergyProvider());
        registrar.addEnergy(EnergyComponentHolder.class, new ComponentEnergyProvider());

        registrar.addFluid(MultiblockInventoryComponentHolder.class, new MultiblockFluidProvider());
        registrar.addFluid(TankBlockEntity.class, new TankFluidProvider());
        registrar.addFluid(CreativeTankBlockEntity.class, new CreativeTankFluidProvider());
        registrar.addFluid(MachineBlockEntity.class, new MachineFluidProvider());

        registrar.addItem(MultiblockInventoryComponentHolder.class, new MultiblockItemProvider());
        registrar.addItem(AbstractStorageBlockEntity.class, new StorageItemProvider());
        registrar.addItem(MachineBlockEntity.class, new MachineItemProvider());

        registrar.addProgress(CrafterComponentHolder.class, new CrafterProgressProvider());
    }

    @Override
    public void registerClient(ClientRegistrar registrar) {
        registrar.addEnergyInfo(ModernIndustrialization.MOD_ID, 0xB70000, "EU");

        registrar.addFluidInfo(MIFluid.class, new CraftingFluidInfoProvider());
    }
}
