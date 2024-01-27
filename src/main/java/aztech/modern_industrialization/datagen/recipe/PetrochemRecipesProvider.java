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
package aztech.modern_industrialization.datagen.recipe;

import static aztech.modern_industrialization.MIFluids.*;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.definition.FluidLike;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import com.google.common.base.Preconditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluid;

public class PetrochemRecipesProvider extends MIRecipesProvider {
    public PetrochemRecipesProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        generateDistillation(consumer, 12, 200, f(CRUDE_OIL, 1000),
                f(SULFURIC_LIGHT_FUEL, 500),
                f(SULFURIC_HEAVY_FUEL, 200),
                f(SULFURIC_NAPHTHA, 300));
        generateDistillation(consumer, 20, 200, f(STEAM_CRACKED_HEAVY_FUEL, 1000),
                f(LIGHT_FUEL, 500),
                f(METHANE, 100),
                f(BUTADIENE, 250),
                f(BENZENE, 150));
        generateDistillation(consumer, 25, 200, f(STEAM_CRACKED_LIGHT_FUEL, 1000),
                f(METHANE, 200),
                f(ACETYLENE, 100),
                f(ETHYLENE, 300),
                f(BUTADIENE, 200),
                f(BENZENE, 200));
        generateDistillation(consumer, 15, 200, f(STEAM_CRACKED_NAPHTHA, 1000),
                f(METHANE, 150),
                f(ACETYLENE, 50),
                f(ETHYLENE, 250),
                f(PROPENE, 75),
                f(BUTADIENE, 125),
                f(BENZENE, 150),
                f(TOLUENE, 100),
                f(ETHYLBENZENE, 100));
        generateDistillation(consumer, 10, 200, f(SHALE_OIL, 1000),
                f(HELIUM, 50),
                f(SULFURIC_CRUDE_OIL, 450),
                f(SULFURIC_NAPHTHA, 500));
        generateSulfuricPurification(consumer, CRUDE_OIL);
        generateSulfuricPurification(consumer, HEAVY_FUEL);
        generateSulfuricPurification(consumer, LIGHT_FUEL);
        generateSulfuricPurification(consumer, NAPHTHA);
        generatePolymerization(consumer, ETHYLENE, POLYETHYLENE);
        generatePolymerization(consumer, VINYL_CHLORIDE, POLYVINYL_CHLORIDE);
        generatePolymerization(consumer, CAPROLACTAM, NYLON);
        generatePolymerization(consumer, ACRYLIC_ACID, ACRYLIC_GLUE);
        generatePolymerization(consumer, STYRENE_BUTADIENE, STYRENE_BUTADIENE_RUBBER);
        generateSteamCracking(consumer, HEAVY_FUEL, STEAM_CRACKED_HEAVY_FUEL);
        generateSteamCracking(consumer, LIGHT_FUEL, STEAM_CRACKED_LIGHT_FUEL);
        generateSteamCracking(consumer, NAPHTHA, STEAM_CRACKED_NAPHTHA);
    }

    /**
     * Generate both the full distillation tower recipe, and each distillery recipe.
     */
    private void generateDistillation(RecipeOutput consumer, int eu, int duration, FluidEntry input, FluidEntry... outputs) {
        String basePath = "petrochem/distillation/" + BuiltInRegistries.FLUID.getKey(input.fluid).getPath() + "_";

        // Full recipe
        var full = new MachineRecipeBuilder(MIMachineRecipeTypes.DISTILLATION_TOWER, eu * outputs.length, duration);
        full.addFluidInput(input.fluid, input.amount);
        for (var output : outputs) {
            full.addFluidOutput(output.fluid, output.amount);
        }
        full.offerTo(consumer, basePath + "full");

        // Partial recipes
        for (int i = 0; i < outputs.length; ++i) {
            var output = outputs[i];
            new MachineRecipeBuilder(MIMachineRecipeTypes.DISTILLERY, eu, duration)
                    .addFluidInput(input.fluid, input.amount).addFluidOutput(output.fluid, output.amount)
                    .offerTo(consumer, basePath + i);
        }
    }

    /**
     * Generate the sulfuric -> purified fluid chemical reactor recipe.
     */
    private void generateSulfuricPurification(RecipeOutput consumer, Fluid purifiedFluid) {
        String baseName = BuiltInRegistries.FLUID.getKey(purifiedFluid).getPath();
        Fluid sulfuricFluid = BuiltInRegistries.FLUID.get(new MIIdentifier("sulfuric_" + baseName));
        Preconditions.checkArgument(sulfuricFluid instanceof MIFluid);

        new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 16, 400)
                .addFluidInput(sulfuricFluid, 12000).addFluidInput(HYDROGEN.asFluid(), 2000)
                .addFluidOutput(purifiedFluid, 12000).addFluidOutput(SULFURIC_ACID.asFluid(), 2000)
                .offerTo(consumer, "petrochem/sulfuric_purification/" + baseName);
    }

    private void generateSulfuricPurification(RecipeOutput consumer, FluidLike purifiedFluid) {
        generateSulfuricPurification(consumer, purifiedFluid.asFluid());
    }

    private void generatePolymerization(RecipeOutput consumer, Fluid input, Fluid output) {
        String baseNameInput = BuiltInRegistries.FLUID.getKey(input).getPath();

        for (var kind : PolymerizationKind.values()) {
            new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 12, 700)
                    .addItemInput("#forge:tiny_dusts/" + kind.catalystMaterial, kind.inputTinyDust)
                    .addFluidInput(input, 500)
                    .addFluidOutput(output, kind.outputMillis)
                    .offerTo(consumer, "petrochem/polymerization/" + baseNameInput + "_" + kind.name().toLowerCase());
        }
    }

    private void generatePolymerization(RecipeOutput consumer, FluidLike input, FluidLike output) {
        generatePolymerization(consumer, input.asFluid(), output.asFluid());
    }

    private void generateSteamCracking(RecipeOutput consumer, FluidLike input, FluidLike output) {
        new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 8, 100)
                .addFluidInput(input, 1000)
                .addFluidInput(STEAM, 100)
                .addFluidOutput(output, 1000)
                .offerTo(consumer, "petrochem/steam_cracking/" + BuiltInRegistries.FLUID.getKey(input.asFluid()).getPath());
    }

    private static FluidEntry f(Fluid fluid, int amount) {
        return new FluidEntry(fluid, amount);
    }

    private static FluidEntry f(FluidLike fluid, int amount) {
        return f(fluid.asFluid(), amount);
    }

    private record FluidEntry(Fluid fluid, int amount) {
    }

    private enum PolymerizationKind {
        LEAD("lead", 4, 300),
        CHROMIUM("chromium", 1, 700),
        ;

        private final String catalystMaterial;
        private final int inputTinyDust;
        private final int outputMillis;

        PolymerizationKind(String catalystMaterial, int inputTinyDust, int outputMillis) {
            this.catalystMaterial = catalystMaterial;
            this.inputTinyDust = inputTinyDust;
            this.outputMillis = outputMillis;
        }
    }
}
