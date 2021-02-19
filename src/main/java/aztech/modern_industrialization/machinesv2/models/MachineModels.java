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
package aztech.modern_industrialization.machinesv2.models;

import aztech.modern_industrialization.MIIdentifier;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

public final class MachineModels {
    public static void init() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new MachineModelProvider());
        ModelLoadingRegistry.INSTANCE.registerModelProvider(new MachineModelProvider());
    }

    @SuppressWarnings("IfCanBeSwitch")
    public static void addTieredMachine(String tier, String name, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        MachineCasingModel defaultCasing;
        if (tier.equals("bronze")) {
            defaultCasing = MachineCasings.BRONZE;
        } else if (tier.equals("steel")) {
            defaultCasing = MachineCasings.STEEL;
        } else if (tier.equals("lv")) {
            defaultCasing = MachineCasings.LV;
        } else {
            throw new RuntimeException("Invalid tier: " + tier);
        }
        MachineUnbakedModel model = new MachineUnbakedModel(name, frontOverlay, topOverlay, sideOverlay, defaultCasing).withStandardOverlays();
        MachineModelProvider.register(new MIIdentifier("block/" + tier + "_" + name), model);
        MachineModelProvider.register(new MIIdentifier("item/" + tier + "_" + name), model);
    }

    private MachineModels() {
    }
}
