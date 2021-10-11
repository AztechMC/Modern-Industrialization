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
package aztech.modern_industrialization.recipe.json;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings({ "FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection" })
public final class MIRecipeJson implements RecipeJson {
    private final String type;
    private final int eu;
    private final int duration;
    private final List<MIItemInput> item_inputs = new ArrayList<>();
    private final List<MIFluidInput> fluid_inputs = new ArrayList<>();
    private final List<MIFluidOutput> fluid_outputs = new ArrayList<>();
    private final List<MIItemOutput> item_outputs = new ArrayList<>();

    private static class MIItemInput {
        String item;
        String tag;
        int amount;
    }

    private static class MIItemInputProbability extends MIItemInput {
        double probability;

        MIItemInputProbability(double probability) {
            this.probability = probability;
        }
    }

    private static class MIFluidInput {
        String fluid;
        int amount;
    }

    private static class MIFluidInputProbability extends MIFluidInput {
        double probability;

        MIFluidInputProbability(double probability) {
            this.probability = probability;
        }
    }

    private static class MIFluidOutput {
        String fluid;
        int amount;
    }

    private static class MIItemOutput {
        String item;
        int amount;
    }

    private static class MIItemOutputProbability extends MIItemOutput {
        double probability;
    }

    public MIRecipeJson(String type, int eu, int duration) {
        this.type = "modern_industrialization:" + type;
        this.eu = eu;
        this.duration = duration;
    }

    public MIRecipeJson addItemInput(String maybeTag, int amount) {
        return addItemInput(maybeTag, amount, 1);
    }

    public MIRecipeJson addItemInput(String maybeTag, int amount, double probability) {
        MIItemInput input = probability == 1 ? new MIItemInput() : new MIItemInputProbability(probability);
        input.amount = amount;
        if (maybeTag.startsWith("#")) {
            input.tag = maybeTag.substring(1);
        } else {
            input.item = maybeTag;
        }
        item_inputs.add(input);
        return this;
    }

    public MIRecipeJson addFluidInput(String fluid, int amount) {
        MIFluidInput input = new MIFluidInput();
        input.fluid = fluid;
        input.amount = amount;
        fluid_inputs.add(input);
        return this;
    }

    public MIRecipeJson addFluidInput(String fluid, int amount, double probability) {
        MIFluidInput input = new MIFluidInputProbability(probability);
        input.fluid = fluid;
        input.amount = amount;
        fluid_inputs.add(input);
        return this;
    }

    public MIRecipeJson addFluidOutput(String fluid, int amount) {
        MIFluidOutput output = new MIFluidOutput();
        output.fluid = fluid;
        output.amount = amount;
        fluid_outputs.add(output);
        return this;
    }

    public MIRecipeJson addFluidInput(Fluid fluid, int amount) {
        Identifier id = Registry.FLUID.getId(fluid);
        if (id.equals(Registry.FLUID.getDefaultId())) {
            throw new RuntimeException("Could not find id for fluid " + fluid);
        }
        return addFluidInput(id.toString(), amount);
    }

    public MIRecipeJson addFluidInput(Fluid fluid, int amount, double probability) {
        Identifier id = Registry.FLUID.getId(fluid);
        if (id.equals(Registry.FLUID.getDefaultId())) {
            throw new RuntimeException("Could not find id for fluid " + fluid);
        }
        return addFluidInput(id.toString(), amount, probability);
    }

    public MIRecipeJson addOutput(String itemId, int amount) {
        MIItemOutput output = new MIItemOutput();
        output.item = itemId;
        output.amount = amount;
        item_outputs.add(output);
        return this;
    }

    public MIRecipeJson addOutput(String itemId, int amount, double probability) {
        MIItemOutputProbability output = new MIItemOutputProbability();
        output.item = itemId;
        output.amount = amount;
        output.probability = probability;
        item_outputs.add(output);
        return this;
    }

}
