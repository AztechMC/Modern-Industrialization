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

import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.modern_industrialization.materials.MaterialBuilder;
import java.util.function.Consumer;

public class KubeJSProxy {
    public static KubeJSProxy instance = new KubeJSProxy();

    public void fireAddMaterialsEvent() {
    }

    public void fireModifyMaterialEvent(MaterialBuilder materialBuilder) {
    }

    public void fireRegisterFluidsEvent() {
    }

    public void fireRegisterRecipeTypesEvent() {
    }

    public void fireRegisterMachineCasingsEvent() {
    }

    public void fireRegisterMachinesEvent() {
    }

    public void fireRegisterUpgradesEvent() {
    }

    public void fireAddMultiblockSlotsEvent(String category, SlotPositions.Builder itemInputs, SlotPositions.Builder itemOutputs,
            SlotPositions.Builder fluidInputs, SlotPositions.Builder fluidOutputs) {
    }

    public void fireAddEbfTiersEvent(Consumer<ElectricBlastFurnaceBlockEntity.Tier> tierConsumer) {
    }

    public void fireCustomConditionEvent() {
    }

    public void fireCableTiersEvent() {
    }

    public void fireRegisterFluidNeutronInteractionsEvent() {
    }
}
