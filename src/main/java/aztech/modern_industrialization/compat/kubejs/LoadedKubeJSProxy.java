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
package aztech.modern_industrialization.compat.kubejs;

import aztech.modern_industrialization.compat.kubejs.machine.*;
import aztech.modern_industrialization.compat.kubejs.material.AddMaterialsEventJS;
import aztech.modern_industrialization.compat.kubejs.material.MIMaterialKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.material.ModifyMaterialEventJS;
import aztech.modern_industrialization.compat.kubejs.recipe.CustomConditionEventJS;
import aztech.modern_industrialization.compat.kubejs.recipe.MIRecipeKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.registration.MIRegistrationKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.registration.RegisterCableTiersEventJS;
import aztech.modern_industrialization.compat.kubejs.registration.RegisterFluidFuelsEventJS;
import aztech.modern_industrialization.compat.kubejs.registration.RegisterFluidNeutronInteractionsEventJS;
import aztech.modern_industrialization.compat.kubejs.registration.RegisterFluidsEventJS;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.modern_industrialization.materials.MaterialBuilder;
import java.util.function.Consumer;

public class LoadedKubeJSProxy extends KubeJSProxy {
    @Override
    public void fireAddMaterialsEvent() {
        MIMaterialKubeJSEvents.ADD_MATERIALS.post(new AddMaterialsEventJS());
    }

    @Override
    public void fireModifyMaterialEvent(MaterialBuilder materialBuilder) {
        MIMaterialKubeJSEvents.MODIFY_MATERIAL.post(new ModifyMaterialEventJS(materialBuilder), materialBuilder.getMaterialName());
    }

    @Override
    public void fireRegisterFluidsEvent() {
        MIRegistrationKubeJSEvents.REGISTER_FLUIDS.post(new RegisterFluidsEventJS());
    }

    @Override
    public void fireRegisterFluidFuelsEvent() {
        MIRegistrationKubeJSEvents.REGISTER_FLUID_FUELS.post(new RegisterFluidFuelsEventJS());
    }

    @Override
    public void fireRegisterRecipeTypesEvent() {
        MIMachineKubeJSEvents.REGISTER_RECIPE_TYPES.post(new RegisterRecipeTypesEventJS());
    }

    @Override
    public void fireRegisterMachineCasingsEvent() {
        MIMachineKubeJSEvents.REGISTER_CASINGS.post(new RegisterCasingsEventJS());
    }

    @Override
    public void fireRegisterMachinesEvent() {
        MIMachineKubeJSEvents.REGISTER_MACHINES.post(new RegisterMachinesEventJS());
    }

    @Override
    public void fireRegisterUpgradesEvent() {
        MIMachineKubeJSEvents.REGISTER_UPGRADES.post(new RegisterUpgradesEventJS());
    }

    @Override
    public void fireAddMultiblockSlotsEvent(String category, SlotPositions.Builder itemInputs, SlotPositions.Builder itemOutputs,
            SlotPositions.Builder fluidInputs, SlotPositions.Builder fluidOutputs) {
        var event = new AddMultiblockSlotsEventJS(itemInputs, itemOutputs, fluidInputs, fluidOutputs);
        MIMachineKubeJSEvents.ADD_MULTIBLOCK_SLOTS.post(event, category);
    }

    @Override
    public void fireAddEbfTiersEvent(Consumer<ElectricBlastFurnaceBlockEntity.Tier> tierConsumer) {
        MIMachineKubeJSEvents.ADD_EBF_TIERS.post(new AddEbfTiersEventJS(tierConsumer));
    }

    @Override
    public void fireCustomConditionEvent() {
        MIRecipeKubeJSEvents.CUSTOM_CONDITION.post(new CustomConditionEventJS());
    }

    @Override
    public void fireCableTiersEvent() {
        MIRegistrationKubeJSEvents.REGISTER_CABLE_TIERS.post(new RegisterCableTiersEventJS());
    }

    @Override
    public void fireRegisterFluidNeutronInteractionsEvent() {
        MIRegistrationKubeJSEvents.REGISTER_FLUID_NEUTRON_INTERACTIONS.post(new RegisterFluidNeutronInteractionsEventJS());
    }

}
