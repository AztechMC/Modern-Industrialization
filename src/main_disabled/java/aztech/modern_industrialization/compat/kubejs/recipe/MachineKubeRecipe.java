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

package aztech.modern_industrialization.compat.kubejs.recipe;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.slf4j.Logger;

public class MachineKubeRecipe extends KubeRecipe implements ProcessConditionHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(MI.id("machine"), MachineKubeRecipe.class, MachineKubeRecipe::new);

    private <T> MachineKubeRecipe addToList(RecipeKey<List<T>> key, T element) {
        // In 1.21, this can be null when the recipe is constructed via a JS constructor apparently.
        var value = getValue(key);
        var list = value == null ? new ArrayList<T>() : new ArrayList<>(value);
        list.add(element);
        setValue(key, list);
        return this;
    }

    public MachineKubeRecipe itemIn(SizedIngredient ingredient) {
        return itemIn(ingredient, 1F);
    }

    public MachineKubeRecipe itemIn(SizedIngredient ingredient, float chance) {
        return addToList(MachineRecipeSchema.ITEM_INPUTS, new MachineRecipe.ItemInput(ingredient.ingredient(), ingredient.count(), chance));
    }

    public MachineKubeRecipe itemOut(ItemStack output) {
        return itemOut(output, 1F);
    }

    public MachineKubeRecipe itemOut(ItemStack output, float chance) {
        return addToList(MachineRecipeSchema.ITEM_OUTPUTS, new MachineRecipe.ItemOutput(ItemVariant.of(output), output.getCount(), chance));
    }

    public MachineKubeRecipe fluidIn(SizedFluidIngredient ingredient) {
        return fluidInInternal(ingredient, 1F); // skip chance == 1 check
    }

    public MachineKubeRecipe fluidIn(SizedFluidIngredient ingredient, float chance) {
        // TODO: remove this mess once we don't need to worry about backwards compat anymore.
        if ((ingredient.amount() == 1 || ingredient.amount() == 1000) && chance > 1) {
            LOGGER.warn("fluidIn with separate fluid and amount is deprecated. Use fluidIn(\"%dx %s\") notation instead.".formatted(
                    (int) chance,
                    BuiltInRegistries.FLUID.getKey(ingredient.ingredient().getStacks()[0].getFluid())));
            return fluidIn(new SizedFluidIngredient(ingredient.ingredient(), (int) chance));
        } else if (ingredient.amount() == 1000 && chance == 1) {
            throw new IllegalArgumentException(
                    "fluidIn(\"some_fluid\", 1) is ambiguous. Use either fluidIn(\"1x some_fluid\") or fluidIn(\"some_fluid\") depending on what you meant.");
        }

        return fluidInInternal(ingredient, chance);
    }

    private MachineKubeRecipe fluidInInternal(SizedFluidIngredient ingredient, float chance) {
        if (chance > 1) {
            throw new IllegalArgumentException("Fluid input chance must be between 0 and 1. It is " + chance);
        }
        return addToList(MachineRecipeSchema.FLUID_INPUTS, new MachineRecipe.FluidInput(ingredient.ingredient(), ingredient.amount(), chance));
    }

    public MachineKubeRecipe fluidOut(FluidStack output) {
        return fluidOutInternal(output, 1F); // skip chance == 1 check
    }

    public MachineKubeRecipe fluidOut(FluidStack output, float chance) {
        // TODO: remove this mess once we don't need to worry about backwards compat anymore.
        if ((output.getAmount() == 1 || output.getAmount() == 1000) && chance > 1) {
            LOGGER.warn("fluidOut with separate fluid and amount is deprecated. Use fluidOut(\"%dx %s\") notation instead.".formatted(
                    (int) chance,
                    BuiltInRegistries.FLUID.getKey(output.getFluid())));
            return fluidOut(output.copyWithAmount((int) chance));
        } else if (output.getAmount() == 1000 && chance == 1) {
            throw new IllegalArgumentException(
                    "fluidOut(\"some_fluid\", 1) is ambiguous. Use either fluidOut(\"1x some_fluid\") or fluidOut(\"some_fluid\") depending on what you meant.");
        }

        return fluidOutInternal(output, chance);
    }

    private MachineKubeRecipe fluidOutInternal(FluidStack output, float chance) {
        if (!output.isComponentsPatchEmpty()) {
            throw new IllegalArgumentException("FluidStack components are not supported in machine recipe outputs.");
        }
        return addToList(MachineRecipeSchema.FLUID_OUTPUTS, new MachineRecipe.FluidOutput(output.getFluid(), output.getAmount(), chance));
    }

    @Deprecated(forRemoval = true)
    public MachineKubeRecipe fluidIn(FluidIngredient fluid, long mbs, float probability) {
        LOGGER.warn("fluidIn with separate fluid and amount is deprecated. Use fluidIn(\"%dx %s\", %f) notation instead.".formatted(
                mbs,
                BuiltInRegistries.FLUID.getKey(fluid.getStacks()[0].getFluid()),
                probability));
        return fluidIn(new SizedFluidIngredient(fluid, (int) mbs), probability);
    }

    @Deprecated(forRemoval = true)
    public MachineKubeRecipe fluidOut(Fluid fluid, long mbs, float probability) {
        LOGGER.warn("fluidOut with separate fluid and amount is deprecated. Use fluidOut(\"%dx %s\", %f) notation instead.".formatted(
                mbs,
                BuiltInRegistries.FLUID.getKey(fluid),
                probability));
        return addToList(MachineRecipeSchema.FLUID_OUTPUTS, new MachineRecipe.FluidOutput(fluid, mbs, probability));
    }

    @Override
    public MachineKubeRecipe processCondition(MachineProcessCondition condition) {
        return addToList(MachineRecipeSchema.MACHINE_PROCESS_CONDITIONS, condition);
    }
}
