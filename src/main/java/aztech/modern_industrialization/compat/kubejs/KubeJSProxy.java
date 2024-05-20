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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.neoforged.fml.ModList;

public class KubeJSProxy {
    public static void blockUntilKubeJSIsLoaded() {
        try {
            if (!loadLatch.await(1, TimeUnit.MINUTES)) {
                throw new IllegalStateException("Failed to wait for KubeJS initialization. Timeout of 1 minute was not enough?");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (ModList.get().isLoaded("kubejs") && instance.getClass() == KubeJSProxy.class) {
            throw new IllegalStateException("Failed to wait for KubeJS initialization.");
        }
    }

    public static CountDownLatch loadLatch = new CountDownLatch(ModList.get().isLoaded("kubejs") ? 1 : 0);
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
