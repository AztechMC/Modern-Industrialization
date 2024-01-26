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

import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.recipe.json.ShapedRecipeJson;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;

public class HatchRecipesProvider extends MIRecipesProvider {

    private static final String pathPrefix = "hatches/";

    public HatchRecipesProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {

        String[] casings = { "bronze", "steel", "basic", "advanced", "turbo", "highly_advanced", "quantum" };
        String[] tanks = { "bronze", "steel", "", "aluminum", "stainless_steel", "titanium", "" };
        String[] cables = { "", "", "tin", "electrum", "aluminum", "annealed_copper", "superconductor" };
        String[] voltage = { "", "", "lv", "mv", "hv", "ev", "superconductor" };

        for (int i = 0; i < casings.length; i++) {

            String casing = String.format("modern_industrialization:%s_machine_hull", casings[i]);
            if (i < 2) {
                casing = String.format("modern_industrialization:%s_machine_casing", casings[i]);
            }

            String tank = String.format("modern_industrialization:%s_tank", tanks[i]);
            String fluidInput = String.format("modern_industrialization:%s_fluid_input_hatch", casings[i]);
            String fluidOutput = String.format("modern_industrialization:%s_fluid_output_hatch", casings[i]);
            String itemInput = String.format("modern_industrialization:%s_item_input_hatch", casings[i]);
            String itemOutput = String.format("modern_industrialization:%s_item_output_hatch", casings[i]);
            String energyInput = String.format("modern_industrialization:%s_energy_input_hatch", voltage[i]);
            String energyOutput = String.format("modern_industrialization:%s_energy_output_hatch", voltage[i]);
            String cable = String.format("modern_industrialization:%s_cable", cables[i]);
            String hopper = "minecraft:hopper";

            String[][] ABs = { { fluidInput, fluidOutput }, { itemInput, itemOutput }, { energyInput, energyOutput } };
            String[][] prefixes = { { "fluid_input", "fluid_output" }, { "item_input", "item_output" }, { "energy_input", "energy_output" } };
            String[] others = { tank, hopper, cable };

            for (int j = 0; j < ABs.length; j++) {

                if (((j != 2) && !casings[i].equals("basic") && !casings[i].equals("quantum")) || (j == 2 && !cables[i].equals(""))) {

                    String[] AB = ABs[j];
                    String other = others[j];

                    for (int k = 0; k < 2; k++) {
                        ShapedRecipeJson craft = new ShapedRecipeJson(AB[k], 1, "U", "V").addInput('U', k == 0 ? other : casing).addInput('V',
                                k == 1 ? other : casing);

                        MachineRecipeBuilder craftAsbl = craft.exportToMachine(MIMachineRecipeTypes.ASSEMBLER, 8, 200, 1);

                        MachineRecipeBuilder unpacker = new MachineRecipeBuilder(MIMachineRecipeTypes.UNPACKER, 2, 200).addItemOutput(casing, 1)
                                .addItemOutput(other, 1).addItemInput(AB[k], 1);

                        ShapedRecipeJson craftFromOther = new ShapedRecipeJson(AB[k], 1, "U").addInput('U', AB[(k + 1) % 2]);

                        craft.offerTo(consumer, pathPrefix + casings[i] + "/" + prefixes[j][k] + "_hatch");
                        craftFromOther.offerTo(consumer, pathPrefix + casings[i] + "/" + prefixes[j][k] + "_from_" + (k == 0 ? "output" : "input"));
                        craftAsbl.offerTo(consumer, pathPrefix + casings[i] + "/assembler/" + prefixes[j][k] + "_hatch");
                        unpacker.offerTo(consumer, pathPrefix + casings[i] + "/unpacker/" + prefixes[j][k] + "_hatch");

                    }

                }
            }

        }

    }
}
