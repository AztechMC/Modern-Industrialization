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
package aztech.modern_industrialization.compat.kubejs.machine;

import static aztech.modern_industrialization.machines.init.SingleBlockCraftingMachines.*;

import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.RecipeEfficiencyBar;
import aztech.modern_industrialization.machines.init.MachineRegistrationHelper;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.init.SingleBlockCraftingMachines;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import dev.latvian.mods.kubejs.event.EventJS;
import java.util.List;
import java.util.function.Consumer;

public class RegisterMachinesEventJS extends EventJS implements ShapeTemplateHelper {
    public ProgressBar.Parameters progressBar(int renderX, int renderY, String type) {
        return new ProgressBar.Parameters(renderX, renderY, type);
    }

    public RecipeEfficiencyBar.Parameters efficiencyBar(int renderX, int renderY) {
        return new RecipeEfficiencyBar.Parameters(renderX, renderY);
    }

    public EnergyBar.Parameters energyBar(int renderX, int renderY) {
        return new EnergyBar.Parameters(renderX, renderY);
    }

    public void craftingSingleBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, List<String> tiers,
            // gui
            int backgroundHeight, // can be -1 to use default
            ProgressBar.Parameters progressBar, RecipeEfficiencyBar.Parameters efficiencyBar, EnergyBar.Parameters energyBar,
            // slots
            int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs, int bucketCapacity,
            Consumer<SlotPositions.Builder> itemSlotPositions, Consumer<SlotPositions.Builder> fluidSlotPositions,
            // model
            boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        int tiersMask = 0;
        for (String tier : tiers) {
            tiersMask |= switch (tier) {
            case "bronze" -> TIER_BRONZE;
            case "steel" -> TIER_STEEL;
            case "electric" -> TIER_ELECTRIC;
            default -> throw new IllegalArgumentException("Unknown tier: " + tier);
            };
        }
        Consumer<MachineGuiParameters.Builder> guiParams = backgroundHeight < 0 ? p -> {
        } : p -> p.backgroundHeight(backgroundHeight);

        SingleBlockCraftingMachines.registerMachineTiers(
                englishName, internalName, recipeType,
                itemInputs, itemOutputs, fluidInputs, fluidOutputs,
                guiParams, progressBar, efficiencyBar, energyBar,
                itemSlotPositions, fluidSlotPositions,
                frontOverlay, topOverlay, sideOverlay,
                tiersMask, bucketCapacity);
    }

    public void simpleElectricCraftingMultiBlock(
            // general
            String englishName, String internalName, MachineRecipeType recipeType, ShapeTemplate multiblockShape,
            // REI parameters
            ProgressBar.Parameters progressBar,
            Consumer<SlotPositions.Builder> itemInputPositions, Consumer<SlotPositions.Builder> itemOutputPositions,
            Consumer<SlotPositions.Builder> fluidInputPositions, Consumer<SlotPositions.Builder> fluidOutputPositions,
            // model
            String controllerCasingName, String overlayFolder, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {

        var controllerCasing = MachineCasings.get(controllerCasingName);

        MachineRegistrationHelper.registerMachine(
                englishName, internalName,
                bet -> new ElectricCraftingMultiblockBlockEntity(bet, internalName, multiblockShape, recipeType));
        MachineRegistrationHelper.addMachineModel(internalName, overlayFolder, controllerCasing, frontOverlay, topOverlay, sideOverlay);
        new MultiblockMachines.Rei(englishName, internalName, recipeType, progressBar)
                .items(itemInputPositions::accept, itemOutputPositions::accept)
                .fluids(fluidInputPositions::accept, fluidOutputPositions::accept)
                .register();
        ReiMachineRecipes.registerMultiblockShape(internalName, multiblockShape);
    }
}
