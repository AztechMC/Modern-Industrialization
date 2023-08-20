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

import aztech.modern_industrialization.definition.FluidLike;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings({ "FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection" })
public class MIRecipeJson<T extends MIRecipeJson<?>> extends RecipeJson {

    protected transient final MachineRecipeType machineRecipeType;

    protected MIRecipeJson(MachineRecipeType machineRecipeType, int eu, int duration) {
        this.type = BuiltInRegistries.RECIPE_SERIALIZER.getKey(machineRecipeType).toString();
        this.eu = eu;
        this.duration = duration;
        this.machineRecipeType = machineRecipeType;
    }

    protected MIRecipeJson(MIRecipeJson<?> otherWithSameData) {
        this.type = otherWithSameData.type;
        this.eu = otherWithSameData.eu;
        this.duration = otherWithSameData.duration;
        this.machineRecipeType = otherWithSameData.machineRecipeType;

        this.fluidInputs = otherWithSameData.fluidInputs;
        this.fluidOutputs = otherWithSameData.fluidOutputs;
        this.itemInputs = otherWithSameData.itemInputs;
        this.itemOutputs = otherWithSameData.itemOutputs;
    }

    public static MIRecipeJson<MIRecipeJson<?>> create(MachineRecipeType machineRecipeType, int eu, int duration) {
        return new MIRecipeJson<>(machineRecipeType, eu, duration);
    }

    protected final String type;
    protected final int eu;
    protected final int duration;

    @SerializedName("fluid_inputs")
    private List<MIFluidInput> fluidInputs;
    @SerializedName("fluid_outputs")
    private List<MIFluidOutput> fluidOutputs;

    @SerializedName("item_inputs")
    private List<MIItemInput> itemInputs;
    @SerializedName("item_outputs")
    private List<MIItemOutput> itemOutputs;

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

    private static class MIItemOutput {
        String item;
        int amount;
    }

    private static class MIItemOutputProbability extends MIItemOutput {

        double probability;

        MIItemOutputProbability(double probability) {
            this.probability = probability;
        }
    }

    private static class MIFluidOutput {
        String fluid;
        int amount;
    }

    private static class MIFluidOutputProbability extends MIFluidOutput {
        double probability;

        MIFluidOutputProbability(double probability) {
            this.probability = probability;
        }
    }

    public T addItemInput(TagKey<Item> tag, int amount) {
        return addItemInput("#" + tag.location().toString(), amount);
    }

    public T addItemInput(String maybeTag, int amount) {
        return addItemInput(maybeTag, amount, 1);
    }

    public T addItemInput(ItemLike item, int amount, double probability) {
        return addItemInput(BuiltInRegistries.ITEM.getKey(item.asItem()).toString(), amount, probability);
    }

    public T addItemInput(ItemLike item, int amount) {
        return addItemInput(item.asItem(), amount, 1);
    }

    public T addItemInput(String maybeTag, int amount, double probability) {
        MIItemInput input = probability == 1 ? new MIItemInput() : new MIItemInputProbability(probability);
        input.amount = amount;
        if (maybeTag.startsWith("#")) {
            input.tag = maybeTag.substring(1);
        } else {
            input.item = maybeTag;
        }
        if (itemInputs == null) {
            itemInputs = new ArrayList<>();
        }
        itemInputs.add(input);
        return (T) this;
    }

    public T addItemOutput(String itemId, int amount) {
        return addItemOutput(itemId, amount, 1);
    }

    public T addItemOutput(String itemId, int amount, double probability) {
        MIItemOutput output = new MIItemOutputProbability(probability);
        if (probability == 1) {
            output = new MIItemOutput();
        }
        output.item = itemId;
        output.amount = amount;
        if (itemOutputs == null) {
            itemOutputs = new ArrayList<>();
        }
        itemOutputs.add(output);
        return (T) this;
    }

    public T addItemOutput(ItemLike item, int amount, double probability) {
        return addItemOutput(BuiltInRegistries.ITEM.getKey(item.asItem()).toString(), amount, probability);
    }

    public T addItemOutput(ItemLike item, int amount) {
        return addItemOutput(BuiltInRegistries.ITEM.getKey(item.asItem()).toString(), amount);
    }

    public T addItemOutput(Item item, int amount) {
        return addItemOutput(item, amount, 1);
    }

    public T addFluidInput(String fluid, int amount) {
        return addFluidInput(fluid, amount, 1);
    }

    public T addFluidInput(String fluid, int amount, double probability) {

        MIFluidInput input = new MIFluidInputProbability(probability);
        if (probability == 1) {
            input = new MIFluidInput();
        }
        input.fluid = fluid;
        input.amount = amount;
        if (fluidInputs == null) {
            fluidInputs = new ArrayList<>();
        }
        fluidInputs.add(input);
        return (T) this;
    }

    public T addFluidInput(Fluid fluid, int amount, double probability) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id.equals(BuiltInRegistries.FLUID.getDefaultKey())) {
            throw new RuntimeException("Could not find id for fluid " + fluid);
        }
        return addFluidInput(id.toString(), amount, probability);
    }

    public T addFluidInput(FluidLike fluid, int amount, double probability) {
        return addFluidInput(fluid.asFluid(), amount, probability);
    }

    public T addFluidInput(FluidLike fluid, int amount) {
        return addFluidInput(fluid.asFluid(), amount);
    }

    public T addFluidInput(Fluid fluid, int amount) {
        return addFluidInput(fluid, amount, 1);
    }

    public T addFluidOutput(String fluid, int amount) {
        return addFluidOutput(fluid, amount, 1);
    }

    public T addFluidOutput(FluidLike fluid, int amount) {
        return addFluidOutput(fluid.asFluid(), amount);
    }

    public T addFluidOutput(String fluid, int amount, double probability) {
        MIFluidOutput output = new MIFluidOutputProbability(probability);
        if (probability == 1) {
            output = new MIFluidOutput();
        }
        output.fluid = fluid;
        output.amount = amount;
        if (fluidOutputs == null) {
            fluidOutputs = new ArrayList<>();
        }
        fluidOutputs.add(output);
        return (T) this;
    }

    public T addFluidOutput(Fluid fluid, int amount) {
        return addFluidOutput(fluid, amount, 1);
    }

    public T addFluidOutput(Fluid fluid, int amount, double probability) {
        return addFluidOutput(BuiltInRegistries.FLUID.getKey(fluid).toString(), amount, probability);
    }

    @Override
    public JsonObject toJsonObject() {
        var ret = super.toJsonObject();
        if (type.equals("modern_industrialization:forge_hammer")) {
            // Remove duration from forge hammer recipes and convert eu to hammer_damage if > 0
            ret.remove("duration");
            int dmg = ret.remove("eu").getAsInt();
            if (dmg > 0) {
                ret.addProperty("hammer_damage", dmg);
            }
        }
        return ret;
    }
}
