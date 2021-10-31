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
package aztech.modern_industrialization.machines.models;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.MachineBlockEntityRenderer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.block.entity.BlockEntityType;

public final class MachineModels {
    public static void init() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new MachineModelProvider());
        ModelLoadingRegistry.INSTANCE.registerModelProvider(new MachineModelProvider());
    }

    // The tier is a string to prevent loading client classes on a dedicated server.
    @SuppressWarnings("IfCanBeSwitch")
    public static void addTieredMachine(String tier, String id, String machineType, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        MachineCasing defaultCasing;
        if (tier.equals("bronze")) {
            defaultCasing = MachineCasings.BRONZE;
        } else if (tier.equals("steel")) {
            defaultCasing = MachineCasings.STEEL;
        } else if (tier.equals("electric")) {
            defaultCasing = MachineCasings.LV;
        } else {
            throw new RuntimeException("Invalid tier: " + tier);
        }
        addTieredMachine(id, machineType, defaultCasing, frontOverlay, topOverlay, sideOverlay);
    }

    public static void addTieredMachine(String id, String overlayFolder, MachineCasing defaultCasing, boolean frontOverlay, boolean topOverlay,
            boolean sideOverlay) {
        addTieredMachine(id, overlayFolder, defaultCasing, frontOverlay, topOverlay, sideOverlay, true);
    }

    public static void addTieredMachine(String id, String overlayFolder, MachineCasing defaultCasing, boolean frontOverlay, boolean topOverlay,
            boolean sideOverlay, boolean hasActive) {
        MachineUnbakedModel model = new MachineUnbakedModel(overlayFolder, frontOverlay, topOverlay, sideOverlay, hasActive, defaultCasing.mcm)
                .withStandardOverlays();
        MachineModelProvider.register(new MIIdentifier("block/" + id), model);
        MachineModelProvider.register(new MIIdentifier("item/" + id), model);
    }

    public static void addTieredMachineTiers(String name, boolean frontOverlay, boolean topOverlay, boolean sideOverlay, String... tiers) {
        for (String tier : tiers) {
            addTieredMachine(tier, tier + "_" + name, name, frontOverlay, topOverlay, sideOverlay);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void addMachineBer(BlockEntityType bet, String id) {
        BlockEntityRendererRegistry.register(bet, c -> new MachineBlockEntityRenderer(c));
    }

    private MachineModels() {
    }
}
