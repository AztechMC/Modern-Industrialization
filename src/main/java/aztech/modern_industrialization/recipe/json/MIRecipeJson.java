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
public final class MIRecipeJson {
    private final String type;
    private final int eu;
    private final int duration;
    private final List<MIItemInput> item_inputs = new ArrayList<>();
    private final List<MIFluidInput> fluid_inputs = new ArrayList<>();
    private final List<MIItemOutput> item_outputs = new ArrayList<>();

    private static class MIItemInput {
        String item;
        String tag;
        int amount;
    }

    private static class MIFluidInput {
        String fluid;
        int amount;
    }

    private static class MIItemOutput {
        String item;
        int amount;
    }

    public MIRecipeJson(String type, int eu, int duration) {
        this.type = "modern_industrialization:" + type;
        this.eu = eu;
        this.duration = duration;
    }

    public MIRecipeJson addItemInput(String maybeTag, int amount) {
        MIItemInput input = new MIItemInput();
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

    public MIRecipeJson addFluidInput(Fluid fluid, int amount) {
        Identifier id = Registry.FLUID.getId(fluid);
        if (id.equals(Registry.FLUID.getDefaultId())) {
            throw new RuntimeException("Could not find id for fluid " + fluid);
        }
        return addFluidInput(id.toString(), amount);
    }

    public MIRecipeJson addOutput(String itemId, int amount) {
        MIItemOutput output = new MIItemOutput();
        output.item = itemId;
        output.amount = amount;
        item_outputs.add(output);
        return this;
    }
}
